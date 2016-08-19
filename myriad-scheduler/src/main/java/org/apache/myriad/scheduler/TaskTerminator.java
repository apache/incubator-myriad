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
package org.apache.myriad.scheduler;

import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.scheduler.fgs.OfferLifecycleManager;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * {@link TaskTerminator} is basically a reaper process responsible for killing
 * tasks marked as Killable by {@link MyriadOperations} that are stored
 * within a {@link SchedulerState} object
 */
public class TaskTerminator implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskTerminator.class);

  private final SchedulerState schedulerState;
  private final MyriadDriverManager driverManager;
  private final OfferLifecycleManager offerLifeCycleManager;

  @Inject
  public TaskTerminator(SchedulerState schedulerState, MyriadDriverManager driverManager,
                        OfferLifecycleManager offerLifecycleManager) {
    this.schedulerState = schedulerState;
    this.driverManager = driverManager;
    this.offerLifeCycleManager = offerLifecycleManager;
  }

  /**
   * Encapsulates logic that retrieves the collection of killable tasks from the
   * SchedulerState object. If a task is in pending state, the task is simply 
   * removed from SchedulerState. Any tasks in a running state were not successfully
   * killed by Mesos or the callback failed, so the another kill attempt is made.
   */
  @Override
  public void run() { 
    //If there are 1..n killable tasks, proceed; otherwise, simply return
    if (CollectionUtils.isNotEmpty(schedulerState.getKillableTaskIds())) {
      /*
       * Clone the killable task collection, iterate through all tasks, and 
       * process any pending and/or non-pending tasks
       */
      Set<TaskID> killableTasks = Sets.newHashSet(schedulerState.getKillableTaskIds());
      Status driverStatus = driverManager.getDriverStatus();

      //TODO (hokiegeek2) Can the DriverManager be restarted? If not, should the ResourceManager stop?
      if (Status.DRIVER_RUNNING != driverStatus) {
        LOGGER.warn("Cannot kill tasks because Mesos Driver is not running. Status: {}", driverStatus);
        return;
      }

      for (TaskID taskIdToKill : killableTasks) {
        LOGGER.info("Received task kill request for task: {}", taskIdToKill);
        if (isPendingTask(taskIdToKill)) {
          handlePendingTask(taskIdToKill);
        } else {
          handleNonPendingTask(taskIdToKill);
        }
      }
    }
  }
  
  private void handlePendingTask(TaskID taskId) {
    /*
     * since task is pending and has not started, simply remove 
     * it from SchedulerState task collection
     */
    schedulerState.removeTask(taskId);
  }
  
  private void handleNonPendingTask(TaskID taskId) { 
    /*
     * Kill the task and decline additional offers for it, but hold off removing from SchedulerState. 
     * Removal of the killable task must be done following invocation of statusUpdate callback method
     * which constitutes acknowledgement from Mesos that the kill task request succeeded.
     */
    Status status = this.driverManager.kill(taskId);
    NodeTask task = schedulerState.getTask(taskId);

    if (task != null) {
      offerLifeCycleManager.declineOutstandingOffers(task.getHostname());
    } 
    if (status.equals(Status.DRIVER_RUNNING)) {
      LOGGER.info("Kill request for {} was submitted to a running SchedulerDriver", taskId);
    } else {
      LOGGER.warn("Kill task request for {} submitted to non-running SchedulerDriver, may fail", taskId);
    }
  }

  private boolean isPendingTask(TaskID taskId) {
    return this.schedulerState.getPendingTaskIds().contains(taskId);
  }
}