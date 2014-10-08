package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class OfferRescindedEventFactory implements
        EventFactory<OfferRescindedEvent> {

    @Override
    public OfferRescindedEvent newInstance() {
        return new OfferRescindedEvent();
    }
}
