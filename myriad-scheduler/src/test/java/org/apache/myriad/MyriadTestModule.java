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

package org.apache.myriad;

import java.io.IOException;
import java.util.Map;

import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.scheduler.ExecutorCommandLineGenerator;
import org.apache.myriad.scheduler.NMExecutorCommandLineGenerator;
import org.apache.myriad.scheduler.NMTaskFactory;
import org.apache.myriad.scheduler.ServiceTaskFactory;
import org.apache.myriad.scheduler.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

/**
 * AbstractModule extension for UnitTests
 */
public class MyriadTestModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadTestModule.class);

  private MyriadConfiguration cfg;

  @SuppressWarnings("unchecked")
  @Override
  protected void configure() {

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
          MyriadConfiguration.class);
    } catch (IOException e1) {
      LOGGER.error("IOException", e1);
      return;
    }

    if (cfg == null) {
      return;
    }

    bind(MyriadConfiguration.class).toInstance(cfg);

    MapBinder<String, TaskFactory> mapBinder = MapBinder.newMapBinder(binder(), String.class, TaskFactory.class);
    mapBinder.addBinding(NodeManagerConfiguration.DEFAULT_NM_TASK_PREFIX).to(NMTaskFactory.class).in(Scopes.SINGLETON);
    Map<String, ServiceConfiguration> auxServicesConfigs = cfg.getServiceConfigurations();
    for (Map.Entry<String, ServiceConfiguration> entry : auxServicesConfigs.entrySet()) {
      String taskFactoryClass = entry.getValue().getTaskFactoryImplName().orNull();
      if (taskFactoryClass != null) {
        try {
          Class<? extends TaskFactory> implClass = (Class<? extends TaskFactory>) Class.forName(taskFactoryClass);
          mapBinder.addBinding(entry.getKey()).to(implClass).in(Scopes.SINGLETON);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      } else {
        mapBinder.addBinding(entry.getKey()).to(ServiceTaskFactory.class).in(Scopes.SINGLETON);
      }
    }
  }

  @Provides
  @Singleton
  ExecutorCommandLineGenerator providesCLIGenerator(MyriadConfiguration cfg) {
    return new NMExecutorCommandLineGenerator(cfg);
  }

}
