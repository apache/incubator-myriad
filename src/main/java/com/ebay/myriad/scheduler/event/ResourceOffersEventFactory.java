package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * resource offer event factory
 */
public class ResourceOffersEventFactory implements
        EventFactory<ResourceOffersEvent> {

    @Override
    public ResourceOffersEvent newInstance() {
        return new ResourceOffersEvent();
    }
}
