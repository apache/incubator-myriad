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

import com.lmax.disruptor.EventTranslator;
import java.util.List;
import javax.inject.Inject;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.event.DisconnectedEvent;
import org.apache.myriad.scheduler.event.ErrorEvent;
import org.apache.myriad.scheduler.event.ExecutorLostEvent;
import org.apache.myriad.scheduler.event.FrameworkMessageEvent;
import org.apache.myriad.scheduler.event.OfferRescindedEvent;
import org.apache.myriad.scheduler.event.ReRegisteredEvent;
import org.apache.myriad.scheduler.event.RegisteredEvent;
import org.apache.myriad.scheduler.event.ResourceOffersEvent;
import org.apache.myriad.scheduler.event.SlaveLostEvent;
import org.apache.myriad.scheduler.event.StatusUpdateEvent;

/**
 * Myriad Scheduler
 */
public class MyriadScheduler implements Scheduler {
  private org.apache.myriad.DisruptorManager disruptorManager;

  @Inject
  public MyriadScheduler(final MyriadConfiguration cfg, final org.apache.myriad.DisruptorManager disruptorManager) {
    this.disruptorManager = disruptorManager;
  }

  @Override
  public void registered(final SchedulerDriver driver, final Protos.FrameworkID frameworkId, final Protos.MasterInfo masterInfo) {
    disruptorManager.getRegisteredEventDisruptor().publishEvent(new EventTranslator<RegisteredEvent>() {
      @Override
      public void translateTo(RegisteredEvent event, long sequence) {
        event.setDriver(driver);
        event.setFrameworkId(frameworkId);
        event.setMasterInfo(masterInfo);
      }
    });
  }

  @Override
  public void reregistered(final SchedulerDriver driver, final Protos.MasterInfo masterInfo) {
    disruptorManager.getReRegisteredEventDisruptor().publishEvent(new EventTranslator<ReRegisteredEvent>() {
      @Override
      public void translateTo(ReRegisteredEvent event, long sequence) {
        event.setDriver(driver);
        event.setMasterInfo(masterInfo);
      }
    });
  }

  @Override
  public void resourceOffers(final SchedulerDriver driver, final List<Protos.Offer> offers) {
    disruptorManager.getResourceOffersEventDisruptor().publishEvent(new EventTranslator<ResourceOffersEvent>() {
      @Override
      public void translateTo(ResourceOffersEvent event, long sequence) {
        event.setDriver(driver);
        event.setOffers(offers);
      }
    });
  }

  @Override
  public void offerRescinded(final SchedulerDriver driver, final Protos.OfferID offerId) {
    disruptorManager.getOfferRescindedEventDisruptor().publishEvent(new EventTranslator<OfferRescindedEvent>() {
      @Override
      public void translateTo(OfferRescindedEvent event, long sequence) {
        event.setDriver(driver);
        event.setOfferId(offerId);
      }
    });
  }

  @Override
  public void statusUpdate(final SchedulerDriver driver, final Protos.TaskStatus status) {
    disruptorManager.getStatusUpdateEventDisruptor().publishEvent(new EventTranslator<StatusUpdateEvent>() {
      @Override
      public void translateTo(StatusUpdateEvent event, long sequence) {
        event.setDriver(driver);
        event.setStatus(status);
      }
    });
  }

  @Override
  public void frameworkMessage(final SchedulerDriver driver, final Protos.ExecutorID executorId, final Protos.SlaveID slaveId,
                               final byte[] bytes) {
    disruptorManager.getFrameworkMessageEventDisruptor().publishEvent(new EventTranslator<FrameworkMessageEvent>() {
      @Override
      public void translateTo(FrameworkMessageEvent event, long sequence) {
        event.setDriver(driver);
        event.setBytes(bytes);
        event.setExecutorId(executorId);
        event.setSlaveId(slaveId);
      }
    });
  }

  @Override
  public void disconnected(final SchedulerDriver driver) {
    disruptorManager.getDisconnectedEventDisruptor().publishEvent(new EventTranslator<DisconnectedEvent>() {
      @Override
      public void translateTo(DisconnectedEvent event, long sequence) {
        event.setDriver(driver);
      }
    });
  }

  @Override
  public void slaveLost(final SchedulerDriver driver, final Protos.SlaveID slaveId) {
    disruptorManager.getSlaveLostEventDisruptor().publishEvent(new EventTranslator<SlaveLostEvent>() {
      @Override
      public void translateTo(SlaveLostEvent event, long sequence) {
        event.setDriver(driver);
        event.setSlaveId(slaveId);
      }
    });
  }

  @Override
  public void executorLost(final SchedulerDriver driver, final Protos.ExecutorID executorId, final Protos.SlaveID slaveId,
                           final int exitStatus) {
    disruptorManager.getExecutorLostEventDisruptor().publishEvent(new EventTranslator<ExecutorLostEvent>() {
      @Override
      public void translateTo(ExecutorLostEvent event, long sequence) {
        event.setDriver(driver);
        event.setExecutorId(executorId);
        event.setSlaveId(slaveId);
        event.setExitStatus(exitStatus);
      }
    });
  }

  @Override
  public void error(final SchedulerDriver driver, final String message) {
    disruptorManager.getErrorEventDisruptor().publishEvent(new EventTranslator<ErrorEvent>() {
      @Override
      public void translateTo(ErrorEvent event, long sequence) {
        event.setDriver(driver);
        event.setMessage(message);
      }
    });
  }
}
