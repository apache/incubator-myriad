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

import com.ebay.myriad.scheduler.event.ExecutorLostEvent;
import com.lmax.disruptor.EventHandler;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.SlaveID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles and logs executor lost events
 */
public class ExecutorLostEventHandler implements EventHandler<ExecutorLostEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorLostEventHandler.class);

    @Override
    public void onEvent(ExecutorLostEvent event, long sequence, boolean endOfBatch) throws Exception {
        ExecutorID executorId = event.getExecutorId();
        SlaveID slaveId = event.getSlaveId();
        int exitStatus = event.getExitStatus();
        LOGGER.info("Executor {} of slave {} lost with exit status: {}",
                executorId, slaveId, exitStatus);
    }

}
