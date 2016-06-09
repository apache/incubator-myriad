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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NMProfile Manager
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

  public int numberOfProfiles() {
    return profiles.size();
  }
  
  @Override
  public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);

    for (Map.Entry<String, NMProfile> profile : profiles.entrySet()) {
      NMProfile value = profile.getValue();
      builder.append("name", value.getName());
      builder.append("cpus", value.getCpus());
      builder.append("memory", value.getMemory());
    }

    return builder.toString();
  }
}