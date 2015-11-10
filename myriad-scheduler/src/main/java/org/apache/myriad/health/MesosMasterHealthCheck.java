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
package org.apache.myriad.health;

import com.codahale.metrics.health.HealthCheck;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health check for Mesos master
 */
public class MesosMasterHealthCheck extends HealthCheck {
  public static final String NAME = "mesos-master";

  private static final Logger LOGGER = LoggerFactory.getLogger(MesosMasterHealthCheck.class);

  private MyriadConfiguration cfg;

  @Inject
  public MesosMasterHealthCheck(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }

  @Override
  protected Result check() throws Exception {
    String mesosMaster = cfg.getMesosMaster();
    int zkIndex = mesosMaster.indexOf("zk://", 0);
    Result result = Result.unhealthy("Unable to connect to: " + mesosMaster);
    if (zkIndex >= 0) {
      final int fromIndex = 5;
      String zkHostPorts = mesosMaster.substring(fromIndex, mesosMaster.indexOf("/", fromIndex));

      String[] hostPorts = zkHostPorts.split(",");

      for (String hostPort : hostPorts) {
        final int maxRetries = 3;
        final int baseSleepTimeMs = 1000;
        CuratorFramework client = CuratorFrameworkFactory.newClient(hostPort, new ExponentialBackoffRetry(baseSleepTimeMs,
            maxRetries));
        client.start();
        final int blockTime = 5;
        client.blockUntilConnected(blockTime, TimeUnit.SECONDS);

        switch (client.getState()) {
          case STARTED:
            result = Result.healthy();
            break;
          case STOPPED:
            LOGGER.info("Unable to reach: ", hostPort);
            break;
          case LATENT:
            LOGGER.info("Unable to reach: ", hostPort);
            break;
          default:
            LOGGER.info("Unable to reach: ", hostPort);
        }
      }
    } else {
      if (HealthCheckUtils.checkHostPort(mesosMaster)) {
        result = Result.healthy();
      }
    }

    return result;
  }
}
