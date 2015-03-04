package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * mesos slave lost event factory
 */
public class SlaveLostEventFactory implements EventFactory<SlaveLostEvent> {

    @Override
    public SlaveLostEvent newInstance() {
        return new SlaveLostEvent();
    }

}
