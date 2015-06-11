package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * offer rescinded event factory
 */
public class OfferRescindedEventFactory implements
        EventFactory<OfferRescindedEvent> {

    @Override
    public OfferRescindedEvent newInstance() {
        return new OfferRescindedEvent();
    }
}
