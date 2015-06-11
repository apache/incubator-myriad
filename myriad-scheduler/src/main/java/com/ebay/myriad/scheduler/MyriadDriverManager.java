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
package com.ebay.myriad.scheduler;

import com.google.common.base.Preconditions;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    public Status stopDriver() {
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
                LOGGER.info("Task {} killed with status: {}", taskId,
                        this.driverStatus);
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
        return this.driver != null
                && this.driverStatus == Status.DRIVER_NOT_STARTED;
    }

    private boolean isRunning() {
        return this.driver != null
                && this.driverStatus == Status.DRIVER_RUNNING;
    }
}
