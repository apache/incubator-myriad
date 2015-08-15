/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ebay.myriad.executor;

import java.nio.ByteBuffer;

import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.server.api.ApplicationInitializationContext;
import org.apache.hadoop.yarn.server.api.ApplicationTerminationContext;
import org.apache.hadoop.yarn.server.api.AuxiliaryService;
import org.apache.hadoop.yarn.server.api.ContainerTerminationContext;

import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.Protos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auxillary service wrapper for MyriadExecutor 
 */
public class MyriadExecutorAuxService  extends AuxiliaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadExecutor.class);
  private static final String SERVICE_NAME = "myriad_service";
  public static final String YARN_CONTAINER_TASK_ID_PREFIX = "yarn_";

  private MesosExecutorDriver driver;

  protected MyriadExecutorAuxService() {
    super(SERVICE_NAME);
  }

  @Override
  protected void serviceStart() throws Exception {
    LOGGER.info("Starting MyriadExecutor...");

    new Thread(new Runnable() {
      public void run() {
        driver = new MesosExecutorDriver(new MyriadExecutor());
        LOGGER.error("MyriadExecutor exit with status " +
        Integer.toString(driver.run() == Status.DRIVER_STOPPED ? 0 : 1));
      }
    }).start();
  }

  @Override
  public void initializeApplication(
    ApplicationInitializationContext initAppContext) {
    LOGGER.debug("initializeApplication");
  }

  @Override
  public void stopApplication(ApplicationTerminationContext stopAppContext) {
    LOGGER.debug("stopApplication");
  }

  @Override
  public ByteBuffer getMetaData() {
    LOGGER.debug("getMetaData");
    return null;
  }

  @Override
  public void stopContainer(ContainerTerminationContext stopContainerContext) {
    sendStatus(stopContainerContext.getContainerId(), TaskState.TASK_FINISHED);
  }

  private void sendStatus(ContainerId containerId, TaskState taskState) {
    Protos.TaskID taskId = Protos.TaskID.newBuilder()
      .setValue(YARN_CONTAINER_TASK_ID_PREFIX + containerId.toString())
      .build();

    TaskStatus status = TaskStatus.newBuilder()
      .setTaskId(taskId)
      .setState(taskState)
      .build();
    driver.sendStatusUpdate(status);
    LOGGER.debug("Sent status " + taskState + " for taskId " + taskId);
  }

}
