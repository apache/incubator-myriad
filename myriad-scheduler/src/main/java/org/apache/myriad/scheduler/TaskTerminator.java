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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
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

/**
 * {@link TaskTerminator} is responsible for killing tasks.
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

  @Override
  public void run() {
    // clone a copy of the killable tasks
    Set<TaskID> killableTasks = Sets.newHashSet(schedulerState.getKillableTasks());

    if (CollectionUtils.isEmpty(killableTasks)) {
      return;
    }

    Status driverStatus = driverManager.getDriverStatus();
    if (Status.DRIVER_RUNNING != driverStatus) {
      LOGGER.warn("Cannot kill tasks, as driver is not running. Status: {}", driverStatus);
      return;
    }

    for (TaskID taskIdToKill : killableTasks) {
      if (this.schedulerState.getPendingTaskIds().contains(taskIdToKill)) {
        this.schedulerState.removeTask(taskIdToKill);
      } else {
        Status status = this.driverManager.kill(taskIdToKill);
        NodeTask task = schedulerState.getTask(taskIdToKill);
        if (task != null) {
          offerLifeCycleManager.declineOutstandingOffers(task.getHostname());
          this.schedulerState.removeTask(taskIdToKill);
        } else {
          schedulerState.removeTask(taskIdToKill);
          LOGGER.warn("NodeTask with taskId: {} does not exist", taskIdToKill);
        }
        Preconditions.checkState(status == Status.DRIVER_RUNNING);
      }
    }
  }
}
