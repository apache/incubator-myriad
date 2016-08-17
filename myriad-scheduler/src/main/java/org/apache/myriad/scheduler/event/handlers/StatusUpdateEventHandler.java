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
 * Handles and logs mesos StatusUpdateEvents based upon the corresponding
 * Protos.TaskState enum value
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
  
  /**
   * Encapsulates the logic to log and respond to the incoming StatusUpdateEvent per the
   * Event TaskStatus state:
   * 
   * 1. TASK_STAGING: mark task as staging wtihin SchedulerState
   * 2. TASK_STARTING: mark task as staging within SchedulerState
   * 3. TASK_RUNNING: mark task as active within SchedulerState
   * 4. TASK_FINISHED: decline outstanding offers and remove task from SchedulerState
   * 5. TASK_FAILED: decline outstanding offers, remove failed, killable tasks from SchedulerState,
   *    mark as pending non-killable, failed tasks
   * 6. TASK_KILLED: decline outstanding offers, removed killed tasks from SchedulerState
   * 7. TASK_LOST: decline outstanding offers, remove killable, lost tasks from SchedulerState,
   *    mark as pending non-killable, lost tasks
   */
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
        cleanupTask(taskId, task, "finished");
        break;
      case TASK_FAILED:
        cleanupFailedTask(taskId, task, "failed");
        break;
      case TASK_KILLED:
        cleanupTask(taskId, task, "killed");
        break;
      case TASK_LOST:
        cleanupFailedTask(taskId, task, "lost");
        break;
      default:
        LOGGER.error("Invalid state: {}", state);
        break;
    }
  }

  private void cleanupFailedTask(TaskID taskId, NodeTask task, String stopReason) {
    offerLifecycleManager.declineOutstandingOffers(task.getHostname());
    /*
     * Remove the task from SchedulerState if the task is killable.  Otherwise,
     * mark the task as pending to enable restart.
     */
    if (taskIsKillable(taskId)) {
      schedulerState.removeTask(taskId);
      LOGGER.info("Removed killable, {} task with id {}", stopReason, taskId);
    } else {
      schedulerState.makeTaskPending(taskId);        
      LOGGER.info("Marked as pending {} task with id {}", stopReason, taskId);
    }  
  }
  
  private void cleanupTask(TaskID taskId, NodeTask task, String stopReason) {
    offerLifecycleManager.declineOutstandingOffers(task.getHostname());
    schedulerState.removeTask(taskId);    
    LOGGER.info("Removed {} task with id {}", stopReason, taskId);
  }
  private boolean taskIsKillable(TaskID taskId) {
    return schedulerState.getKillableTaskIds().contains(taskId);
  }
}