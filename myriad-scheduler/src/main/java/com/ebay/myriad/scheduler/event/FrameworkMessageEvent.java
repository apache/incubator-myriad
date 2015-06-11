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
package com.ebay.myriad.scheduler.event;

import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

/**
 * framework message event
 */
public class FrameworkMessageEvent {
    private SchedulerDriver driver;
    private Protos.ExecutorID executorId;
    private Protos.SlaveID slaveId;
    private byte[] bytes;

    public SchedulerDriver getDriver() {
        return driver;
    }

    public void setDriver(SchedulerDriver driver) {
        this.driver = driver;
    }

    public Protos.ExecutorID getExecutorId() {
        return executorId;
    }

    public void setExecutorId(Protos.ExecutorID executorId) {
        this.executorId = executorId;
    }

    public Protos.SlaveID getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Protos.SlaveID slaveId) {
        this.slaveId = slaveId;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}
