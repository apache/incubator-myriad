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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for the myriad scheduler driver
 */
public class MyriadDriverManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadDriverManager.class);
  private final Lock driverLock;
  private MyriadDriver driver;
  private Status driverStatus;

  @Inject
  public MyriadDriverManager(MyriadDriver driver) {
    this.driver = driver;
    this.driverLock = new ReentrantLock();
    this.driverStatus = Protos.Status.DRIVER_NOT_STARTED;
  }

  public Status startDriver() {
    this.driverLock.lock();
    try {
      Preconditions.checkState(this.isStartable());
      LOGGER.info("Starting driver...");
      this.driverStatus = driver.start();
      LOGGER.info("Driver started with status: {}", this.driverStatus);
    } finally {
      this.driverLock.unlock();
    }
    return this.driverStatus;
  }


  /**
   * Stop driver, executor, and tasks if false, otherwise just the driver.
   *
   * @return driver status
   */
  public Status stopDriver(boolean failover) {
    this.driverLock.lock();
    try {
      if (isRunning()) {
        if (failover) {
          LOGGER.info("Stopping driver ...");
        } else {
          LOGGER.info("Stopping driver and terminating tasks...");
        }
        this.driverStatus = this.driver.stop(failover);
        LOGGER.info("Stopped driver with status: {}", this.driverStatus);
      }
    } finally {
      this.driverLock.unlock();
    }
    return driverStatus;
  }

  /**
   * Aborting driver without stopping tasks.
   *
   * @return driver status
   */
  public Status abortDriver() {
    this.driverLock.lock();
    try {
      if (isRunning()) {
        LOGGER.info("Aborting driver...");
        this.driverStatus = this.driver.abort();
        LOGGER.info("Aborted driver with status: {}", this.driverStatus);
      }
    } finally {
      this.driverLock.unlock();
    }
    return driverStatus;
  }

  public Status kill(final TaskID taskId) {
    LOGGER.info("Killing task {}", taskId);
    this.driverLock.lock();
    try {
      if (isRunning()) {
        this.driverStatus = driver.kill(taskId);
        LOGGER.info("Task {} killed with status: {}", taskId, this.driverStatus);
      } else {
        LOGGER.warn("Cannot kill task, driver is not running");
      }
    } finally {
      this.driverLock.unlock();
    }

    return driverStatus;
  }

  public Status getDriverStatus() {
    return this.driverStatus;
  }

  private boolean isStartable() {
    return this.driver != null && this.driverStatus == Status.DRIVER_NOT_STARTED;
  }

  private boolean isRunning() {
    return this.driver != null && this.driverStatus == Status.DRIVER_RUNNING;
  }
}
