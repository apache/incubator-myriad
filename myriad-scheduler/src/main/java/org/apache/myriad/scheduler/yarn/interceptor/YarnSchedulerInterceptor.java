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

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainerEventType;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeImpl;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerApplicationAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.YarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;

/**
 * Allows interception of YARN's scheduler events (or methods).
 */
public interface YarnSchedulerInterceptor {

  /**
   * Filters the method callbacks.
   */
  interface CallBackFilter {
    /**
     * Method to determine if any other methods in {@link YarnSchedulerInterceptor}
     * pertaining to a given node manager should be invoked or not.
     *
     * @param nodeManager NodeId of the Node Manager registered with RM.
     * @return true to allow invoking further interceptor methods. false otherwise.
     */
    public boolean allowCallBacksForNode(NodeId nodeManager);
  }

  /**
   * Return an instance of {@link CallBackFilter}. {@link CallBackFilter#allowCallBacksForNode(NodeId)}
   * method is invoked to *determine* if any of the other methods pertaining to a specific node
   * needs to be invoked or not.
   *
   * @return
   */
  public CallBackFilter getCallBackFilter();

  /**
   * Invoked *before* {@link org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler#releaseContainers(List,
   * SchedulerApplicationAttempt)}
   * only if {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
  */

  public void beforeReleaseContainers(List<ContainerId> containers, SchedulerApplicationAttempt attempt);

  /**
   * Invoked *before* {@link org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler#completedContainer(RMContainer,
   * ContainerStatus, RMContainerEventType)}
   * only if {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
   */
  public void beforeCompletedContainer(RMContainer rmContainer, ContainerStatus containerStatus, RMContainerEventType event);

    /**
     * Invoked *before* {@link AbstractYarnScheduler#reinitialize(Configuration, RMContext)}
     *
     * @param conf
     * @param yarnScheduler
     * @param rmContext
     * @throws IOException
     */
  public void init(Configuration conf, AbstractYarnScheduler yarnScheduler, RMContext rmContext) throws IOException;

  /**
   * Invoked *before* {@link RMNodeImpl#handle(RMNodeEvent)} only if
   * {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
   *
   * @param event
   * @param context
   */
  public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context);

  /**
   * Invoked *before* {@link YarnScheduler#handle(org.apache.hadoop.yarn.event.Event)} only if
   * {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
   *
   * @param event
   */
  public void beforeSchedulerEventHandled(SchedulerEvent event);

  /**
   * Invoked *after* {@link YarnScheduler#handle(org.apache.hadoop.yarn.event.Event)} only if
   * {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
   *
   * @param event
   */
  public void afterSchedulerEventHandled(SchedulerEvent event);

}
