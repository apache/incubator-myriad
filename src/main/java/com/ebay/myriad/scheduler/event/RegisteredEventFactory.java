package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class RegisteredEventFactory implements EventFactory<RegisteredEvent> {

    @Override
    public RegisteredEvent newInstance() {
        return new RegisteredEvent();
    }

}
