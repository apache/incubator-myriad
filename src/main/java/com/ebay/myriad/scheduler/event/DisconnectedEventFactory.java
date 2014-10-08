package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class DisconnectedEventFactory implements
        EventFactory<DisconnectedEvent> {

    @Override
    public DisconnectedEvent newInstance() {
        return new DisconnectedEvent();
    }

}
