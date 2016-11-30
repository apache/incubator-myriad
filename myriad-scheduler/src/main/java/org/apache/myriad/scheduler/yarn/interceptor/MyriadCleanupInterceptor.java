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

import org.apache.myriad.Main;
import org.apache.myriad.scheduler.MyriadDriverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for shutdown Myriad by invoking stopDriver upon the 
 * Myriad driver {@link org.apache.myriad.Main}
 */
public class MyriadCleanupInterceptor extends BaseInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadCleanupInterceptor.class);

  private final InterceptorRegistry registry;

  public MyriadCleanupInterceptor(InterceptorRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void cleanup() throws IOException {
    try {
      LOGGER.info("stopping mesosDriver..");
      Main.getInjector().getInstance(MyriadDriverManager.class).stopDriver(false);
      LOGGER.info("stopped mesosDriver..");
    } catch (Exception e) {
      // Abort shutdown RM
      throw new RuntimeException("Failed to stop myriad", e);
    }
    LOGGER.info("Stopped myriad.");
  }
}
