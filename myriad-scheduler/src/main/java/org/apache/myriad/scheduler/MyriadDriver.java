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

import javax.inject.Inject;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver for Myriad scheduler.
 */
public class MyriadDriver {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadDriver.class);

  private final SchedulerDriver driver;

  @Inject
  public MyriadDriver(SchedulerDriver driver) {
    this.driver = driver;
  }

  public Status stop(boolean failover) {
    LOGGER.info("Stopping driver");
    Status status = driver.stop(failover);
    LOGGER.info("Driver stopped with status: {}", status);
    return status;
  }

  public Status start() {
    LOGGER.info("Starting driver");
    Status status = driver.start();
    LOGGER.info("Driver started with status: {}", status);
    return status;
  }

  public Status kill(final TaskID taskId) {
    Status status = driver.killTask(taskId);
    LOGGER.info("Task {} killed with status: {}", taskId, status);
    return status;
  }

  public Status abort() {
    LOGGER.info("Aborting driver");
    Status status = driver.abort();
    LOGGER.info("Driver aborted with status: {}", status);
    return status;
  }

  public SchedulerDriver getDriver() {
    return driver;
  }
}
