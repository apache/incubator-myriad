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
package com.ebay.myriad.scheduler;

import com.ebay.myriad.DisruptorManager;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.event.DisconnectedEvent;
import com.ebay.myriad.scheduler.event.ErrorEvent;
import com.ebay.myriad.scheduler.event.ExecutorLostEvent;
import com.ebay.myriad.scheduler.event.FrameworkMessageEvent;
import com.ebay.myriad.scheduler.event.OfferRescindedEvent;
import com.ebay.myriad.scheduler.event.ReRegisteredEvent;
import com.ebay.myriad.scheduler.event.RegisteredEvent;
import com.ebay.myriad.scheduler.event.ResourceOffersEvent;
import com.ebay.myriad.scheduler.event.SlaveLostEvent;
import com.ebay.myriad.scheduler.event.StatusUpdateEvent;
import com.lmax.disruptor.EventTranslator;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import javax.inject.Inject;
import java.util.List;

/**
 * Myriad Scheduler
 */
public class MyriadScheduler implements Scheduler {
    private DisruptorManager disruptorManager;

    @Inject
    public MyriadScheduler(final MyriadConfiguration cfg,
                           final DisruptorManager disruptorManager) {
        this.disruptorManager = disruptorManager;
    }

    @Override
    public void registered(final SchedulerDriver driver,
                           final Protos.FrameworkID frameworkId,
                           final Protos.MasterInfo masterInfo) {
        disruptorManager.getRegisteredEventDisruptor().publishEvent(
                new EventTranslator<RegisteredEvent>() {
                    @Override
                    public void translateTo(RegisteredEvent event, long sequence) {
                        event.setDriver(driver);
                        event.setFrameworkId(frameworkId);
                        event.setMasterInfo(masterInfo);
                    }
                });
    }

    @Override
    public void reregistered(final SchedulerDriver driver,
                             final Protos.MasterInfo masterInfo) {
        disruptorManager.getReRegisteredEventDisruptor().publishEvent(
                new EventTranslator<ReRegisteredEvent>() {
                    @Override
                    public void translateTo(ReRegisteredEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                        event.setMasterInfo(masterInfo);
                    }
                });
    }

    @Override
    public void resourceOffers(final SchedulerDriver driver,
                               final List<Protos.Offer> offers) {
        disruptorManager.getResourceOffersEventDisruptor().publishEvent(
                new EventTranslator<ResourceOffersEvent>() {
                    @Override
                    public void translateTo(ResourceOffersEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                        event.setOffers(offers);
                    }
                });
    }

    @Override
    public void offerRescinded(final SchedulerDriver driver,
                               final Protos.OfferID offerId) {
        disruptorManager.getOfferRescindedEventDisruptor().publishEvent(
                new EventTranslator<OfferRescindedEvent>() {
                    @Override
                    public void translateTo(OfferRescindedEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                        event.setOfferId(offerId);
                    }
                });
    }

    @Override
    public void statusUpdate(final SchedulerDriver driver,
                             final Protos.TaskStatus status) {
        disruptorManager.getStatusUpdateEventDisruptor().publishEvent(
                new EventTranslator<StatusUpdateEvent>() {
                    @Override
                    public void translateTo(StatusUpdateEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                        event.setStatus(status);
                    }
                });
    }

    @Override
    public void frameworkMessage(final SchedulerDriver driver,
                                 final Protos.ExecutorID executorId,
                                 final Protos.SlaveID slaveId,
                                 final byte[] bytes) {
        disruptorManager.getFrameworkMessageEventDisruptor().publishEvent(
                new EventTranslator<FrameworkMessageEvent>() {
                    @Override
                    public void translateTo(FrameworkMessageEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                        event.setBytes(bytes);
                        event.setExecutorId(executorId);
                        event.setSlaveId(slaveId);
                    }
                });
    }

    @Override
    public void disconnected(final SchedulerDriver driver) {
        disruptorManager.getDisconnectedEventDisruptor().publishEvent(
                new EventTranslator<DisconnectedEvent>() {
                    @Override
                    public void translateTo(DisconnectedEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                    }
                });
    }

    @Override
    public void slaveLost(final SchedulerDriver driver,
                          final Protos.SlaveID slaveId) {
        disruptorManager.getSlaveLostEventDisruptor().publishEvent(
                new EventTranslator<SlaveLostEvent>() {
                    @Override
                    public void translateTo(SlaveLostEvent event, long sequence) {
                        event.setDriver(driver);
                        event.setSlaveId(slaveId);
                    }
                });
    }

    @Override
    public void executorLost(final SchedulerDriver driver,
                             final Protos.ExecutorID executorId,
                             final Protos.SlaveID slaveId,
                             final int exitStatus) {
        disruptorManager.getExecutorLostEventDisruptor().publishEvent(
                new EventTranslator<ExecutorLostEvent>() {
                    @Override
                    public void translateTo(ExecutorLostEvent event,
                                            long sequence) {
                        event.setDriver(driver);
                        event.setExecutorId(executorId);
                        event.setSlaveId(slaveId);
                        event.setExitStatus(exitStatus);
                    }
                });
    }

    @Override
    public void error(final SchedulerDriver driver, final String message) {
        disruptorManager.getErrorEventDisruptor().publishEvent(
                new EventTranslator<ErrorEvent>() {
                    @Override
                    public void translateTo(ErrorEvent event, long sequence) {
                        event.setDriver(driver);
                        event.setMessage(message);
                    }
                });
    }
}
