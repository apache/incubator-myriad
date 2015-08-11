package com.ebay.myriad;

import java.util.concurrent.FutureTask;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;
import org.mockito.Mockito;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.MyriadDriver;
import com.ebay.myriad.scheduler.MyriadScheduler;
import com.ebay.myriad.state.MyriadState;
import com.ebay.myriad.state.SchedulerState;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

/**
 * Guice Module for Mesos objects.
 */
public class MesosModule extends AbstractModule {
  public MesosModule() {
  }

  @Override
  protected void configure() {
    bind(MyriadDriver.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  SchedulerDriver providesSchedulerDriver(
      MyriadScheduler scheduler,
      MyriadConfiguration cfg,
      SchedulerState schedulerState) {

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
    Protos.FrameworkID id = Protos.FrameworkID.newBuilder()
      .setValue("1").build();

    Mockito.when(var.value()).thenReturn(id.toByteArray());
    FutureTask<Variable> futureTask = new FutureTask<Variable>(dummyTask, var);
    futureTask.run();
    Mockito.when(stateStore.fetch(MyriadState.KEY_FRAMEWORK_ID))
      .thenReturn(futureTask);

    return stateStore;
  }
}
