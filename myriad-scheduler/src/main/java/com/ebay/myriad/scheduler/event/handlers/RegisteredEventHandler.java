/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.scheduler.event.handlers;

import com.ebay.myriad.scheduler.ReconcileService;
import com.ebay.myriad.scheduler.event.RegisteredEvent;
import com.ebay.myriad.state.SchedulerState;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * handles and logs mesos registered events
 */
public class RegisteredEventHandler implements EventHandler<RegisteredEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredEventHandler.class);

    @Inject
    private SchedulerState schedulerState;

    @Inject
    private ReconcileService reconcileService;

    @Override
    public void onEvent(RegisteredEvent event, long sequence, boolean endOfBatch) throws Exception {
        LOGGER.info("Received event: {} with frameworkId: {}", event, event.getFrameworkId());
        schedulerState.getMyriadState().setFrameworkId(event.getFrameworkId());
        reconcileService.reconcile(event.getDriver());
    }

}
