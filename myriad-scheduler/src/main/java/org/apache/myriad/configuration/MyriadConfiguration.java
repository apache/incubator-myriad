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

import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * Myriad Configuration commonly defined in the YML file
 * <pre>
 * mesosMaster: 10.0.2.15:5050
 * checkpoint: false
 * frameworkFailoverTimeout: 43200000
 * frameworkName: MyriadAlpha
 * nativeLibrary: /usr/local/lib/libmesos.so
 * zkServers: localhost:2181
 * zkTimeout: 20000
 * profiles:
 * small:
 * cpu: 1
 * mem: 1100
 * medium:
 * cpu: 2
 * mem: 2048
 * large:
 * cpu: 4
 * mem: 4096
 * rebalancer: false
 * nodemanager:
 * jvmMaxMemoryMB: 1024
 * user: hduser
 * cpus: 0.2
 * cgroupPath: /sys/fs/cgroup
 * executor:
 * jvmMaxMemoryMB: 256
 * path: file://localhost/usr/local/libexec/mesos/myriad-executor-runnable-0.1.0.jar
 * containerInfo:
 *   DockerInfo:
 *     image: mesos/myriad
 *     forcePullImage: false
 *     parameters:
 *       -
 *         key: key
 *         value: value
 *       -
 *         key: key
 *         value: value
 *   volumes:
 *     -
 *       containerPath: path
 *       hostPath: path
 *       mode: RW
 *     -
 *       containerPath: path
 *       hostPath: path
 * yarnEnvironment:
 *   YARN_HOME: /usr/local/hadoop
 * </pre>
 */
public class MyriadConfiguration {
  /**
   * By default framework checkpointing is turned off.
   */
  public static final Boolean DEFAULT_CHECKPOINT = false;

  /**
   * By default rebalancer is turned off.
   */
  public static final Boolean DEFAULT_REBALANCER_ENABLED = false;

  /**
   * By default ha is turned off.
   */
  public static final Boolean DEFAULT_HA_ENABLED = false;

  /**
   * By default framework failover timeout is 1 day.
   */
  public static final Double DEFAULT_FRAMEWORK_FAILOVER_TIMEOUT_MS = 86400000.0;

  public static final String DEFAULT_FRAMEWORK_NAME = "myriad-scheduler";

  public static final String DEFAULT_NATIVE_LIBRARY = "/usr/local/lib/libmesos.so";

  public static final Integer DEFAULT_ZK_TIMEOUT = 20000;

  public static final Integer DEFAULT_REST_API_PORT = 8192;
  
  public static final String DEFAULT_ROLE = "*";
  
  public static final String DEFAULT_ZK_SERVERS = "localhost:2181";
  
  public static final String DEFAULT_CGROUP_PATH = "/sys/fs/cgroup";

  /**
   * By default ha is turned off.
   */
  public static final Boolean DEFAULT_CGROUPS_ENABLED = false;

  public static final Map<String, ServiceConfiguration> EMPTY_SERVICE_CONFIGURATION = Collections.emptyMap();

  @JsonProperty
  @NotEmpty
  private String mesosMaster;

  @JsonProperty
  private Boolean checkpoint;

  @JsonProperty
  private MyriadContainerConfiguration containerInfo;

  @JsonProperty
  private Double frameworkFailoverTimeout;

  @JsonProperty
  private String frameworkName;

  @JsonProperty
  private String frameworkRole;

  @JsonProperty
  private String frameworkUser;

  @JsonProperty
  private String frameworkSuperUser;

  @JsonProperty
  @NotEmpty
  private Map<String, Map<String, String>> profiles;

  @JsonProperty
  @NotEmpty
  private Map<String, Integer> nmInstances;

  @JsonProperty
  private Boolean rebalancer;

  @JsonProperty
  private Boolean haEnabled;

  @JsonProperty
  private NodeManagerConfiguration nodemanager;

  @JsonProperty
  private Map<String, ServiceConfiguration> services;

  @JsonProperty
  @NotEmpty
  private MyriadExecutorConfiguration executor;

  @JsonProperty
  private String nativeLibrary;

  @JsonProperty
  @NotEmpty
  private String zkServers;

  @JsonProperty
  private Integer zkTimeout;

  @JsonProperty
  private Integer restApiPort;

  @JsonProperty
  @NotEmpty
  private Map<String, String> yarnEnvironment;

  @JsonProperty
  private String mesosAuthenticationPrincipal;

  @JsonProperty
  private String mesosAuthenticationSecretFilename;

  @JsonProperty
  private String servedConfigPath;

  @JsonProperty
  private String servedBinaryPath;

  @JsonProperty
  private String cgroupPath;

  @JsonProperty
  private Boolean cgroupEnabled;

  public MyriadConfiguration() {
  }

  public String getMesosMaster() {
    return mesosMaster;
  }

  public Boolean isCheckpoint() {
    return Optional.fromNullable(checkpoint).or(DEFAULT_CHECKPOINT);
  }

  public Optional<MyriadContainerConfiguration> getContainerInfo() {
    return Optional.fromNullable(containerInfo);
  }

  public String getFrameworkRole() {
    return Optional.fromNullable(frameworkRole).or(DEFAULT_ROLE);
  }

  public String getFrameworkName() {
    return Optional.fromNullable(frameworkName).or(DEFAULT_FRAMEWORK_NAME);
  }
  
  public Double getFrameworkFailoverTimeout() {
    return Optional.fromNullable(frameworkFailoverTimeout).or(DEFAULT_FRAMEWORK_FAILOVER_TIMEOUT_MS);
  }

  public Optional<String> getFrameworkUser() {
    return Optional.fromNullable(frameworkUser);
  }

  public Optional<String> getFrameworkSuperUser() {
    return Optional.fromNullable(frameworkSuperUser);
  }

  public Map<String, Map<String, String>> getProfiles() {
    return profiles;
  }

  public Map<String, Integer> getNmInstances() {
    return nmInstances;
  }

  public Boolean isRebalancerEnabled() {
    return Optional.fromNullable(rebalancer).or(DEFAULT_REBALANCER_ENABLED);
  }

  public Boolean isHAEnabled() {
    return Optional.fromNullable(haEnabled).or(DEFAULT_HA_ENABLED);
  }

  public NodeManagerConfiguration getNodeManagerConfiguration() {
    return nodemanager;
  }

  public Map<String, ServiceConfiguration> getServiceConfigurations() {
    return Optional.fromNullable(services).or(EMPTY_SERVICE_CONFIGURATION);
  }

  public Optional<ServiceConfiguration> getServiceConfiguration(String taskName) {
    return Optional.fromNullable(services.get(taskName));
  }

  public MyriadExecutorConfiguration getMyriadExecutorConfiguration() {
    return this.executor;
  }

  public String getNativeLibrary() {
    return Optional.fromNullable(nativeLibrary).or(DEFAULT_NATIVE_LIBRARY);
  }

  public String getZkServers() {
    return Optional.fromNullable(zkServers).or(DEFAULT_ZK_SERVERS);
  }

  public Integer getZkTimeout() {
    return Optional.fromNullable(zkTimeout).or(DEFAULT_ZK_TIMEOUT);
  }

  public Integer getRestApiPort() {
    return Optional.fromNullable(restApiPort).or(DEFAULT_REST_API_PORT);
  }

  public Map<String, String> getYarnEnvironment() {
    return yarnEnvironment;
  }

  public String getMesosAuthenticationSecretFilename() {
    return mesosAuthenticationSecretFilename;
  }

  public String getMesosAuthenticationPrincipal() {
    return mesosAuthenticationPrincipal;
  }

  public Optional<String> getServedConfigPath() {
    return Optional.fromNullable(servedConfigPath);
  }

  public Optional<String> getServedBinaryPath() {
    return Optional.fromNullable(servedBinaryPath);
  }

  public String getCGroupPath() {
    return Optional.fromNullable(cgroupPath).or(DEFAULT_CGROUP_PATH);
  }

  public Boolean isCgroupEnabled() {
    return Optional.fromNullable(cgroupEnabled).or(DEFAULT_CGROUPS_ENABLED);
  }
}
