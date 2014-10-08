package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class ReRegisteredEventFactory implements
        EventFactory<ReRegisteredEvent> {

    @Override
    public ReRegisteredEvent newInstance() {
        return new ReRegisteredEvent();
    }
}
