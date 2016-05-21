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

import java.util.HashMap;
import java.util.Map;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation assumes NM binaries already deployed
 */
public class NMExecutorCLGenImpl implements ExecutorCommandLineGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(NMExecutorCLGenImpl.class);

  public static final String ENV_YARN_NODEMANAGER_OPTS = "YARN_NODEMANAGER_OPTS";
  public static final String KEY_YARN_NM_CGROUPS_PATH = "yarn.nodemanager.cgroups.path";
  public static final String KEY_YARN_RM_HOSTNAME = "yarn.resourcemanager.hostname";

  /**
   * YARN class to help handle LCE resources
   */
  // TODO (mohit): Should it be configurable ?
  public static final String KEY_YARN_NM_LCE_CGROUPS_HIERARCHY = "yarn.nodemanager.linux-container-executor.cgroups.hierarchy";
  public static final String KEY_YARN_HOME = "yarn.home";
  public static final String KEY_NM_RESOURCE_CPU_VCORES = "nodemanager.resource.cpu-vcores";
  public static final String KEY_NM_RESOURCE_MEM_MB = "nodemanager.resource.memory-mb";
  public static final String YARN_NM_CMD = " $YARN_HOME/bin/yarn nodemanager";
  public static final String KEY_NM_ADDRESS = "myriad.yarn.nodemanager.address";
  public static final String KEY_NM_LOCALIZER_ADDRESS = "myriad.yarn.nodemanager.localizer.address";
  public static final String KEY_NM_WEBAPP_ADDRESS = "myriad.yarn.nodemanager.webapp.address";
  public static final String KEY_NM_SHUFFLE_PORT = "myriad.mapreduce.shuffle.port";

  private static final String ALL_LOCAL_IPV4ADDR = "0.0.0.0:";
  private static final String PROPERTY_FORMAT = "-D%s=%s";

  private Map<String, String> environment = new HashMap<>();
  protected MyriadConfiguration cfg;
  protected YarnConfiguration conf = new YarnConfiguration();

  public NMExecutorCLGenImpl(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }

  @Override
  public String generateCommandLine(ServiceResourceProfile profile, Ports ports) {
    StringBuilder cmdLine = new StringBuilder();

    generateEnvironment(profile, (NMPorts) ports);
    appendCgroupsCmds(cmdLine);
    appendYarnHomeExport(cmdLine);
    appendEnvForNM(cmdLine);
    cmdLine.append(YARN_NM_CMD);
    return cmdLine.toString();
  }

  protected void generateEnvironment(ServiceResourceProfile profile, NMPorts ports) {
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
      addYarnNodemanagerOpt(KEY_YARN_NM_LCE_CGROUPS_HIERARCHY, "mesos/$TASK_DIR");
      if (environment.containsKey("YARN_HOME")) {
        addYarnNodemanagerOpt(KEY_YARN_HOME, environment.get("YARN_HOME"));
      }
    }
    addYarnNodemanagerOpt(KEY_NM_RESOURCE_CPU_VCORES, Integer.toString(profile.getCpus().intValue()));
    addYarnNodemanagerOpt(KEY_NM_RESOURCE_MEM_MB, Integer.toString(profile.getMemory().intValue()));
    addYarnNodemanagerOpt(KEY_NM_ADDRESS, ALL_LOCAL_IPV4ADDR + Long.valueOf(ports.getRpcPort()).toString());
    addYarnNodemanagerOpt(KEY_NM_LOCALIZER_ADDRESS, ALL_LOCAL_IPV4ADDR + Long.valueOf(ports.getLocalizerPort()).toString());
    addYarnNodemanagerOpt(KEY_NM_WEBAPP_ADDRESS, ALL_LOCAL_IPV4ADDR + Long.valueOf(ports.getWebAppHttpPort()).toString());
    addYarnNodemanagerOpt(KEY_NM_SHUFFLE_PORT, Long.valueOf(ports.getShufflePort()).toString());
  }

  protected void appendEnvForNM(StringBuilder cmdLine) {
    cmdLine.append(" env ");
    for (Map.Entry<String, String> env : environment.entrySet()) {
      cmdLine.append(env.getKey()).append("=").append("\"").append(env.getValue()).append("\" ");
    }
  }

  protected void appendCgroupsCmds(StringBuilder cmdLine) {
    if (cfg.getFrameworkSuperUser().isPresent()) {
      cmdLine.append(" export TASK_DIR=`basename $PWD`&&");
      //The container executor script expects mount-path to exist and owned by the yarn user
      //See: https://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/NodeManagerCgroups.html
      //If YARN ever moves to cgroup/mem it will be necessary to add a mem version.
      appendSudo(cmdLine);
      cmdLine.append("chown " + cfg.getFrameworkUser().get() + " ");
      cmdLine.append(cfg.getCGroupPath());
      cmdLine.append("/cpu/mesos/$TASK_DIR &&");
    } else {
      LOGGER.info("frameworkSuperUser not enabled ignoring cgroup configuration");
    }
  }

  protected void appendYarnHomeExport(StringBuilder cmdLine) {
    if (environment.containsKey("YARN_HOME")) {
      cmdLine.append(" export YARN_HOME=");
      cmdLine.append(environment.get("YARN_HOME"));
      cmdLine.append(";");
    }
  }

  protected void appendSudo(StringBuilder cmdLine) {
    if (cfg.getFrameworkSuperUser().isPresent()) {
      cmdLine.append(" sudo ");
    }
  }

  protected void appendUserSudo(StringBuilder cmdLine) {
    if (cfg.getFrameworkSuperUser().isPresent()) {
      cmdLine.append(" sudo -E -u ");
      cmdLine.append(cfg.getFrameworkUser().get());
      cmdLine.append(" -H ");
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

  @Override
  public String getConfigurationUrl() {
    String httpPolicy = conf.get(TaskFactory.YARN_HTTP_POLICY);
    if (httpPolicy != null && httpPolicy.equals(TaskFactory.YARN_HTTP_POLICY_HTTPS_ONLY)) {
      String address = conf.get(TaskFactory.YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS);
      if (address == null || address.isEmpty()) {
        address = conf.get(TaskFactory.YARN_RESOURCEMANAGER_HOSTNAME) + ":8090";
      }
      return "https://" + address + "/conf";
    } else {
      String address = conf.get(TaskFactory.YARN_RESOURCEMANAGER_WEBAPP_ADDRESS);
      if (address == null || address.isEmpty()) {
        address = conf.get(TaskFactory.YARN_RESOURCEMANAGER_HOSTNAME) + ":8088";
      }
      return "http://" + address + "/conf";
    }
  }
}
