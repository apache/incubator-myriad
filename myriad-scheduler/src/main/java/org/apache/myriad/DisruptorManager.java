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

import com.google.inject.Injector;
import com.lmax.disruptor.dsl.Disruptor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.myriad.scheduler.event.*;
import org.apache.myriad.scheduler.event.handlers.DisconnectedEventHandler;
import org.apache.myriad.scheduler.event.handlers.ErrorEventHandler;
import org.apache.myriad.scheduler.event.handlers.ExecutorLostEventHandler;
import org.apache.myriad.scheduler.event.handlers.FrameworkMessageEventHandler;
import org.apache.myriad.scheduler.event.handlers.OfferRescindedEventHandler;
import org.apache.myriad.scheduler.event.handlers.ReRegisteredEventHandler;
import org.apache.myriad.scheduler.event.handlers.RegisteredEventHandler;
import org.apache.myriad.scheduler.event.handlers.ResourceOffersEventHandler;
import org.apache.myriad.scheduler.event.handlers.SlaveLostEventHandler;
import org.apache.myriad.scheduler.event.handlers.StatusUpdateEventHandler;

/**
 * Disruptor class is an event bus used in high speed financial systems. http://martinfowler.com/articles/lmax.html
 * Here it is used to abstract incoming events.
 */
public class DisruptorManager {
  private ExecutorService disruptorExecutors;

  private static final int DEFAULT_SMALL_RINGBUFFER_SIZE = 64;
  private static final int DEFAULT_LARGE_RINGBUFFER_SIZE = 1024;

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

  @SuppressWarnings("unchecked")
  public void init(Injector injector) {
    this.disruptorExecutors = Executors.newCachedThreadPool();

    // todo:  (kensipe) need to make ringsize configurable (overriding the defaults)


    this.registeredEventDisruptor = new Disruptor<>(new RegisteredEventFactory(), DEFAULT_SMALL_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.registeredEventDisruptor.handleEventsWith(injector.getInstance(RegisteredEventHandler.class));
    this.registeredEventDisruptor.start();

    this.reRegisteredEventDisruptor = new Disruptor<>(new ReRegisteredEventFactory(), DEFAULT_SMALL_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.reRegisteredEventDisruptor.handleEventsWith(injector.getInstance(ReRegisteredEventHandler.class));
    this.reRegisteredEventDisruptor.start();


    this.resourceOffersEventDisruptor = new Disruptor<>(new ResourceOffersEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.resourceOffersEventDisruptor.handleEventsWith(injector.getInstance(ResourceOffersEventHandler.class));
    this.resourceOffersEventDisruptor.start();

    this.offerRescindedEventDisruptor = new Disruptor<>(new OfferRescindedEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.offerRescindedEventDisruptor.handleEventsWith(injector.getInstance(OfferRescindedEventHandler.class));
    this.offerRescindedEventDisruptor.start();

    this.statusUpdateEventDisruptor = new Disruptor<>(new StatusUpdateEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.statusUpdateEventDisruptor.handleEventsWith(injector.getInstance(StatusUpdateEventHandler.class));
    this.statusUpdateEventDisruptor.start();

    this.frameworkMessageEventDisruptor = new Disruptor<>(new FrameworkMessageEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.frameworkMessageEventDisruptor.handleEventsWith(injector.getInstance(FrameworkMessageEventHandler.class));
    this.frameworkMessageEventDisruptor.start();

    this.disconnectedEventDisruptor = new Disruptor<>(new DisconnectedEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.disconnectedEventDisruptor.handleEventsWith(injector.getInstance(DisconnectedEventHandler.class));
    this.disconnectedEventDisruptor.start();

    this.slaveLostEventDisruptor = new Disruptor<>(new SlaveLostEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.slaveLostEventDisruptor.handleEventsWith(injector.getInstance(SlaveLostEventHandler.class));
    this.slaveLostEventDisruptor.start();

    this.executorLostEventDisruptor = new Disruptor<>(new ExecutorLostEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE,
        disruptorExecutors);
    this.executorLostEventDisruptor.handleEventsWith(injector.getInstance(ExecutorLostEventHandler.class));
    this.executorLostEventDisruptor.start();

    this.errorEventDisruptor = new Disruptor<>(new ErrorEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.errorEventDisruptor.handleEventsWith(injector.getInstance(ErrorEventHandler.class));
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
