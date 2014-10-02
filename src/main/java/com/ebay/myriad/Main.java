/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad;

import com.codahale.metrics.health.HealthCheckRegistry;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.health.MesosDriverHealthCheck;
import com.ebay.myriad.health.MesosMasterHealthCheck;
import com.ebay.myriad.health.ZookeeperHealthCheck;
import com.ebay.myriad.scheduler.MyriadDriverManager;
import com.ebay.myriad.scheduler.NMProfile;
import com.ebay.myriad.scheduler.NMProfileManager;
import com.ebay.myriad.scheduler.Rebalancer;
import com.ebay.myriad.scheduler.TaskTerminator;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	private ScheduledExecutorService terminatorService;

	private ScheduledExecutorService rebalancerService;

	public static void main(String[] args) throws Exception {
    initialize();
  }

  public static void initialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    MyriadConfiguration cfg = mapper.readValue(
      Thread.currentThread().getContextClassLoader().getResource("config.yml"),
      MyriadConfiguration.class);
    new Main().run(cfg);
  }

	public void run(MyriadConfiguration cfg) {
		MyriadModule myriadModule = new MyriadModule(cfg);
		Injector injector = Guice.createInjector(myriadModule);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Bindings: " + injector.getAllBindings());
		}

		JmxReporter.forRegistry(new MetricRegistry()).build().start();

		registerResources(injector);
		initHealthChecks(injector);
		initProfiles(cfg, injector);
		initDisruptors(injector);
		initRebalancerService(cfg, injector);
		initTerminatorService(injector);
    startMesosDriver(injector);
	}

	private void startMesosDriver(Injector injector) {
    injector.getInstance(MyriadDriverManager.class).startDriver();
	}

	/**
	 * Registers API resources.
	 * 
	 * @param injector
	 */
	private void registerResources(Injector injector) {
    //TODO(Santosh): accomplish the below without dropwizard
//    env.jersey().register(injector.getInstance(ConfigurationResource.class));
//		env.jersey().register(injector.getInstance(DashboardResource.class));
//		env.jersey().register(injector.getInstance(ClustersResource.class));
//		env.jersey().register(injector.getInstance(SchedulerStateResource.class));
	}

	/**
	 * Initializes health checks.
	 * 
	 * @param injector
	 */
	private void initHealthChecks(Injector injector) {
		LOGGER.info("Initializing HealthChecks");
    HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    healthCheckRegistry.register(MesosMasterHealthCheck.NAME,
      injector.getInstance(MesosMasterHealthCheck.class));
		healthCheckRegistry.register(ZookeeperHealthCheck.NAME,
      injector.getInstance(ZookeeperHealthCheck.class));
		healthCheckRegistry.register(MesosDriverHealthCheck.NAME,
      injector.getInstance(MesosDriverHealthCheck.class));
	}

	private void initProfiles(final MyriadConfiguration cfg,
                            Injector injector) {
		LOGGER.info("Initializing Profiles");
		NMProfileManager profileManager = injector
				.getInstance(NMProfileManager.class);
		Map<String, Map<String, String>> profiles = cfg.getProfiles();
		if (MapUtils.isNotEmpty(profiles)) {
			Iterator<String> profileKeys = profiles.keySet().iterator();
			while (profileKeys.hasNext()) {
				String profileKey = profileKeys.next();
				Map<String, String> profileResourceMap = profiles
						.get(profileKey);
				if (MapUtils.isNotEmpty(profiles)
						&& profileResourceMap.containsKey("cpu")
						&& profileResourceMap.containsKey("mem")) {
					double cpu = Double.parseDouble(profileResourceMap
							.get("cpu"));
					double mem = Double.parseDouble(profileResourceMap
							.get("mem"));

					profileManager.add(new NMProfile(profileKey, cpu, mem));
				} else {
					LOGGER.error("Invalid definition for profile: "
							+ profileKey);
				}
			}
		}
	}

	private void initTerminatorService(Injector injector) {
		LOGGER.info("Initializing Terminator");
		terminatorService = Executors.newScheduledThreadPool(1);
		terminatorService.scheduleAtFixedRate(
				injector.getInstance(TaskTerminator.class), 100, 2000,
				TimeUnit.MILLISECONDS);
	}

	private void initRebalancerService(MyriadConfiguration cfg,
                                     Injector injector) {
		if (cfg.isRebalancer()) {
			LOGGER.info("Initializing Rebalancer");
			rebalancerService = Executors.newScheduledThreadPool(1);
			rebalancerService.scheduleAtFixedRate(
					injector.getInstance(Rebalancer.class), 100, 5000,
					TimeUnit.MILLISECONDS);
		} else {
			LOGGER.info("Rebalancer is not turned on");
		}
	}

	private void initDisruptors(Injector injector) {
		LOGGER.info("Initializing Disruptors");
		DisruptorManager disruptorManager = injector
				.getInstance(DisruptorManager.class);
		disruptorManager.init(injector);
	}

}