/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.apache.myriad.scheduler.event;

import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;

/**
 * mesos slave lost event
 */
public class SlaveLostEvent {
    private SchedulerDriver driver;
    private Protos.SlaveID slaveId;

    public SchedulerDriver getDriver() {
        return driver;
    }

    public void setDriver(SchedulerDriver driver) {
        this.driver = driver;
    }

    public Protos.SlaveID getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Protos.SlaveID slaveId) {
        this.slaveId = slaveId;
    }
}
