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

import com.ebay.myriad.scheduler.event.StatusUpdateEvent;
import com.ebay.myriad.state.SchedulerState;
import com.lmax.disruptor.EventHandler;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * handles and logs mesos status update events
 */
public class StatusUpdateEventHandler implements EventHandler<StatusUpdateEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdateEventHandler.class);

    @Inject
    private SchedulerState schedulerState;

    @Override
    public void onEvent(StatusUpdateEvent event,
                        long sequence,
                        boolean endOfBatch) throws Exception {
        TaskStatus status = event.getStatus();
        this.schedulerState.updateTask(status);
        TaskID taskId = status.getTaskId();
        if (!schedulerState.hasTask(taskId)) {
            LOGGER.warn("Task: {} not found, status: {}", taskId, status.getState());
            return;
        }
        LOGGER.info("Status Update for task: {} | state: {}", taskId, status.getState());
        TaskState state = status.getState();

        switch (state) {
            case TASK_STAGING:
                schedulerState.makeTaskStaging(taskId);
                break;
            case TASK_STARTING:
                schedulerState.makeTaskStaging(taskId);
                break;
            case TASK_RUNNING:
                schedulerState.makeTaskActive(taskId);
                break;
            case TASK_FINISHED:
                schedulerState.removeTask(taskId);
                break;
            case TASK_FAILED:
                // Add to pending tasks
                schedulerState.makeTaskPending(taskId);
                break;
            case TASK_KILLED:
                schedulerState.removeTask(taskId);
                break;
            case TASK_LOST:
                schedulerState.makeTaskPending(taskId);
                break;
            default:
                LOGGER.error("Invalid state: {}", state);
                break;
        }
    }
}
