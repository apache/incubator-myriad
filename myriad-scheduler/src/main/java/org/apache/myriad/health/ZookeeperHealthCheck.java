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
import javax.inject.Inject;
import org.apache.myriad.configuration.MyriadConfiguration;

/**
 * Health Check on ZK
 */
public class ZookeeperHealthCheck extends HealthCheck {
  public static final String NAME = "zookeeper";
  private MyriadConfiguration cfg;

  @Inject
  public ZookeeperHealthCheck(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }

  @Override
  protected Result check() throws Exception {
    // todo:  (kensipe) this needs to be implemented
    return Result.healthy();
  }
}
