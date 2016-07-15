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
package org.apache.myriad.scheduler.yarn.interceptor;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.myriad.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for initializing Myriad by invoking initialize upon the 
 * Myriad driver {@link org.apache.myriad.Main}
 */
public class MyriadInitializationInterceptor extends BaseInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadInitializationInterceptor.class);

  private final InterceptorRegistry registry;

  public MyriadInitializationInterceptor(InterceptorRegistry registry) {
    this.registry = registry;
  }

  /**
   * Initialize Myriad plugin before RM's scheduler is initialized.
   * This includes registration with Mesos master, initialization of
   * the myriad web application, initializing guice modules etc.
   */
  @Override
  public void init(Configuration conf, AbstractYarnScheduler yarnScheduler, RMContext rmContext) throws IOException {
    try {
      Main.initialize(conf, yarnScheduler, rmContext, registry);
    } catch (Exception e) {
      // Abort bringing up RM
      throw new RuntimeException("Failed to initialize myriad", e);
    }
    LOGGER.info("Initialized myriad.");
  }
}
