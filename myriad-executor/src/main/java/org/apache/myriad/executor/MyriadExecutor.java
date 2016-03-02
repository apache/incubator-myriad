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
package org.apache.myriad.executor;

import java.nio.charset.Charset;
import java.util.Set;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Myriad's Executor
 */
public class MyriadExecutor implements Executor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadExecutor.class);

  private Set<String> containerIds;

  public MyriadExecutor(Set<String> containerTaskIds) {
    this.containerIds = containerTaskIds;
  }

  @Override
  public void registered(ExecutorDriver driver, ExecutorInfo executorInfo, FrameworkInfo frameworkInfo, SlaveInfo slaveInfo) {
    LOGGER.debug("Registered ", executorInfo, " for framework ", frameworkInfo, " on mesos slave ", slaveInfo);
  }

  @Override
  public void reregistered(ExecutorDriver driver, SlaveInfo slaveInfo) {
    LOGGER.debug("ReRegistered");
  }

  @Override
  public void disconnected(ExecutorDriver driver) {
    LOGGER.info("Disconnected");
  }

  @Override
  public void launchTask(final ExecutorDriver driver, final TaskInfo task) {
    LOGGER.debug("launchTask received for taskId: " + task.getTaskId());
    TaskStatus status = TaskStatus.newBuilder().setTaskId(task.getTaskId()).setState(TaskState.TASK_RUNNING).build();
    driver.sendStatusUpdate(status);
  }

  @Override
  public void killTask(ExecutorDriver driver, TaskID taskId) {
    String taskIdString = taskId.toString();
    LOGGER.debug("killTask received for taskId: " + taskIdString);
    TaskStatus status;

    if (!taskIdString.contains(MyriadExecutorAuxService.YARN_CONTAINER_TASK_ID_PREFIX)) {
      // Inform mesos of killing all tasks corresponding to yarn containers that are
      // currently running 
      synchronized (containerIds) {
        for (String containerId : containerIds) {
          Protos.TaskID containerTaskId = Protos.TaskID.newBuilder().setValue(
              MyriadExecutorAuxService.YARN_CONTAINER_TASK_ID_PREFIX + containerId).build();
          status = TaskStatus.newBuilder().setTaskId(containerTaskId).setState(TaskState.TASK_KILLED).build();
          driver.sendStatusUpdate(status);
        }
      }

      // Now kill the node manager task
      status = TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_KILLED).build();
      driver.sendStatusUpdate(status);
      LOGGER.info("NodeManager shutdown after receiving KILL_TASK for taskId {}", taskIdString);
      Runtime.getRuntime().exit(0);

    } else {
      status = TaskStatus.newBuilder().setTaskId(taskId).setState(TaskState.TASK_KILLED).build();
      driver.sendStatusUpdate(status);
      synchronized (containerIds) {
        //Likely the container isn't in here, but just in case remove it.
        if (containerIds.remove(taskIdString.substring(MyriadExecutorAuxService.YARN_CONTAINER_FULL_PREFIX.length(),
            taskIdString.length()))) {
          LOGGER.debug("Removed taskId {} from containerIds", taskIdString);
        }
      }
      LOGGER.debug("Killing " + taskId);
    }
  }

  @Override
  public void frameworkMessage(ExecutorDriver driver, byte[] data) {
    LOGGER.info("Framework message received: ", new String(data, Charset.defaultCharset()));
  }

  @Override
  public void shutdown(ExecutorDriver driver) {
    LOGGER.debug("Shutdown");
  }

  @Override
  public void error(ExecutorDriver driver, String message) {
    LOGGER.error("Error message: " + message);
  }
}
