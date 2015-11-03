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
package org.apache.myriad;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.MapUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.health.MesosDriverHealthCheck;
import org.apache.myriad.health.MesosMasterHealthCheck;
import org.apache.myriad.health.ZookeeperHealthCheck;
import org.apache.myriad.scheduler.ExtendedResourceProfile;
import org.apache.myriad.scheduler.MyriadDriverManager;
import org.apache.myriad.scheduler.MyriadOperations;
import org.apache.myriad.scheduler.NMProfile;
import org.apache.myriad.scheduler.Rebalancer;
import org.apache.myriad.scheduler.ServiceProfileManager;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.ServiceTaskConstraints;
import org.apache.myriad.scheduler.TaskConstraintsManager;
import org.apache.myriad.scheduler.TaskFactory;
import org.apache.myriad.scheduler.TaskTerminator;
import org.apache.myriad.scheduler.TaskUtils;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.MyriadWebServer;
import org.apache.myriad.webapp.WebAppGuiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for myriad scheduler
 */
public class Main {
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  private MyriadWebServer webServer;
  private ScheduledExecutorService terminatorService;

  private ScheduledExecutorService rebalancerService;
  private HealthCheckRegistry healthCheckRegistry;

  private static Injector injector;

  public static void initialize(Configuration hadoopConf, AbstractYarnScheduler yarnScheduler, RMContext rmContext,
                                InterceptorRegistry registry) throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    MyriadConfiguration cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource(
        "myriad-config-default.yml"), MyriadConfiguration.class);

    MyriadModule myriadModule = new MyriadModule(cfg, hadoopConf, yarnScheduler, rmContext, registry);
    MesosModule mesosModule = new MesosModule();
    injector = Guice.createInjector(myriadModule, mesosModule, new WebAppGuiceModule());

    new Main().run(cfg);
  }

  // TODO (Kannan Rajah) Hack to get injector in unit test.
  public static Injector getInjector() {
    return injector;
  }

  public void run(MyriadConfiguration cfg) throws Exception {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Bindings: " + injector.getAllBindings());
    }

    JmxReporter.forRegistry(new MetricRegistry()).build().start();

    initWebApp(injector);
    initHealthChecks(injector);
    initProfiles(injector);
    validateNMInstances(injector);
    initServiceConfigurations(cfg, injector);
    initDisruptors(injector);

    initRebalancerService(cfg, injector);
    initTerminatorService(injector);
    startMesosDriver(injector);
    startNMInstances(injector);
    startJavaBasedTaskInstance(injector);
  }


  private void startMesosDriver(Injector injector) {
    LOGGER.info("starting mesosDriver..");
    injector.getInstance(MyriadDriverManager.class).startDriver();
    LOGGER.info("started mesosDriver..");
  }

  /**
   * Brings up the embedded jetty webserver for serving REST APIs.
   *
   * @param injector
   */
  private void initWebApp(Injector injector) throws Exception {
    webServer = injector.getInstance(MyriadWebServer.class);
    webServer.start();
  }

  /**
   * Initializes health checks.
   *
   * @param injector
   */
  private void initHealthChecks(Injector injector) {
    LOGGER.info("Initializing HealthChecks");
    healthCheckRegistry = new HealthCheckRegistry();
    healthCheckRegistry.register(MesosMasterHealthCheck.NAME, injector.getInstance(MesosMasterHealthCheck.class));
    healthCheckRegistry.register(ZookeeperHealthCheck.NAME, injector.getInstance(ZookeeperHealthCheck.class));
    healthCheckRegistry.register(MesosDriverHealthCheck.NAME, injector.getInstance(MesosDriverHealthCheck.class));
  }

  private void initProfiles(Injector injector) {
    LOGGER.info("Initializing Profiles");
    ServiceProfileManager profileManager = injector.getInstance(ServiceProfileManager.class);
    TaskConstraintsManager taskConstraintsManager = injector.getInstance(TaskConstraintsManager.class);
    taskConstraintsManager.addTaskConstraints(NodeManagerConfiguration.NM_TASK_PREFIX, new TaskFactory.NMTaskConstraints());
    Map<String, Map<String, String>> profiles = injector.getInstance(MyriadConfiguration.class).getProfiles();
    TaskUtils taskUtils = injector.getInstance(TaskUtils.class);
    if (MapUtils.isNotEmpty(profiles)) {
      for (Map.Entry<String, Map<String, String>> profile : profiles.entrySet()) {
        Map<String, String> profileResourceMap = profile.getValue();
        if (MapUtils.isNotEmpty(profiles) && profileResourceMap.containsKey("cpu") && profileResourceMap.containsKey("mem")) {
          Long cpu = Long.parseLong(profileResourceMap.get("cpu"));
          Long mem = Long.parseLong(profileResourceMap.get("mem"));

          ServiceResourceProfile serviceProfile = new ExtendedResourceProfile(new NMProfile(profile.getKey(), cpu, mem),
              taskUtils.getNodeManagerCpus(), taskUtils.getNodeManagerMemory());
          serviceProfile.setExecutorCpu(taskUtils.getExecutorCpus());
          serviceProfile.setExecutorMemory(taskUtils.getExecutorMemory());

          profileManager.add(serviceProfile);
        } else {
          LOGGER.error("Invalid definition for profile: " + profile.getKey());
        }
      }
    }
  }

  private void validateNMInstances(Injector injector) {
    LOGGER.info("Validating nmInstances..");
    Map<String, Integer> nmInstances = injector.getInstance(MyriadConfiguration.class).getNmInstances();
    ServiceProfileManager profileManager = injector.getInstance(ServiceProfileManager.class);

    long maxCpu = Long.MIN_VALUE;
    long maxMem = Long.MIN_VALUE;
    for (Map.Entry<String, Integer> entry : nmInstances.entrySet()) {
      String profile = entry.getKey();
      ServiceResourceProfile nodeManager = profileManager.get(profile);
      if (nodeManager == null) {
        throw new RuntimeException("Invalid profile name '" + profile + "' specified in 'nmInstances'");
      }
      if (entry.getValue() > 0) {
        if (nodeManager.getCpus() > maxCpu) { // find the profile with largest number of cpus
          maxCpu = nodeManager.getCpus().longValue();
          maxMem = nodeManager.getMemory().longValue(); // use the memory from the same profile
        }
      }
    }
    if (maxCpu <= 0 || maxMem <= 0) {
      throw new RuntimeException(
          "Please configure 'nmInstances' with at least one instance/profile " + "with non-zero cpu/mem resources.");
    }
  }

  private void startNMInstances(Injector injector) {
    Map<String, Integer> nmInstances = injector.getInstance(MyriadConfiguration.class).getNmInstances();
    MyriadOperations myriadOperations = injector.getInstance(MyriadOperations.class);
    ServiceProfileManager profileManager = injector.getInstance(ServiceProfileManager.class);
    SchedulerState schedulerState = injector.getInstance(SchedulerState.class);

    Set<org.apache.myriad.state.NodeTask> launchedNMTasks = new HashSet<>();
    launchedNMTasks.addAll(schedulerState.getPendingTasksByType(NodeManagerConfiguration.NM_TASK_PREFIX));
    if (!launchedNMTasks.isEmpty()) {
      LOGGER.info("{} NM(s) in pending state. Not launching additional NMs", launchedNMTasks.size());
      return;
    }

    launchedNMTasks.addAll(schedulerState.getStagingTasksByType(NodeManagerConfiguration.NM_TASK_PREFIX));
    if (!launchedNMTasks.isEmpty()) {
      LOGGER.info("{} NM(s) in staging state. Not launching additional NMs", launchedNMTasks.size());
      return;
    }

    launchedNMTasks.addAll(schedulerState.getActiveTasksByType(NodeManagerConfiguration.NM_TASK_PREFIX));
    if (!launchedNMTasks.isEmpty()) {
      LOGGER.info("{} NM(s) in active state. Not launching additional NMs", launchedNMTasks.size());
      return;
    }

    for (Map.Entry<String, Integer> entry : nmInstances.entrySet()) {
      LOGGER.info("Launching {} NM(s) with profile {}", entry.getValue(), entry.getKey());
      myriadOperations.flexUpCluster(profileManager.get(entry.getKey()), entry.getValue(), null);
    }
  }

  /**
   * Create ServiceProfile for any configured service
   *
   * @param cfg
   * @param injector
   */
  private void initServiceConfigurations(MyriadConfiguration cfg, Injector injector) {
    LOGGER.info("Initializing initServiceConfigurations");
    ServiceProfileManager profileManager = injector.getInstance(ServiceProfileManager.class);
    TaskConstraintsManager taskConstraintsManager = injector.getInstance(TaskConstraintsManager.class);

    Map<String, ServiceConfiguration> servicesConfigs = injector.getInstance(MyriadConfiguration.class).getServiceConfigurations();
    if (servicesConfigs != null) {
      for (Map.Entry<String, ServiceConfiguration> entry : servicesConfigs.entrySet()) {
        final String taskPrefix = entry.getKey();
        ServiceConfiguration config = entry.getValue();
        final Double cpu = config.getCpus().or(ServiceConfiguration.DEFAULT_CPU);
        final Double mem = config.getJvmMaxMemoryMB().or(ServiceConfiguration.DEFAULT_MEMORY);

        profileManager.add(new ServiceResourceProfile(taskPrefix, cpu, mem));
        taskConstraintsManager.addTaskConstraints(taskPrefix, new ServiceTaskConstraints(cfg, taskPrefix));
      }
    }
  }

  private void initTerminatorService(Injector injector) {
    LOGGER.info("Initializing Terminator");
    terminatorService = Executors.newScheduledThreadPool(1);
    final int initialDelay = 100;
    final int period = 2000;
    terminatorService.scheduleAtFixedRate(injector.getInstance(TaskTerminator.class), initialDelay, period, TimeUnit.MILLISECONDS);
  }

  private void initRebalancerService(MyriadConfiguration cfg, Injector injector) {
    if (cfg.isRebalancer()) {
      LOGGER.info("Initializing Rebalancer");
      rebalancerService = Executors.newScheduledThreadPool(1);
      final int initialDelay = 100;
      final int period = 5000;
      rebalancerService.scheduleAtFixedRate(injector.getInstance(Rebalancer.class), initialDelay, period, TimeUnit.MILLISECONDS);
    } else {
      LOGGER.info("Rebalancer is not turned on");
    }
  }

  private void initDisruptors(Injector injector) {
    LOGGER.info("Initializing Disruptors");
    DisruptorManager disruptorManager = injector.getInstance(DisruptorManager.class);
    disruptorManager.init(injector);
  }

  /**
   * Start tasks for configured services
   *
   * @param injector
   */
  private void startJavaBasedTaskInstance(Injector injector) {
    Map<String, ServiceConfiguration> auxServicesConfigs = injector.getInstance(MyriadConfiguration.class)
        .getServiceConfigurations();
    if (auxServicesConfigs != null) {
      MyriadOperations myriadOperations = injector.getInstance(MyriadOperations.class);
      for (Map.Entry<String, ServiceConfiguration> entry : auxServicesConfigs.entrySet()) {
        try {
          myriadOperations.flexUpAService(entry.getValue().getMaxInstances().or(1), entry.getKey());
        } catch (MyriadBadConfigurationException e) {
          LOGGER.warn("Exception while trying to flexup service: {}", entry.getKey(), e);
        }
      }
    }
  }
}
