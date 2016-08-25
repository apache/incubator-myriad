/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler.fgs;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.SchedulerUtils;
import org.apache.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles node manager heartbeat.
 */
public class NMHeartBeatHandler extends BaseInterceptor {
  @VisibleForTesting
  Logger logger = LoggerFactory.getLogger(NMHeartBeatHandler.class);

  private final AbstractYarnScheduler yarnScheduler;
  private final MyriadDriver myriadDriver;
  private final YarnNodeCapacityManager yarnNodeCapacityMgr;
  private final OfferLifecycleManager offerLifecycleMgr;
  private final NodeStore nodeStore;
  private final SchedulerState state;
  private final NodeManagerConfiguration conf;

  @Inject
  public NMHeartBeatHandler(InterceptorRegistry registry, AbstractYarnScheduler yarnScheduler, MyriadDriver myriadDriver,
                            YarnNodeCapacityManager yarnNodeCapacityMgr, OfferLifecycleManager offerLifecycleMgr,
                            NodeStore nodeStore, SchedulerState state, NodeManagerConfiguration conf) {

    if (registry != null) {
      registry.register(this);
    }

    this.yarnScheduler = yarnScheduler;
    this.myriadDriver = myriadDriver;
    this.yarnNodeCapacityMgr = yarnNodeCapacityMgr;
    this.offerLifecycleMgr = offerLifecycleMgr;
    this.nodeStore = nodeStore;
    this.state = state;
    this.conf = conf;
  }

  @Override
  public CallBackFilter getCallBackFilter() {
    return new CallBackFilter() {
      @Override
      public boolean allowCallBacksForNode(NodeId nodeManager) {
        return SchedulerUtils.isEligibleForFineGrainedScaling(nodeManager.getHost(), state);
      }
    };
  }

  @Override
  public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {
    switch (event.getType()) {
      case STARTED:
        // Since the RMNode was just started, it should not have a non-zero capacity
        RMNode rmNode = context.getRMNodes().get(event.getNodeId());
        
        if (isNonZeroCapacityNode(rmNode)) {
          Resource totalCapability = rmNode.getTotalCapability();
          logger.warn(
              "FineGrainedScaling feature got invoked for a NM with non-zero capacity. Host: {}, Mem: {}, CPU: {}. Setting the " +
              "NM's capacity to (0G,0CPU)", rmNode.getHostName(), totalCapability.getMemory(), totalCapability.getVirtualCores());
          totalCapability.setMemory(0);
          totalCapability.setVirtualCores(0);
        }
        break;

      case STATUS_UPDATE:
        handleStatusUpdate(event, context);
        break;

      default:
        break;
    }
  }

  @VisibleForTesting
  protected boolean isNonZeroCapacityNode(RMNode node) {
    Resource resource = node.getTotalCapability();
    return (resource.getMemory() != 0 || resource.getVirtualCores() != 0);
  }
  
  @VisibleForTesting
  protected void handleStatusUpdate(RMNodeEvent event, RMContext context) {
    if (!(event instanceof RMNodeStatusEvent)) {
      logger.error("{} not an instance of {}", event.getClass().getName(), RMNodeStatusEvent.class.getName());
      return;
    }

    RMNodeStatusEvent statusEvent = (RMNodeStatusEvent) event;
    RMNode rmNode = context.getRMNodes().get(event.getNodeId());
    String hostName = rmNode.getNodeID().getHost();

    Node host = nodeStore.getNode(hostName);
    if (host != null) {
      host.snapshotRunningContainers();
    }

    /*
     * Set the new node capacity which is the sum of the current node resources plus those offered by Mesos. 
     * If the sum is greater than the max capacity of the node, reject the offer.
     */
    Resource offeredResources = getNewResourcesOfferedByMesos(hostName);
    Resource currentResources = getResourcesUnderUse(statusEvent);
    
    if (offerWithinResourceLimits(currentResources, offeredResources)) {
      yarnNodeCapacityMgr.setNodeCapacity(rmNode, Resources.add(currentResources, offeredResources));
      logger.info("Updated resources for {} with {} cores and {} memory", rmNode.getNode().getName(), 
              offeredResources.getVirtualCores(), offeredResources.getMemory());
    } else {
      logger.info("Did not update {} with {} cores and {} memory, over max cpu cores and/or max memory", 
              rmNode.getNode().getName(), offeredResources.getVirtualCores(), offeredResources.getMemory());
    }
  }
  
  @VisibleForTesting
  protected boolean offerWithinResourceLimits(Resource currentResources, Resource offeredResources) {
    int newMemory = currentResources.getMemory() + offeredResources.getMemory();
    int newCores = currentResources.getVirtualCores() + offeredResources.getVirtualCores();
       
    return (newMemory <= conf.getJvmMaxMemoryMB() && newCores <= conf.getMaxCpus());
  }
  
  @VisibleForTesting
  protected Resource getNewResourcesOfferedByMesos(String hostname) {
    OfferFeed feed = offerLifecycleMgr.getOfferFeed(hostname);
    List<Offer> offers = new ArrayList<>();
    Protos.Offer offer;
        
    while ((offer = feed.poll()) != null) {
      offers.add(offer);     
      offerLifecycleMgr.markAsConsumed(offer);
    }
    
    Resource fromMesosOffers = OfferUtils.getYarnResourcesFromMesosOffers(offers);

    if (logger.isDebugEnabled()) {
      logger.debug("NM on host {} got {} CPUs and {} memory from mesos", hostname, fromMesosOffers.getVirtualCores(),
          fromMesosOffers.getMemory());
    }

    return fromMesosOffers;
  }

  @VisibleForTesting
  protected Resource getResourcesUnderUse(RMNodeStatusEvent statusEvent) {
    Resource usedResources = Resource.newInstance(0, 0);
    for (ContainerStatus status : statusEvent.getContainers()) {
      if (containerInUse(status)) {
        RMContainer rmContainer = yarnScheduler.getRMContainer(status.getContainerId());
        // (sdaingade) This check is needed as RMContainer information may not be populated
        // immediately after a RM restart.
        if (rmContainer != null) {
          Resources.addTo(usedResources, rmContainer.getAllocatedResource());
        }
      }
    }
    return usedResources;
  }
  
  private boolean containerInUse(ContainerStatus status) {
    return (status.getState() == ContainerState.NEW || status.getState() == ContainerState.RUNNING);
  }
}
