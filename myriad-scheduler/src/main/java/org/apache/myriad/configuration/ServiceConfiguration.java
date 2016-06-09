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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myriad.configuration;

import java.util.Map;
import java.util.TreeMap;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * Configuration for any service/task to be started from Myriad Scheduler
 */
public class ServiceConfiguration {
  /**
   * Translates to -Xmx for the Mesos executor JVM.
   * Default number of CPU cores per Mesos executor JVM.
   */
  public static final Double DEFAULT_CPU_CORES = 0.1;

  /**
   * Default amount of RAM per Mesos executor JVM.
   */
  public static final Double DEFAULT_JVM_MAX_MEMORY_MB  = 256.0;
  
  /**
   * Translates to -Xmx for the Mesos executor JVM.
   * Allot 10% more memory to account for JVM overhead.
   */
  public static final double JVM_OVERHEAD = 0.1;
  
  /**
   * Translates to -Xmx for the Mesos executor JVM.
   */
  @JsonProperty
  protected Double jvmMaxMemoryMB;

  /**
   * Amount of CPU share given to Mesos executor JVM.
   */
  @JsonProperty
  protected Double cpus;

  /**
   * Translates to JVM opts for the Mesos executor JVM.
   */
  @JsonProperty
  protected String jvmOpts;

  @JsonProperty
  protected Map<String, Long> ports;

  /**
   * If we will have some services
   * that are not easy to express just by properties
   * we can use this one to have a specific implementation
   */
  @JsonProperty
  protected String taskFactoryImplName;

  @JsonProperty
  protected String envSettings;

  @JsonProperty
  @NotEmpty
  protected String taskName;

  @JsonProperty
  protected Integer maxInstances;

  @JsonProperty
  @NotEmpty
  protected String command;

  @JsonProperty
  protected String serviceOptsName;

  private Double generateMaxMemory() {
    return (DEFAULT_JVM_MAX_MEMORY_MB) * (1 + JVM_OVERHEAD);
  }

  public Double getJvmMaxMemoryMB() {
    return Optional.fromNullable(jvmMaxMemoryMB).or(generateMaxMemory());
  }

  public Optional<String> getJvmOpts() {
    return Optional.fromNullable(jvmOpts);
  }

  public Double getCpus() {
    return Optional.fromNullable(cpus).or(DEFAULT_CPU_CORES);
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public Optional<String> getTaskFactoryImplName() {
    return Optional.fromNullable(taskFactoryImplName);
  }

  public String getEnvSettings() {
    return envSettings;
  }

  public Map<String, Long> getPorts() {
    return Optional.fromNullable(ports).or(new TreeMap<String, Long>());
  }

  public Optional<Integer> getMaxInstances() {
    return Optional.fromNullable(maxInstances);
  }

  public Optional<String> getCommand() {
    return Optional.fromNullable(command);
  }

  public Optional<String> getServiceOpts() {
    return Optional.fromNullable(serviceOptsName);
  }
}
