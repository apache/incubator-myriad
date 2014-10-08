package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class ResourceOffersEventFactory implements
        EventFactory<ResourceOffersEvent> {

    @Override
    public ResourceOffersEvent newInstance() {
        return new ResourceOffersEvent();
    }
}
