package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class StatusUpdateEventFactory implements
        EventFactory<StatusUpdateEvent> {

    @Override
    public StatusUpdateEvent newInstance() {
        return new StatusUpdateEvent();
    }

}
