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
package com.ebay.myriad.health;

import com.codahale.metrics.health.HealthCheck;
import com.ebay.myriad.scheduler.MyriadDriverManager;
import org.apache.mesos.Protos.Status;

import javax.inject.Inject;

/**
 * Health Check that Mesos Master is running
 */
public class MesosDriverHealthCheck extends HealthCheck {

    public static final String NAME = "mesos-driver";
    private MyriadDriverManager driverManager;

    @Inject
    public MesosDriverHealthCheck(MyriadDriverManager driverManager) {
        this.driverManager = driverManager;
    }

    @Override
    protected Result check() throws Exception {
        Status driverStatus = driverManager.getDriverStatus();
        if (Status.DRIVER_RUNNING == driverStatus) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Driver status: " + driverStatus);
        }
    }
}
