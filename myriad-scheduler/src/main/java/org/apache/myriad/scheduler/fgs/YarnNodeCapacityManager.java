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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;

import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceOption;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainerEventType;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerApplicationAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeResourceUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.executor.ContainerTaskStatusRequest;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.ResourceUtils;
import org.apache.myriad.scheduler.SchedulerUtils;
import org.apache.myriad.scheduler.TaskUtils;
import org.apache.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the capacity exposed by NodeManager. It uses the offers available
 * from Mesos to inflate the node capacity and lets ResourceManager make the
 * scheduling decision. After the scheduling decision is done, there are 2 cases:
 * <p/>
 * 1. If ResourceManager did not use the expanded capacity, then the node's
 * capacity is reverted back to original value and the offer is declined.
 * 2. If ResourceManager ended up using the expanded capacity, then the node's
 * capacity is updated accordingly and any unused capacity is returned back to
 * Mesos.
 */
public class YarnNodeCapacityManager extends BaseInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(YarnNodeCapacityManager.class);

  private final AbstractYarnScheduler yarnScheduler;
  private final RMContext rmContext;
  private final MyriadDriver myriadDriver;
  private final OfferLifecycleManager offerLifecycleMgr;
  private final NodeStore nodeStore;
  private final SchedulerState state;
  private static final Lock yarnSchedulerLock = new ReentrantLock();
  private static final Resource ZERO_RESOURCE = Resource.newInstance(0, 0);
  private TaskUtils taskUtils;

  @Inject
  public YarnNodeCapacityManager(InterceptorRegistry registry, AbstractYarnScheduler yarnScheduler, RMContext rmContext,
                                 MyriadDriver myriadDriver, OfferLifecycleManager offerLifecycleMgr, NodeStore nodeStore,
                                 SchedulerState state, TaskUtils taskUtils) {
    if (registry != null) {
      registry.register(this);
    }
    this.yarnScheduler = yarnScheduler;
    this.rmContext = rmContext;
    this.myriadDriver = myriadDriver;
    this.offerLifecycleMgr = offerLifecycleMgr;
    this.nodeStore = nodeStore;
    this.state = state;
    this.taskUtils = taskUtils;
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

  private Protos.TaskID containerToTaskId(RMContainer container) {
    return Protos.TaskID.newBuilder().setValue("yarn_" + container.getContainerId()).build();
  }

  @Override
  public void beforeReleaseContainers(List<ContainerId> containerIds, SchedulerApplicationAttempt attempt) {
    //NOOP beforeCompletedContainer does this
  }

  @Override
  public void beforeCompletedContainer(RMContainer rmContainer, ContainerStatus containerStatus, RMContainerEventType type) {
    if (type.equals(RMContainerEventType.KILL) || type.equals(RMContainerEventType.RELEASED)) {
      LOGGER.info("{} completed with exit status {}, killing cooresponding mesos task.", rmContainer.getContainerId().toString(), type);
      removeYarnTask(rmContainer);
    }
  }

  private void removeYarnTask(RMContainer rmContainer) {
    if (rmContainer != null && rmContainer.getContainer() != null) {
      Protos.TaskID taskId = containerToTaskId(rmContainer);
      //TODO (darinj) Reliable messaging
      state.makeTaskKillable(taskId);
      myriadDriver.kill(taskId);
      String hostname = rmContainer.getContainer().getNodeId().getHost();
      Node node = nodeStore.getNode(hostname);
      if (node != null) {
        RMNode rmNode = node.getNode().getRMNode();
        Resource resource = rmContainer.getContainer().getResource();
        decrementNodeCapacity(rmNode, resource);
        LOGGER.info("Removed task yarn_{} with exit status freeing {} cpu and {} mem.", rmContainer.getContainer().toString(),
            rmContainer.getContainerExitStatus(), resource.getVirtualCores(), resource.getMemory());
      } else {
        LOGGER.warn(hostname + " not found");
      }
    }
  }

  @Override
  public void afterSchedulerEventHandled(SchedulerEvent event) {
    switch (event.getType()) {
      case NODE_ADDED:
        if (!(event instanceof NodeAddedSchedulerEvent)) {
          LOGGER.error("{} not an instance of {}", event.getClass().getName(), NodeAddedSchedulerEvent.class.getName());
          return;
        }

        NodeAddedSchedulerEvent nodeAddedEvent = (NodeAddedSchedulerEvent) event;
        NodeId nodeId = nodeAddedEvent.getAddedRMNode().getNodeID();
        String host = nodeId.getHost();

        SchedulerNode node = yarnScheduler.getSchedulerNode(nodeId);
        nodeStore.add(node);
        LOGGER.info("afterSchedulerEventHandled: NM registration from node {}", host);
        break;

      case NODE_UPDATE:
        if (!(event instanceof NodeUpdateSchedulerEvent)) {
          LOGGER.error("{} not an instance of {}", event.getClass().getName(), NodeUpdateSchedulerEvent.class.getName());
          return;
        }

        RMNode rmNode = ((NodeUpdateSchedulerEvent) event).getRMNode();
        handleContainerAllocation(rmNode);

        break;

      default:
        break;
    }
  }

  /**
   * Checks if any containers were allocated in the current scheduler run and
   * launches the corresponding Mesos tasks. It also udpates the node
   * capacity depending on what portion of the consumed offers were actually
   * used.
   */
  @VisibleForTesting
  protected void handleContainerAllocation(RMNode rmNode) {
    String host = rmNode.getNodeID().getHost();

    ConsumedOffer consumedOffer = offerLifecycleMgr.drainConsumedOffer(host);
    if (consumedOffer == null) {
      LOGGER.debug("No offer consumed for {}", host);
      return;
    }

    Node node = nodeStore.getNode(host);
    Set<RMContainer> containersBeforeSched = node.getContainerSnapshot();
    Set<RMContainer> containersAfterSched = new HashSet<>(node.getNode().getRunningContainers());

    Set<RMContainer> containersAllocatedByMesosOffer = (containersBeforeSched == null) ? containersAfterSched : Sets.difference(
        containersAfterSched, containersBeforeSched);

    if (containersAllocatedByMesosOffer.isEmpty()) {
      LOGGER.debug("No containers allocated using Mesos offers for host: {}", host);
      for (Protos.Offer offer : consumedOffer.getOffers()) {
        offerLifecycleMgr.declineOffer(offer);
      }
      decrementNodeCapacity(rmNode, OfferUtils.getYarnResourcesFromMesosOffers(consumedOffer.getOffers()));
    } else {
      LOGGER.debug("Containers allocated using Mesos offers for host: {} count: {}", host, containersAllocatedByMesosOffer.size());

      // Identify the Mesos tasks that need to be launched
      List<Protos.TaskInfo> tasks = Lists.newArrayList();
      Resource resUsed = Resource.newInstance(0, 0);

      for (RMContainer newContainer : containersAllocatedByMesosOffer) {
        tasks.add(getTaskInfoForContainer(newContainer, consumedOffer, node));
        resUsed = Resources.add(resUsed, newContainer.getAllocatedResource());
      }

      // Reduce node capacity to account for unused offers
      Resource resOffered = OfferUtils.getYarnResourcesFromMesosOffers(consumedOffer.getOffers());
      Resource resUnused = Resources.subtract(resOffered, resUsed);
      decrementNodeCapacity(rmNode, resUnused);
      myriadDriver.getDriver().launchTasks(consumedOffer.getOfferIds(), tasks);
    }

    // No need to hold on to the snapshot anymore
    node.removeContainerSnapshot();
  }


  public void incrementNodeCapacity(RMNode rmNode, Resource addedCapacity) {
    setNodeCapacity(rmNode, Resources.add(rmNode.getTotalCapability(), addedCapacity));
  }

  public void decrementNodeCapacity(RMNode rmNode, Resource removedCapacity) {
    setNodeCapacity(rmNode, Resources.subtract(rmNode.getTotalCapability(), removedCapacity));
  }

  /**
   * 1. Updates {@link RMNode#getTotalCapability()} with newCapacity.
   * 2. Sends out a {@link NodeResourceUpdateSchedulerEvent} that's handled by YARN's scheduler.
   * The scheduler updates the corresponding {@link SchedulerNode} with the newCapacity.
   *
   * @param rmNode
   * @param newCapacity
   */
  @SuppressWarnings("unchecked")
  public void setNodeCapacity(RMNode rmNode, Resource newCapacity) {
    //NOOP prevent YARN warning changing to same size
    if ((Resources.equals(rmNode.getTotalCapability(), newCapacity))) {
      return;
    }
    if (yarnScheduler.getSchedulerNode(rmNode.getNodeID()) == null) {
      LOGGER.info("Yarn Scheduler doesn't have node {}, probably UNHEALTHY", rmNode.getNodeID());
      return;
    }
    yarnSchedulerLock.lock();
    try {
      if (newCapacity.getMemory() < 0 || newCapacity.getVirtualCores() < 0) {
        Resource zeroed = ResourceUtils.componentwiseMax(ZERO_RESOURCE, newCapacity);
        rmNode.getTotalCapability().setMemory(zeroed.getMemory());
        rmNode.getTotalCapability().setVirtualCores(zeroed.getVirtualCores());
        LOGGER.warn("Asked to set Node {} to a value less than zero!  Had {}, setting to {}.",
            rmNode.getHttpAddress(), rmNode.getTotalCapability().toString(), zeroed.toString());
      } else {
        rmNode.getTotalCapability().setMemory(newCapacity.getMemory());
        rmNode.getTotalCapability().setVirtualCores(newCapacity.getVirtualCores());
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Setting capacity for node {} to {}", rmNode.getHostName(), newCapacity);
        }
      }
      // updates the scheduler with the new capacity for the NM.
      // the event is handled by the scheduler asynchronously
      rmContext.getDispatcher().getEventHandler().handle(new NodeResourceUpdateSchedulerEvent(rmNode, ResourceOption.newInstance(
          rmNode.getTotalCapability(), RMNode.OVER_COMMIT_TIMEOUT_MILLIS_DEFAULT)));
    } finally {
      yarnSchedulerLock.unlock();
    }
  }

  private Protos.TaskInfo getTaskInfoForContainer(RMContainer rmContainer, ConsumedOffer consumedOffer, Node node) {

    Protos.Offer offer = consumedOffer.getOffers().get(0);
    Container container = rmContainer.getContainer();
    Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(
        ContainerTaskStatusRequest.YARN_CONTAINER_TASK_ID_PREFIX + container.getId().toString()).build();

    // TODO (sdaingade) Remove ExecutorInfo from the Node object
    // as this is now cached in the NodeTask object in scheduler state.
    Protos.ExecutorInfo executorInfo = node.getExecInfo();
    if (executorInfo == null) {
      executorInfo = Protos.ExecutorInfo.newBuilder(state.getNodeTask(offer.getSlaveId(), NodeManagerConfiguration.NM_TASK_PREFIX)
          .getExecutorInfo()).setFrameworkId(offer.getFrameworkId()).build();
      node.setExecInfo(executorInfo);
    }

    return Protos.TaskInfo.newBuilder()
        .setName("task_" + taskId.getValue()).setTaskId(taskId)
        .setSlaveId(offer.getSlaveId())
        .addAllResources(taskUtils.getScalarResource(offer, "cpus", (double) container.getResource().getVirtualCores(), 0.0))
        .addAllResources(taskUtils.getScalarResource(offer, "mem", (double) container.getResource().getMemory(), 0.0))
        .setExecutor(executorInfo)
        .build();
  }
}
