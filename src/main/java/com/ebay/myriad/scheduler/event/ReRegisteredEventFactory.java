package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * Mesos re-register event factory
 */
public class ReRegisteredEventFactory implements
        EventFactory<ReRegisteredEvent> {

    @Override
    public ReRegisteredEvent newInstance() {
        return new ReRegisteredEvent();
    }
}
