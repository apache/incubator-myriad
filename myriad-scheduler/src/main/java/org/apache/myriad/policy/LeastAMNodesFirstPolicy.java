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
package org.apache.myriad.policy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeRemovedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.mesos.Protos;
import org.apache.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A scale down policy that maintains returns a list of nodes running least number of AMs.
 */
public class LeastAMNodesFirstPolicy extends BaseInterceptor implements NodeScaleDownPolicy {
  private static final Logger LOGGER = LoggerFactory.getLogger(LeastAMNodesFirstPolicy.class);

  private final AbstractYarnScheduler yarnScheduler;
  private final SchedulerState schedulerState;

  //TODO(Santosh): Should figure out the right values for the hashmap properties.
  // currently it's tuned for 200 nodes and 50 RM RPC threads (Yarn's default).
  private static final int INITIAL_NODE_SIZE = 200;
  private static final int EXPECTED_CONCURRENT_ACCCESS_COUNT = 50;
  private static final float LOAD_FACTOR_DEFAULT = 0.75f;

  private Map<String, SchedulerNode> schedulerNodes = new ConcurrentHashMap<>(INITIAL_NODE_SIZE, LOAD_FACTOR_DEFAULT,
      EXPECTED_CONCURRENT_ACCCESS_COUNT);

  @Inject
  public LeastAMNodesFirstPolicy(InterceptorRegistry registry, AbstractYarnScheduler yarnScheduler, SchedulerState schedulerState) {
    registry.register(this);
    this.yarnScheduler = yarnScheduler;
    this.schedulerState = schedulerState;
  }

  /**
   * Sort the given list of tasks by the number of App Master containers running on the corresponding NM node.
   *
   * @param taskIDs
   */
  @Override
  public void apply(List<Protos.TaskID> taskIDs) {
    if (LOGGER.isDebugEnabled()) {
      for (SchedulerNode node : schedulerNodes.values()) {
        LOGGER.debug("Host {} is running {} containers including {} App Masters", node.getNodeID().getHost(),
            node.getRunningContainers().size(), getNumAMContainers(node.getRunningContainers()));
      }
    }
    // We need to lock the YARN scheduler here. If we don't do that, then the YARN scheduler can
    // process HBs from NodeManagers and the state of SchedulerNode objects might change while we
    // are in the middle of sorting them based on the least number of AM containers.
    synchronized (yarnScheduler) {
      Collections.sort(taskIDs, new Comparator<Protos.TaskID>() {
        @Override
        public int compare(Protos.TaskID t1, Protos.TaskID t2) {
          SchedulerNode o1 = schedulerNodes.get(schedulerState.getTask(t1).getHostname());
          SchedulerNode o2 = schedulerNodes.get(schedulerState.getTask(t2).getHostname());

          if (o1 == null) { // a NM was launched by Myriad, but it hasn't yet registered with RM
            if (o2 == null) {
              return 0;
            } else {
              return -1;
            }
          } else if (o2 == null) {
            return 1;
          } // else, both the NMs have registered with RM

          List<RMContainer> runningContainers1 = o1.getRunningContainers();
          List<RMContainer> runningContainers2 = o2.getRunningContainers();

          Integer numRunningAMs1 = getNumAMContainers(runningContainers1);
          Integer numRunningAMs2 = getNumAMContainers(runningContainers2);

          Integer numRunningContainers1 = runningContainers1.size();
          Integer numRunningContainers2 = runningContainers2.size();

          // If two NMs are running equal number of AMs, sort them based on total num of running containers
          if (numRunningAMs1.compareTo(numRunningAMs2) == 0) {
            return numRunningContainers1.compareTo(numRunningContainers2);
          }
          return numRunningAMs1.compareTo(numRunningAMs2);
        }
      });
    }
  }

  @Override
  public void afterSchedulerEventHandled(SchedulerEvent event) {

    try {
      switch (event.getType()) {
        case NODE_UPDATE:
          onNodeUpdated((NodeUpdateSchedulerEvent) event);
          break;

        case NODE_REMOVED:
          onNodeRemoved((NodeRemovedSchedulerEvent) event);
          break;

        default:
          break;
      }
    } catch (ClassCastException e) {
      LOGGER.error("incorrect event object", e);
    }
  }

  /**
   * Called whenever a NM HBs to RM. The NM's updates will already be recorded in the
   * SchedulerNode before this method is called.
   *
   * @param event
   */
  private void onNodeUpdated(NodeUpdateSchedulerEvent event) {
    NodeId nodeID = event.getRMNode().getNodeID();
    SchedulerNode schedulerNode = yarnScheduler.getSchedulerNode(nodeID);
    schedulerNodes.put(nodeID.getHost(), schedulerNode); // keep track of only one node per host
  }

  private void onNodeRemoved(NodeRemovedSchedulerEvent event) {
    SchedulerNode schedulerNode = schedulerNodes.get(event.getRemovedRMNode().getNodeID().getHost());
    if (schedulerNode != null && schedulerNode.getNodeID().equals(event.getRemovedRMNode().getNodeID())) {
      schedulerNodes.remove(schedulerNode.getNodeID().getHost());
    }
  }

  private Integer getNumAMContainers(List<RMContainer> containers) {
    int count = 0;
    for (RMContainer container : containers) {
      if (container.isAMContainer()) {
        count++;
      }
    }
    return count;
  }
}
