package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * Factory for creating the disconnect event
 */
public class DisconnectedEventFactory implements EventFactory<DisconnectedEvent> {

    @Override
    public DisconnectedEvent newInstance() {
        return new DisconnectedEvent();
    }

}
