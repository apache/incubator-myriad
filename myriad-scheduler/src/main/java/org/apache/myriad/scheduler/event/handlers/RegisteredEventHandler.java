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
package org.apache.myriad.scheduler.event.handlers;

import com.lmax.disruptor.EventHandler;
import javax.inject.Inject;
import org.apache.myriad.scheduler.event.RegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles and logs mesos registered events
 */
public class RegisteredEventHandler implements EventHandler<RegisteredEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredEventHandler.class);

  @Inject
  private org.apache.myriad.state.SchedulerState schedulerState;

  @Inject
  private org.apache.myriad.scheduler.ReconcileService reconcileService;

  @Override
  public void onEvent(RegisteredEvent event, long sequence, boolean endOfBatch) throws Exception {
    LOGGER.info("Received event: {} with frameworkId: {}", event, event.getFrameworkId());
    schedulerState.setFrameworkId(event.getFrameworkId());
    reconcileService.reconcile(event.getDriver());
  }

}
