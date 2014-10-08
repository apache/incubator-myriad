package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class FrameworkMessageEventFactory implements
        EventFactory<FrameworkMessageEvent> {

    @Override
    public FrameworkMessageEvent newInstance() {
        return new FrameworkMessageEvent();
    }

}
