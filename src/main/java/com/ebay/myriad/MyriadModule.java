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

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.*;
import com.ebay.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import com.ebay.myriad.state.MyriadState;
import com.ebay.myriad.state.SchedulerState;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import org.apache.mesos.state.ZooKeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MyriadModule extends AbstractModule {
    private final static Logger LOGGER = LoggerFactory
            .getLogger(MyriadModule.class);

    private MyriadConfiguration cfg;

    public MyriadModule(MyriadConfiguration cfg) {
        this.cfg = cfg;
    }

    @Override
    protected void configure() {
        LOGGER.debug("Configuring guice");
        bind(MyriadConfiguration.class).toInstance(cfg);
        bind(MyriadDriver.class).in(Scopes.SINGLETON);
        bind(MyriadDriverManager.class).in(Scopes.SINGLETON);
        bind(MyriadScheduler.class).in(Scopes.SINGLETON);
        bind(NMProfileManager.class).in(Scopes.SINGLETON);
        bind(DisruptorManager.class).in(Scopes.SINGLETON);
        bind(TaskFactory.class).to(NMTaskFactoryImpl.class);
    }

    @Provides
    SchedulerState providesSchedulerState(MyriadConfiguration cfg) {
        LOGGER.debug("Configuring SchedulerState provider");
        ZooKeeperState zkState = new ZooKeeperState(
                cfg.getZkServers(),
                cfg.getZkTimeout(),
                TimeUnit.MILLISECONDS,
                "/myriad/" + cfg.getFrameworkName());
        MyriadState state = new MyriadState(zkState);
        return new SchedulerState(state);
    }
}
