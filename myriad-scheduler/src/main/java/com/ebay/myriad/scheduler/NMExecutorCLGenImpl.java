/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebay.myriad.scheduler;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.myriad.configuration.MyriadConfiguration;

/**
 * Implementation assumes NM binaries already deployed 
 */
public class NMExecutorCLGenImpl implements ExecutorCommandLineGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(NMExecutorCLGenImpl.class);

  public static final String ENV_YARN_NODEMANAGER_OPTS =
    "YARN_NODEMANAGER_OPTS";
  public static final String KEY_YARN_NM_CGROUPS_PATH =
    "yarn.nodemanager.cgroups.path";
  public static final String KEY_YARN_RM_HOSTNAME =
    "yarn.resourcemanager.hostname";

  /**
   * YARN container executor class.
   */
  public static final String KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS =
    "yarn.nodemanager.container-executor.class";
  // TODO (mohit): Should it be configurable ?
  public static final String VAL_YARN_NM_CONTAINER_EXECUTOR_CLASS =
    "org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor";
  public static final String DEFAULT_YARN_NM_CONTAINER_EXECUTOR_CLASS =
    "org.apache.hadoop.yarn.server.nodemanager.DefaultContainerExecutor";

  /**
   * YARN class to help handle LCE resources
   */
  public static final String KEY_YARN_NM_LCE_RH_CLASS =
    "yarn.nodemanager.linux-container-executor.resources-handler.class";

  // TODO (mohit): Should it be configurable ?
  public static final String VAL_YARN_NM_LCE_RH_CLASS =
    "org.apache.hadoop.yarn.server.nodemanager.util.CgroupsLCEResourcesHandler";
  public static final String KEY_YARN_NM_LCE_CGROUPS_HIERARCHY =
    "yarn.nodemanager.linux-container-executor.cgroups.hierarchy";
  public static final String VAL_YARN_NM_LCE_CGROUPS_HIERARCHY =
    "mesos/$TASK_DIR";
  public static final String KEY_YARN_NM_LCE_CGROUPS_MOUNT =
    "yarn.nodemanager.linux-container-executor.cgroups.mount";
  public static final String KEY_YARN_NM_LCE_CGROUPS_MOUNT_PATH =
    "yarn.nodemanager.linux-container-executor.cgroups.mount-path";
  public static final String VAL_YARN_NM_LCE_CGROUPS_MOUNT_PATH = "/sys/fs/cgroup";
  public static final String KEY_YARN_NM_LCE_GROUP =
    "yarn.nodemanager.linux-container-executor.group";
  public static final String KEY_YARN_NM_LCE_PATH =
    "yarn.nodemanager.linux-container-executor.path";
  public static final String KEY_YARN_HOME = "yarn.home";
  public static final String KEY_NM_RESOURCE_CPU_VCORES =
    "nodemanager.resource.cpu-vcores";
  public static final String KEY_NM_RESOURCE_MEM_MB =
    "nodemanager.resource.memory-mb";
  public static final String YARN_NM_CMD =
      " $YARN_HOME/bin/yarn nodemanager";
  public static final String KEY_NM_ADDRESS = "myriad.yarn.nodemanager.address";
  public static final String KEY_NM_LOCALIZER_ADDRESS =
    "myriad.yarn.nodemanager.localizer.address";
  public static final String KEY_NM_WEBAPP_ADDRESS =
    "myriad.yarn.nodemanager.webapp.address";
  public static final String KEY_NM_SHUFFLE_PORT =
    "myriad.mapreduce.shuffle.port";

  private static final String ALL_LOCAL_IPV4ADDR =  "0.0.0.0:";
  private static final String PROPERTY_FORMAT = "-D%s=%s";

  private Map<String, String> environment = new HashMap<>();
  protected MyriadConfiguration cfg;

  public NMExecutorCLGenImpl(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }

  @Override
  public String generateCommandLine(NMProfile profile, NMPorts ports) {
    StringBuilder cmdLine = new StringBuilder();

    generateEnvironment(profile, ports);
    appendCgroupsCmds(cmdLine);
    appendYarnHomeExport(cmdLine);
    appendEnvForNM(cmdLine);
    cmdLine.append(YARN_NM_CMD);
    return cmdLine.toString();
  }

  protected void generateEnvironment(NMProfile profile, NMPorts ports) {
    //yarnEnvironemnt configuration from yaml file
    Map<String, String> yarnEnvironmentMap = cfg.getYarnEnvironment();
    if (yarnEnvironmentMap != null) {
      environment.putAll(yarnEnvironmentMap);
    }

    String rmHostName = System.getProperty(KEY_YARN_RM_HOSTNAME);
    if (rmHostName != null && !rmHostName.isEmpty()) {
      addYarnNodemanagerOpt(KEY_YARN_RM_HOSTNAME, rmHostName);
    }

    if (cfg.getNodeManagerConfiguration().getCgroups().or(Boolean.FALSE)) {
      addYarnNodemanagerOpt(KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS,
          VAL_YARN_NM_CONTAINER_EXECUTOR_CLASS);
      addYarnNodemanagerOpt(KEY_YARN_NM_LCE_RH_CLASS, VAL_YARN_NM_LCE_RH_CLASS);

        // TODO: Configure hierarchy
        addYarnNodemanagerOpt(KEY_YARN_NM_LCE_CGROUPS_HIERARCHY,
          VAL_YARN_NM_LCE_CGROUPS_HIERARCHY);
        addYarnNodemanagerOpt(KEY_YARN_NM_LCE_CGROUPS_MOUNT, "true");
        // TODO: Make it configurable
        addYarnNodemanagerOpt(KEY_YARN_NM_LCE_CGROUPS_MOUNT_PATH,
          VAL_YARN_NM_LCE_CGROUPS_MOUNT_PATH);
        addYarnNodemanagerOpt(KEY_YARN_NM_LCE_GROUP, "root");
        if (environment.containsKey("YARN_HOME")) {
          addYarnNodemanagerOpt(KEY_YARN_HOME, environment.get("YARN_HOME"));
        }
    } else {
        // Otherwise configure to use Default
      addYarnNodemanagerOpt(KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS,
          DEFAULT_YARN_NM_CONTAINER_EXECUTOR_CLASS);
    }
    addYarnNodemanagerOpt(KEY_NM_RESOURCE_CPU_VCORES,
      Integer.toString(profile.getCpus().intValue()));
    addYarnNodemanagerOpt(KEY_NM_RESOURCE_MEM_MB,
      Integer.toString(profile.getMemory().intValue()));
    addYarnNodemanagerOpt(KEY_NM_ADDRESS, ALL_LOCAL_IPV4ADDR +
      Long.valueOf(ports.getRpcPort()).toString());
    addYarnNodemanagerOpt(KEY_NM_LOCALIZER_ADDRESS,
      ALL_LOCAL_IPV4ADDR + Long.valueOf(ports.getLocalizerPort()).toString()); 
    addYarnNodemanagerOpt(KEY_NM_WEBAPP_ADDRESS,
      ALL_LOCAL_IPV4ADDR + Long.valueOf(ports.getWebAppHttpPort()).toString());
    addYarnNodemanagerOpt(KEY_NM_SHUFFLE_PORT,
      Long.valueOf(ports.getShufflePort()).toString());
  }

  protected void appendEnvForNM(StringBuilder cmdLine) {
    cmdLine.append(" env ");
    for (Map.Entry<String, String> env : environment.entrySet()) {
      cmdLine.append(env.getKey()).append("=").append("\"")
          .append(env.getValue()).append("\" ");
    }
  }

  protected void appendCgroupsCmds(StringBuilder cmdLine) { 
    if (cfg.getNodeManagerConfiguration().getCgroups().or(Boolean.FALSE)) {
      cmdLine.append(" export TASK_DIR=`basename $PWD`;");
      cmdLine.append(" chmod +x /sys/fs/cgroup/cpu/mesos/$TASK_DIR;");
    }
  }

  protected void appendYarnHomeExport(StringBuilder cmdLine) {
    if (environment.containsKey("YARN_HOME")) {
      cmdLine.append(" export YARN_HOME=" + environment.get("YARN_HOME") + ";");
    }
  }

  protected void addYarnNodemanagerOpt(String propertyName, String propertyValue) {
    String envOpt = String.format(PROPERTY_FORMAT, propertyName, propertyValue);
      if (environment.containsKey(ENV_YARN_NODEMANAGER_OPTS)) {
          String existingOpts = environment.get(ENV_YARN_NODEMANAGER_OPTS);
          environment.put(ENV_YARN_NODEMANAGER_OPTS, existingOpts + " " + envOpt);
      } else {
          environment.put(ENV_YARN_NODEMANAGER_OPTS, envOpt);
      }
  }

}
