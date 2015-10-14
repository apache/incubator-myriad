/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.apache.myriad;

import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.MyriadExecutorConfiguration;
import org.apache.myriad.policy.LeastAMNodesFirstPolicy;
import org.apache.myriad.policy.NodeScaleDownPolicy;
import org.apache.myriad.scheduler.MyriadDriverManager;
import org.apache.myriad.scheduler.MyriadScheduler;
import org.apache.myriad.scheduler.fgs.NMHeartBeatHandler;
import org.apache.myriad.scheduler.NMProfileManager;
import org.apache.myriad.scheduler.fgs.NodeStore;
import org.apache.myriad.scheduler.fgs.OfferLifecycleManager;
import org.apache.myriad.scheduler.DownloadNMExecutorCLGenImpl;
import org.apache.myriad.scheduler.ExecutorCommandLineGenerator;
import org.apache.myriad.scheduler.NMExecutorCLGenImpl;
import org.apache.myriad.scheduler.ReconcileService;
import org.apache.myriad.scheduler.TaskFactory;
import org.apache.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import org.apache.myriad.scheduler.fgs.YarnNodeCapacityManager;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.MyriadStateStore;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.HttpConnectorProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        bind(NMProfileManager.class).in(Scopes.SINGLETON);
        bind(DisruptorManager.class).in(Scopes.SINGLETON);
        bind(TaskFactory.class).to(NMTaskFactoryImpl.class);
        bind(ReconcileService.class).in(Scopes.SINGLETON);
        bind(HttpConnectorProvider.class).in(Scopes.SINGLETON);
        bind(YarnNodeCapacityManager.class).in(Scopes.SINGLETON);
        bind(NodeStore.class).in(Scopes.SINGLETON);
        bind(OfferLifecycleManager.class).in(Scopes.SINGLETON);
        bind(NMHeartBeatHandler.class).asEagerSingleton();

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
