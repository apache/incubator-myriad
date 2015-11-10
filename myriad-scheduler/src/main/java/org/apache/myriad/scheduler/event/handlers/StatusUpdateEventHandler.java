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
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.myriad.scheduler.event.StatusUpdateEvent;
import org.apache.myriad.scheduler.fgs.OfferLifecycleManager;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles and logs mesos status update events
 */
public class StatusUpdateEventHandler implements EventHandler<StatusUpdateEvent> {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatusUpdateEventHandler.class);

  private final SchedulerState schedulerState;
  private final OfferLifecycleManager offerLifecycleManager;

  @Inject
  public StatusUpdateEventHandler(SchedulerState schedulerState, OfferLifecycleManager offerLifecycleManager) {
    this.schedulerState = schedulerState;
    this.offerLifecycleManager = offerLifecycleManager;
  }

  @Override
  public void onEvent(StatusUpdateEvent event, long sequence, boolean endOfBatch) throws Exception {
    TaskStatus status = event.getStatus();
    this.schedulerState.updateTask(status);
    TaskID taskId = status.getTaskId();
    NodeTask task = schedulerState.getTask(taskId);
    if (task == null) {
      LOGGER.warn("Task: {} not found, status: {}", taskId.getValue(), status.getState());
      schedulerState.removeTask(taskId);
      return;
    }
    LOGGER.info("Status Update for task: {} | state: {}", taskId.getValue(), status.getState());
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
        offerLifecycleManager.declineOutstandingOffers(task.getHostname());
        schedulerState.removeTask(taskId);
        break;
      case TASK_FAILED:
        // Add to pending tasks
        offerLifecycleManager.declineOutstandingOffers(task.getHostname());
        schedulerState.makeTaskPending(taskId);
        break;
      case TASK_KILLED:
        offerLifecycleManager.declineOutstandingOffers(task.getHostname());
        schedulerState.removeTask(taskId);
        break;
      case TASK_LOST:
        offerLifecycleManager.declineOutstandingOffers(task.getHostname());
        schedulerState.makeTaskPending(taskId);
        break;
      default:
        LOGGER.error("Invalid state: {}", state);
        break;
    }
  }
}
