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
import com.ebay.myriad.state.NodeTask;
import com.ebay.myriad.state.SchedulerState;
import com.lmax.disruptor.EventHandler;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class StatusUpdateEventHandler implements
        EventHandler<StatusUpdateEvent> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StatusUpdateEventHandler.class);

    @Inject
    private SchedulerState schedulerState;

    @Override
    public void onEvent(StatusUpdateEvent event, long sequence,
                        boolean endOfBatch) throws Exception {
        TaskStatus status = event.getStatus();
        TaskID taskId = status.getTaskId();
        LOGGER.info("Status Update for task: {} | state: {}", taskId,
                status.getState());
        TaskState state = status.getState();

        String taskIdValue = taskId.getValue();
        switch (state) {
            case TASK_STAGING:
                schedulerState.makeTaskStaging(taskIdValue);
                break;
            case TASK_STARTING:
                schedulerState.makeTaskStaging(taskIdValue);
                break;
            case TASK_RUNNING:
                schedulerState.makeTaskActive(taskIdValue);
                NodeTask task = schedulerState.getTask(taskIdValue);
                schedulerState.releaseLock(task.getClusterId());
                break;
            case TASK_FINISHED:
                schedulerState.removeTask(taskIdValue);
                break;
            case TASK_FAILED:
                // Add to pending tasks
                schedulerState.makeTaskPending(taskIdValue);
                break;
            case TASK_KILLED:
                schedulerState.removeTask(taskIdValue);
                break;
            case TASK_LOST:
                schedulerState.makeTaskPending(taskIdValue);
                break;
            default:
                LOGGER.error("Invalid state: {}", state);
                break;
        }
    }

}
