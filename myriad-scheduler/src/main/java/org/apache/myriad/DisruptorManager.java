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

  private Disruptor<org.apache.myriad.scheduler.event.RegisteredEvent> registeredEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.ReRegisteredEvent> reRegisteredEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.ResourceOffersEvent> resourceOffersEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.OfferRescindedEvent> offerRescindedEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.StatusUpdateEvent> statusUpdateEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.FrameworkMessageEvent> frameworkMessageEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.DisconnectedEvent> disconnectedEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.SlaveLostEvent> slaveLostEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.ExecutorLostEvent> executorLostEventDisruptor;
  private Disruptor<org.apache.myriad.scheduler.event.ErrorEvent> errorEventDisruptor;

  @SuppressWarnings("unchecked")
  public void init(Injector injector) {
    this.disruptorExecutors = Executors.newCachedThreadPool();

    // todo:  (kensipe) need to make ringsize configurable (overriding the defaults)


    this.registeredEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.RegisteredEventFactory(), DEFAULT_SMALL_RINGBUFFER_SIZE, disruptorExecutors);
    this.registeredEventDisruptor.handleEventsWith(injector.getInstance(RegisteredEventHandler.class));
    this.registeredEventDisruptor.start();

    this.reRegisteredEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.ReRegisteredEventFactory(), DEFAULT_SMALL_RINGBUFFER_SIZE, disruptorExecutors);
    this.reRegisteredEventDisruptor.handleEventsWith(injector.getInstance(ReRegisteredEventHandler.class));
    this.reRegisteredEventDisruptor.start();


    this.resourceOffersEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.ResourceOffersEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.resourceOffersEventDisruptor.handleEventsWith(injector.getInstance(ResourceOffersEventHandler.class));
    this.resourceOffersEventDisruptor.start();

    this.offerRescindedEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.OfferRescindedEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.offerRescindedEventDisruptor.handleEventsWith(injector.getInstance(OfferRescindedEventHandler.class));
    this.offerRescindedEventDisruptor.start();

    this.statusUpdateEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.StatusUpdateEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.statusUpdateEventDisruptor.handleEventsWith(injector.getInstance(StatusUpdateEventHandler.class));
    this.statusUpdateEventDisruptor.start();

    this.frameworkMessageEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.FrameworkMessageEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.frameworkMessageEventDisruptor.handleEventsWith(injector.getInstance(FrameworkMessageEventHandler.class));
    this.frameworkMessageEventDisruptor.start();

    this.disconnectedEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.DisconnectedEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.disconnectedEventDisruptor.handleEventsWith(injector.getInstance(DisconnectedEventHandler.class));
    this.disconnectedEventDisruptor.start();

    this.slaveLostEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.SlaveLostEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.slaveLostEventDisruptor.handleEventsWith(injector.getInstance(SlaveLostEventHandler.class));
    this.slaveLostEventDisruptor.start();

    this.executorLostEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.ExecutorLostEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.executorLostEventDisruptor.handleEventsWith(injector.getInstance(ExecutorLostEventHandler.class));
    this.executorLostEventDisruptor.start();

    this.errorEventDisruptor = new Disruptor<>(new org.apache.myriad.scheduler.event.ErrorEventFactory(), DEFAULT_LARGE_RINGBUFFER_SIZE, disruptorExecutors);
    this.errorEventDisruptor.handleEventsWith(injector.getInstance(ErrorEventHandler.class));
    this.errorEventDisruptor.start();
  }

  public Disruptor<org.apache.myriad.scheduler.event.RegisteredEvent> getRegisteredEventDisruptor() {
    return registeredEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.ReRegisteredEvent> getReRegisteredEventDisruptor() {
    return reRegisteredEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.ResourceOffersEvent> getResourceOffersEventDisruptor() {
    return resourceOffersEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.OfferRescindedEvent> getOfferRescindedEventDisruptor() {
    return offerRescindedEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.StatusUpdateEvent> getStatusUpdateEventDisruptor() {
    return statusUpdateEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.FrameworkMessageEvent> getFrameworkMessageEventDisruptor() {
    return frameworkMessageEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.DisconnectedEvent> getDisconnectedEventDisruptor() {
    return disconnectedEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.SlaveLostEvent> getSlaveLostEventDisruptor() {
    return slaveLostEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.ExecutorLostEvent> getExecutorLostEventDisruptor() {
    return executorLostEventDisruptor;
  }

  public Disruptor<org.apache.myriad.scheduler.event.ErrorEvent> getErrorEventDisruptor() {
    return errorEventDisruptor;
  }

}
