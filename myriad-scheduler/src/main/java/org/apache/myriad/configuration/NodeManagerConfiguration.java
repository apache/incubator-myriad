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
package org.apache.myriad.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * YARN NodeManager Configuration
 */
public class NodeManagerConfiguration {
  /**
   * Allot 10% more memory to account for JVM overhead.
   */
  public static final double JVM_OVERHEAD = 0.1;

  /**
   * Default -Xmx for NodeManager JVM.
   */
  public static final double DEFAULT_JVM_MAX_MEMORY_MB = 2048;

  /**
   * Default CPU cores for NodeManager JVM.
   */
  public static final double DEFAULT_NM_CPUS = 1;

  /**
   * CGroups disabled by default
   */
  public static final Boolean DEFAULT_NM_CGROUPS = false;
  
  /**
   * Default NodeManager Mesos task prefix
   */
  public static final String NM_TASK_PREFIX = "nm";

  /**
   * Translates to -Xmx for the NodeManager JVM.
   */
  @JsonProperty
  private Double jvmMaxMemoryMB;

  /**
   * Amount of CPU share given to NodeManger JVM. This is critical especially
   * for NodeManager auxiliary services.
   */
  @JsonProperty
  private Double cpus;

  /**
   * Translates to JAVA_OPTS for the NodeManager JVM.
   */
  @JsonProperty
  private String jvmOpts;
  
  /**
   * Determines if cgroups are enabled for the NodeManager
   */
  @JsonProperty
  private Boolean cgroups;

  private Double generateNodeManagerMemory() {
    return (NodeManagerConfiguration.DEFAULT_JVM_MAX_MEMORY_MB) * (1 + NodeManagerConfiguration.JVM_OVERHEAD);
  }
  
  public Double getJvmMaxMemoryMB() {
    return Optional.fromNullable(jvmMaxMemoryMB).or(generateNodeManagerMemory());
  }
  
  public Optional<String> getJvmOpts() {
    return Optional.fromNullable(jvmOpts);
  }
  
  public Double getCpus() {
    return Optional.fromNullable(cpus).or(DEFAULT_NM_CPUS);
  }

  public boolean getCgroups() {
    return Optional.fromNullable(cgroups).or(DEFAULT_NM_CGROUPS);
  }
}