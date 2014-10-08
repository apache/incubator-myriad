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
import com.ebay.myriad.configuration.MyriadConfiguration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MesosMasterHealthCheck extends HealthCheck {
    public static final String NAME = "mesos-master";

    private static final Logger LOGGER = Logger
            .getLogger(MesosMasterHealthCheck.class.getName());

    private MyriadConfiguration cfg;

    @Inject
    public MesosMasterHealthCheck(MyriadConfiguration cfg) {
        this.cfg = cfg;
    }

    @Override
    protected Result check() throws Exception {
        String mesosMaster = cfg.getMesosMaster();
        int zkIndex = mesosMaster.indexOf("zk://", 0);
        if (zkIndex >= 0) {
            String zkHostPorts = mesosMaster.substring(5,
                    mesosMaster.indexOf("/", 5));

            String[] hostPorts = zkHostPorts.split(",");

            for (String hostPort : hostPorts) {
                CuratorFramework client = CuratorFrameworkFactory.newClient(
                        hostPort, new ExponentialBackoffRetry(1000, 3));
                client.start();
                client.blockUntilConnected(5, TimeUnit.SECONDS);

                switch (client.getState()) {
                    case STARTED:
                        return Result.healthy();
                    case STOPPED:
                        LOGGER.fine("Unable to reach: " + hostPort);
                    case LATENT:
                        LOGGER.fine("Unable to reach: " + hostPort);
                    default:
                        LOGGER.fine("Unable to reach: " + hostPort);
                }
            }
        } else {
            if (HealthCheckUtils.checkHostPort(mesosMaster)) {
                return Result.healthy();
            }
        }

        return Result.unhealthy("Unable to connect to: " + mesosMaster);
    }
}
