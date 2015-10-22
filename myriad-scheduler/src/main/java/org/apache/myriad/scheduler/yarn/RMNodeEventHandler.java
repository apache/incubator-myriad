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
package org.apache.myriad.scheduler.yarn;

import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;

/**
 * Passes the {@link RMNodeEvent} events into the {@link YarnSchedulerInterceptor}.
 */
public class RMNodeEventHandler implements EventHandler<RMNodeEvent> {
  private final YarnSchedulerInterceptor interceptor;
  private final RMContext rmContext;

  public RMNodeEventHandler(YarnSchedulerInterceptor interceptor, RMContext rmContext) {
    this.interceptor = interceptor;
    this.rmContext = rmContext;
  }

  @Override
  public void handle(RMNodeEvent event) {
    interceptor.beforeRMNodeEventHandled(event, rmContext);

  }
}
