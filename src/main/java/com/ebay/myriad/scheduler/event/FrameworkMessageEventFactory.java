package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * framework message event factory
 */
public class FrameworkMessageEventFactory implements
        EventFactory<FrameworkMessageEvent> {

    @Override
    public FrameworkMessageEvent newInstance() {
        return new FrameworkMessageEvent();
    }

}
