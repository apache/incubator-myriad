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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ebay.myriad;

import com.ebay.myriad.configuration.ServiceConfiguration;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.configuration.MyriadExecutorConfiguration;
import com.ebay.myriad.configuration.NodeManagerConfiguration;
import com.ebay.myriad.policy.LeastAMNodesFirstPolicy;
import com.ebay.myriad.policy.NodeScaleDownPolicy;
import com.ebay.myriad.scheduler.MyriadDriverManager;
import com.ebay.myriad.scheduler.MyriadScheduler;
import com.ebay.myriad.scheduler.fgs.NMHeartBeatHandler;
import com.ebay.myriad.scheduler.fgs.NodeStore;
import com.ebay.myriad.scheduler.fgs.OfferLifecycleManager;
import com.ebay.myriad.scheduler.DownloadNMExecutorCLGenImpl;
import com.ebay.myriad.scheduler.ExecutorCommandLineGenerator;
import com.ebay.myriad.scheduler.NMExecutorCLGenImpl;
import com.ebay.myriad.scheduler.NMTaskFactoryAnnotation;
import com.ebay.myriad.scheduler.ReconcileService;
import com.ebay.myriad.scheduler.ServiceProfileManager;
import com.ebay.myriad.scheduler.ServiceTaskFactoryImpl;
import com.ebay.myriad.scheduler.TaskConstraintsManager;
import com.ebay.myriad.scheduler.TaskFactory;
import com.ebay.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import com.ebay.myriad.scheduler.fgs.YarnNodeCapacityManager;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.ebay.myriad.state.MyriadStateStore;
import com.ebay.myriad.state.SchedulerState;
import com.ebay.myriad.webapp.HttpConnectorProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Guice Module for Myriad
 */
public class MyriadModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadModule.class);

    private MyriadConfiguration cfg;
    private Configuration hadoopConf;
    private AbstractYarnScheduler yarnScheduler;
    private final RMContext rmContext;
    private InterceptorRegistry interceptorRegistry;

    public MyriadModule(MyriadConfiguration cfg,
                        Configuration hadoopConf,
                        AbstractYarnScheduler yarnScheduler,
                        RMContext rmContext,
                        InterceptorRegistry interceptorRegistry) {
        this.cfg = cfg;
        this.hadoopConf = hadoopConf;
        this.yarnScheduler = yarnScheduler;
        this.rmContext = rmContext;
        this.interceptorRegistry = interceptorRegistry;
    }

    @Override
    protected void configure() {
        LOGGER.debug("Configuring guice");
        bind(MyriadConfiguration.class).toInstance(cfg);
        bind(Configuration.class).toInstance(hadoopConf);
        bind(RMContext.class).toInstance(rmContext);
        bind(AbstractYarnScheduler.class).toInstance(yarnScheduler);
        bind(InterceptorRegistry.class).toInstance(interceptorRegistry);
        bind(MyriadDriverManager.class).in(Scopes.SINGLETON);
        bind(MyriadScheduler.class).in(Scopes.SINGLETON);
        bind(ServiceProfileManager.class).in(Scopes.SINGLETON);
        bind(DisruptorManager.class).in(Scopes.SINGLETON);
        bind(ReconcileService.class).in(Scopes.SINGLETON);
        bind(HttpConnectorProvider.class).in(Scopes.SINGLETON);
        bind(TaskConstraintsManager.class).in(Scopes.SINGLETON);
        // add special binding between TaskFactory and NMTaskFactoryImpl to ease up 
        // usage of TaskFactory
        bind(TaskFactory.class).annotatedWith(NMTaskFactoryAnnotation.class).to(NMTaskFactoryImpl.class);
        bind(YarnNodeCapacityManager.class).in(Scopes.SINGLETON);
        bind(NodeStore.class).in(Scopes.SINGLETON);
        bind(OfferLifecycleManager.class).in(Scopes.SINGLETON);
        bind(NMHeartBeatHandler.class).asEagerSingleton();

        MapBinder<String, TaskFactory> mapBinder
        = MapBinder.newMapBinder(binder(), String.class, TaskFactory.class);
        mapBinder.addBinding(NodeManagerConfiguration.NM_TASK_PREFIX).to(NMTaskFactoryImpl.class).in(Scopes.SINGLETON);
        Map<String, ServiceConfiguration> auxServicesConfigs = cfg.getServiceConfigurations();
        if (auxServicesConfigs != null) {
          for (Map.Entry<String, ServiceConfiguration> entry : auxServicesConfigs.entrySet()) {
            String taskFactoryClass = entry.getValue().getTaskFactoryImplName().orNull();
            if (taskFactoryClass != null) {
              try {
                Class<? extends TaskFactory> implClass = (Class<? extends TaskFactory>) Class.forName(taskFactoryClass);
                mapBinder.addBinding(entry.getKey()).to(implClass).in(Scopes.SINGLETON);
              } catch (ClassNotFoundException e) {
                LOGGER.error("ClassNotFoundException", e);
              }
            } else {
              mapBinder.addBinding(entry.getKey()).to(ServiceTaskFactoryImpl.class).in(Scopes.SINGLETON);
            }
          }
        }
        //TODO(Santosh): Should be configurable as well
        bind(NodeScaleDownPolicy.class).to(LeastAMNodesFirstPolicy.class).in(Scopes.SINGLETON);
    }

    @Provides
    @Singleton
    SchedulerState providesSchedulerState(MyriadConfiguration cfg) {
        LOGGER.debug("Configuring SchedulerState provider");
        MyriadStateStore myriadStateStore = null;
        if (cfg.isHAEnabled()) {
            myriadStateStore = providesMyriadStateStore();
            if (myriadStateStore == null) {
                throw new RuntimeException("Could not find a state store" +
                    " implementation for Myriad. The 'yarn.resourcemanager.store.class'" +
                    " property should be set to a class implementing the" +
                    " MyriadStateStore interface. For e.g." +
                    " org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore");
            }
        }
        return new SchedulerState(myriadStateStore);
    }

    private MyriadStateStore providesMyriadStateStore() {
        // TODO (sdaingade) Read the implementation class from yml
        // once multiple implementations are available.
        if (rmContext.getStateStore() instanceof MyriadStateStore) {
            return (MyriadStateStore) rmContext.getStateStore();
        }
        return null;
    }

    @Provides
    @Singleton
    ExecutorCommandLineGenerator providesCLIGenerator(MyriadConfiguration cfg) {
        ExecutorCommandLineGenerator cliGenerator = null;
        MyriadExecutorConfiguration myriadExecutorConfiguration =
            cfg.getMyriadExecutorConfiguration();
        if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
            cliGenerator = new DownloadNMExecutorCLGenImpl(cfg,
               myriadExecutorConfiguration.getNodeManagerUri().get());
        } else {
            cliGenerator = new NMExecutorCLGenImpl(cfg);
        }
        return cliGenerator;
    }    
}
