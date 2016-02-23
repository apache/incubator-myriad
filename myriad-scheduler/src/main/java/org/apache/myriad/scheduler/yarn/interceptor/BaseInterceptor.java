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
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerApplicationAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;

/**
 * A no-op interceptor whose sole purpose is to serve as a base class
 * for other interceptors. Child interceptors can selectively override the
 * required methods.
 */
public class BaseInterceptor implements YarnSchedulerInterceptor {
  // restrict the constructor
  protected BaseInterceptor() {
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
  }

  @Override
  public void beforeCompletedContainer(RMContainer rmContainer, ContainerStatus containerStatus, RMContainerEventType event) {
  }

  @Override
  public void init(Configuration conf, AbstractYarnScheduler yarnScheduler, RMContext rmContext) throws IOException {
  }

  @Override
  public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {

  }

  @Override
  public void beforeSchedulerEventHandled(SchedulerEvent event) {

  }

  @Override
  public void afterSchedulerEventHandled(SchedulerEvent event) {

  }
}
