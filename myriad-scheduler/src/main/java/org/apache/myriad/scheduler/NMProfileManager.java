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

import com.google.gson.Gson;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Manager Profile Manager
 */
public class NMProfileManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(NMProfileManager.class);

  private Map<String, NMProfile> profiles = new ConcurrentHashMap<>();

  public NMProfile get(String name) {
    return profiles.get(name);
  }

  public void add(NMProfile profile) {
    LOGGER.info("Adding profile {} with CPU: {} and Memory: {}", profile.getName(), profile.getCpus(), profile.getMemory());

    profiles.put(profile.getName(), profile);
  }

  public boolean exists(String name) {
    return this.profiles.containsKey(name);
  }

  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
