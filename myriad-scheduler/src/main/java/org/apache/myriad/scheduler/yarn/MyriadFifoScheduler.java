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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEventType;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;

/**
 * {@link MyriadFifoScheduler} just extends YARN's {@link FifoScheduler} and
 * allows some of the {@link FifoScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFifoScheduler extends FifoScheduler {
  private Configuration conf;

  private RMContext rmContext;
  private YarnSchedulerInterceptor yarnSchedulerInterceptor;
  private RMNodeEventHandler rmNodeEventHandler;

  public MyriadFifoScheduler() {
    super();
  }

  /**
   * Register an event handler that receives {@link RMNodeEvent} events.
   * This event handler is registered ahead of RM's own event handler for RMNodeEvents.
   * For e.g. myriad can inspect a node's HB (RMNodeStatusEvent) before the HB is handled by
   * RM and the scheduler.
   *
   * @param rmContext
   */
  @Override
  public synchronized void setRMContext(RMContext rmContext) {
    this.rmContext = rmContext;
    this.yarnSchedulerInterceptor = new CompositeInterceptor();
    rmNodeEventHandler = new RMNodeEventHandler(yarnSchedulerInterceptor, rmContext);
    rmContext.getDispatcher().register(RMNodeEventType.class, rmNodeEventHandler);
    super.setRMContext(rmContext);
  }

  /**
   * ******** Methods overridden from YARN {@link FifoScheduler}  *********************
   */

  @Override
  public synchronized void serviceInit(Configuration conf) throws Exception {
    this.conf = conf;
    super.serviceInit(conf);
  }

  @Override
  public synchronized void serviceStart() throws Exception {
    this.yarnSchedulerInterceptor.init(conf, this, rmContext);
    super.serviceStart();
  }

  @Override
  public synchronized void handle(SchedulerEvent event) {
    this.yarnSchedulerInterceptor.beforeSchedulerEventHandled(event);
    super.handle(event);
    this.yarnSchedulerInterceptor.afterSchedulerEventHandled(event);
  }
}

