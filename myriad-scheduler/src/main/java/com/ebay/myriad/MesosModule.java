/**
 * Copyright 2015 PayPal
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.Credential;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.FrameworkInfo.Builder;
import org.apache.mesos.SchedulerDriver;
import org.apache.mesos.state.State;
import org.apache.mesos.state.ZooKeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.MyriadDriver;
import com.ebay.myriad.scheduler.MyriadScheduler;
import com.ebay.myriad.state.SchedulerState;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Guice Module for Mesos objects.
 */
public class MesosModule extends AbstractModule {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      MesosModule.class);

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

    Builder frameworkInfoBuilder = FrameworkInfo.newBuilder().setUser("")
      .setName(cfg.getFrameworkName())
      .setCheckpoint(cfg.isCheckpoint())
      .setFailoverTimeout(cfg.getFrameworkFailoverTimeout());

    if (StringUtils.isNotEmpty(cfg.getFrameworkRole())) {
      frameworkInfoBuilder.setRole(cfg.getFrameworkRole());
    }

    FrameworkID frameworkId;
    try {
      frameworkId = schedulerState.getMyriadState().getFrameworkID();
      if (frameworkId != null) {
        LOGGER.info("Attempting to re-register with frameworkId: {}", frameworkId.getValue());
        frameworkInfoBuilder.setId(frameworkId);
      }
    } catch (InterruptedException | ExecutionException | InvalidProtocolBufferException e) {
      LOGGER.error("Error fetching frameworkId", e);
      throw new RuntimeException(e);
    }

    String mesosAuthenticationPrincipal = cfg.getMesosAuthenticationPrincipal();
    String mesosAuthenticationSecretFilename = cfg.getMesosAuthenticationSecretFilename();
    if (StringUtils.isNotEmpty(mesosAuthenticationPrincipal)) {
      frameworkInfoBuilder.setPrincipal(mesosAuthenticationPrincipal);

      Credential.Builder credentialBuilder = Credential.newBuilder();
      credentialBuilder.setPrincipal(mesosAuthenticationPrincipal);
      if (StringUtils.isNotEmpty(mesosAuthenticationSecretFilename)) {
        try  {
          credentialBuilder.setSecret(ByteString.readFrom(
                new FileInputStream(mesosAuthenticationSecretFilename)));
        } catch (FileNotFoundException ex) {
          LOGGER.error("Mesos authentication secret file was not found", ex);
          throw new RuntimeException(ex);
        } catch (IOException ex) {
          LOGGER.error("Error reading Mesos authentication secret file", ex);
          throw new RuntimeException(ex);
        }
      }
      return new MesosSchedulerDriver(scheduler, frameworkInfoBuilder.build(),
          cfg.getMesosMaster(), credentialBuilder.build());
    } else {
      return new MesosSchedulerDriver(scheduler,
          frameworkInfoBuilder.build(), cfg.getMesosMaster());
    }
  }

  @Provides
  @Singleton
  State providesStateStore(MyriadConfiguration cfg) {
    return new ZooKeeperState(
        cfg.getZkServers(),
        cfg.getZkTimeout(),
        TimeUnit.MILLISECONDS,
        "/myriad/" + cfg.getFrameworkName());
  }
}
