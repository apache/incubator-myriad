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

import com.ebay.myriad.scheduler.event.*;
import com.ebay.myriad.scheduler.event.handlers.*;
import com.google.inject.Injector;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DisruptorManager {
    private ExecutorService disruptorExecutors;

    private Disruptor<RegisteredEvent> registeredEventDisruptor;
    private Disruptor<ReRegisteredEvent> reRegisteredEventDisruptor;
    private Disruptor<ResourceOffersEvent> resourceOffersEventDisruptor;
    private Disruptor<OfferRescindedEvent> offerRescindedEventDisruptor;
    private Disruptor<StatusUpdateEvent> statusUpdateEventDisruptor;
    private Disruptor<FrameworkMessageEvent> frameworkMessageEventDisruptor;
    private Disruptor<DisconnectedEvent> disconnectedEventDisruptor;
    private Disruptor<SlaveLostEvent> slaveLostEventDisruptor;
    private Disruptor<ExecutorLostEvent> executorLostEventDisruptor;
    private Disruptor<ErrorEvent> errorEventDisruptor;

    public DisruptorManager() {

    }

    @SuppressWarnings("unchecked")
    public void init(Injector injector) {
        this.disruptorExecutors = Executors.newCachedThreadPool();

        this.registeredEventDisruptor = new Disruptor<>(
                new RegisteredEventFactory(), 64, disruptorExecutors);
        this.registeredEventDisruptor.handleEventsWith(injector
                .getInstance(RegisteredEventHandler.class));
        this.registeredEventDisruptor.start();

        this.reRegisteredEventDisruptor = new Disruptor<>(
                new ReRegisteredEventFactory(), 64, disruptorExecutors);
        this.reRegisteredEventDisruptor.handleEventsWith(injector
                .getInstance(ReRegisteredEventHandler.class));
        this.reRegisteredEventDisruptor.start();

        this.resourceOffersEventDisruptor = new Disruptor<>(
                new ResourceOffersEventFactory(), 1024, disruptorExecutors);
        this.resourceOffersEventDisruptor.handleEventsWith(injector
                .getInstance(ResourceOffersEventHandler.class));
        this.resourceOffersEventDisruptor.start();

        this.offerRescindedEventDisruptor = new Disruptor<>(
                new OfferRescindedEventFactory(), 1024, disruptorExecutors);
        this.offerRescindedEventDisruptor.handleEventsWith(injector
                .getInstance(OfferRescindedEventHandler.class));
        this.offerRescindedEventDisruptor.start();

        this.statusUpdateEventDisruptor = new Disruptor<>(
                new StatusUpdateEventFactory(), 1024, disruptorExecutors);
        this.statusUpdateEventDisruptor.handleEventsWith(injector
                .getInstance(StatusUpdateEventHandler.class));
        this.statusUpdateEventDisruptor.start();

        this.frameworkMessageEventDisruptor = new Disruptor<>(
                new FrameworkMessageEventFactory(), 1024, disruptorExecutors);
        this.frameworkMessageEventDisruptor.handleEventsWith(injector
                .getInstance(FrameworkMessageEventHandler.class));
        this.frameworkMessageEventDisruptor.start();

        this.disconnectedEventDisruptor = new Disruptor<>(
                new DisconnectedEventFactory(), 1024, disruptorExecutors);
        this.disconnectedEventDisruptor.handleEventsWith(injector
                .getInstance(DisconnectedEventHandler.class));
        this.disconnectedEventDisruptor.start();

        this.slaveLostEventDisruptor = new Disruptor<>(
                new SlaveLostEventFactory(), 1024, disruptorExecutors);
        this.slaveLostEventDisruptor.handleEventsWith(injector
                .getInstance(SlaveLostEventHandler.class));
        this.slaveLostEventDisruptor.start();

        this.executorLostEventDisruptor = new Disruptor<>(
                new ExecutorLostEventFactory(), 1024, disruptorExecutors);
        this.executorLostEventDisruptor.handleEventsWith(injector
                .getInstance(ExecutorLostEventHandler.class));
        this.executorLostEventDisruptor.start();

        this.errorEventDisruptor = new Disruptor<>(new ErrorEventFactory(),
                1024, disruptorExecutors);
        this.errorEventDisruptor.handleEventsWith(injector
                .getInstance(ErrorEventHandler.class));
        this.errorEventDisruptor.start();
    }

    public Disruptor<RegisteredEvent> getRegisteredEventDisruptor() {
        return registeredEventDisruptor;
    }

    public Disruptor<ReRegisteredEvent> getReRegisteredEventDisruptor() {
        return reRegisteredEventDisruptor;
    }

    public Disruptor<ResourceOffersEvent> getResourceOffersEventDisruptor() {
        return resourceOffersEventDisruptor;
    }

    public Disruptor<OfferRescindedEvent> getOfferRescindedEventDisruptor() {
        return offerRescindedEventDisruptor;
    }

    public Disruptor<StatusUpdateEvent> getStatusUpdateEventDisruptor() {
        return statusUpdateEventDisruptor;
    }

    public Disruptor<FrameworkMessageEvent> getFrameworkMessageEventDisruptor() {
        return frameworkMessageEventDisruptor;
    }

    public Disruptor<DisconnectedEvent> getDisconnectedEventDisruptor() {
        return disconnectedEventDisruptor;
    }

    public Disruptor<SlaveLostEvent> getSlaveLostEventDisruptor() {
        return slaveLostEventDisruptor;
    }

    public Disruptor<ExecutorLostEvent> getExecutorLostEventDisruptor() {
        return executorLostEventDisruptor;
    }

    public Disruptor<ErrorEvent> getErrorEventDisruptor() {
        return errorEventDisruptor;
    }

}
