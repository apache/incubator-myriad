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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.concurrent.FutureTask;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.state.MyriadState;
import org.mockito.Mockito;

/**
 * Guice Module for Mesos objects.
 */
public class MesosModule extends AbstractModule {
  public MesosModule() {
  }

  @Override
  protected void configure() {
    bind(org.apache.myriad.scheduler.MyriadDriver.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  SchedulerDriver providesSchedulerDriver(org.apache.myriad.scheduler.MyriadScheduler scheduler, MyriadConfiguration cfg,
                                          org.apache.myriad.state.SchedulerState schedulerState) {

    SchedulerDriver driver = Mockito.mock(SchedulerDriver.class);
    Mockito.when(driver.start()).thenReturn(Status.DRIVER_RUNNING);
    Mockito.when(driver.abort()).thenReturn(Status.DRIVER_ABORTED);

    return driver;
  }

  @Provides
  @Singleton
  State providesStateStore(MyriadConfiguration cfg) {
    State stateStore = Mockito.mock(State.class);

    Runnable dummyTask = new Runnable() {
      public void run() {
      }
    };

    Variable var = Mockito.mock(Variable.class);
    Protos.FrameworkID id = Protos.FrameworkID.newBuilder().setValue("1").build();

    Mockito.when(var.value()).thenReturn(id.toByteArray());
    FutureTask<Variable> futureTask = new FutureTask<Variable>(dummyTask, var);
    futureTask.run();
    Mockito.when(stateStore.fetch(MyriadState.KEY_FRAMEWORK_ID)).thenReturn(futureTask);

    return stateStore;
  }
}
