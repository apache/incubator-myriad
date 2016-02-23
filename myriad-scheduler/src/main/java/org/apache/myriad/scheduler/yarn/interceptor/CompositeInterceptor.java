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
package org.apache.myriad.scheduler.yarn.interceptor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainerEventType;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerApplicationAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeRemovedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeResourceUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interceptor that wraps other interceptors. The Myriad{Fair,Capacity,Fifo}Scheduler classes
 * instantiate this class and allow interception of the Yarn scheduler events/method calls.
 * <p/>
 * The {@link CompositeInterceptor} allows other interceptors to be registered via {@link InterceptorRegistry}
 * and passes control to the registered interceptors whenever a event/method call is being intercepted.
 */
public class CompositeInterceptor implements YarnSchedulerInterceptor, InterceptorRegistry {
  private static final Logger LOGGER = LoggerFactory.getLogger(CompositeInterceptor.class);

  private Map<Class<?>, YarnSchedulerInterceptor> interceptors = Maps.newLinkedHashMap();
  private YarnSchedulerInterceptor myriadInitInterceptor;

  /**
   * Called by Myriad{Fair,Capacity,Fifo}Scheduler classes. Creates an instance of
   * {@link MyriadInitializationInterceptor}.
   */
  public CompositeInterceptor() {
    this.myriadInitInterceptor = new MyriadInitializationInterceptor(this);
  }

  @VisibleForTesting
  public void setMyriadInitInterceptor(YarnSchedulerInterceptor myriadInitInterceptor) {
    this.myriadInitInterceptor = myriadInitInterceptor;
  }

  @Override
  public void register(YarnSchedulerInterceptor interceptor) {
    interceptors.put(interceptor.getClass(), interceptor);
    LOGGER.info("Registered {} into the registry.", interceptor.getClass().getName());
  }

  @Override
  public CallBackFilter getCallBackFilter() {
    return new CallBackFilter() {
      @Override
      public boolean allowCallBacksForNode(NodeId nodeManager) {
        return true;
      }
    };
  }

  @Override
  public void beforeReleaseContainers(List<ContainerId> containers, SchedulerApplicationAttempt attempt){
    if (containers != null && attempt != null) {
      for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
        List<ContainerId> filteredContainers = new ArrayList<>();
        for (ContainerId containerId: containers) {
          NodeId nodeId = attempt.getRMContainer(containerId).getContainer().getNodeId();
          if ((nodeId != null && interceptor.getCallBackFilter().allowCallBacksForNode(nodeId))) {
            filteredContainers.add(containerId);
          }
        }
        if (!filteredContainers.isEmpty()) {
          interceptor.beforeReleaseContainers(filteredContainers, attempt);
        }
      }
    }
  }
  @Override
  public void beforeCompletedContainer(RMContainer rmContainer, ContainerStatus containerStatus, RMContainerEventType event) {
    if (rmContainer != null && rmContainer.getContainer() != null) {
      NodeId nodeId = rmContainer.getContainer().getNodeId();
      for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
        if (interceptor.getCallBackFilter().allowCallBacksForNode(nodeId)) {
          interceptor.beforeCompletedContainer(rmContainer, containerStatus, event);
        }
      }
    }
  }

  /**
   * Allows myriad to be initialized via {@link #myriadInitInterceptor}. After myriad is initialized,
   * other interceptors will later register with this class via
   * {@link InterceptorRegistry#register(YarnSchedulerInterceptor)}.
   *
   * @param conf
   * @param yarnScheduler
   * @param rmContext
   * @throws IOException
   */
  @Override
  public void init(Configuration conf, AbstractYarnScheduler yarnScheduler, RMContext rmContext) throws IOException {
    myriadInitInterceptor.init(conf, yarnScheduler, rmContext);
  }

  @Override
  public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {
    for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
      if (interceptor.getCallBackFilter().allowCallBacksForNode(event.getNodeId())) {
        interceptor.beforeRMNodeEventHandled(event, context);
      }
    }
  }

  @Override
  public void beforeSchedulerEventHandled(SchedulerEvent event) {
    for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
      final NodeId nodeId = getNodeIdForSchedulerEvent(event);
      if (nodeId != null && interceptor.getCallBackFilter().allowCallBacksForNode(nodeId)) {
        interceptor.beforeSchedulerEventHandled(event);
      }
    }
  }

  @Override
  public void afterSchedulerEventHandled(SchedulerEvent event) {
    for (YarnSchedulerInterceptor interceptor : interceptors.values()) {
      NodeId nodeId = getNodeIdForSchedulerEvent(event);
      if (nodeId != null && interceptor.getCallBackFilter().allowCallBacksForNode(nodeId)) {
        interceptor.afterSchedulerEventHandled(event);
      }
    }
  }

  private NodeId getNodeIdForSchedulerEvent(SchedulerEvent event) {
    switch (event.getType()) {
      case NODE_ADDED:
        return ((NodeAddedSchedulerEvent) event).getAddedRMNode().getNodeID();
      case NODE_REMOVED:
        return ((NodeRemovedSchedulerEvent) event).getRemovedRMNode().getNodeID();
      case NODE_UPDATE:
        return ((NodeUpdateSchedulerEvent) event).getRMNode().getNodeID();
      case NODE_RESOURCE_UPDATE:
        return ((NodeResourceUpdateSchedulerEvent) event).getRMNode().getNodeID();
    }
    return null;
  }
}
