package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * Factory for error events
 */
public class ErrorEventFactory implements EventFactory<ErrorEvent> {

    @Override
    public ErrorEvent newInstance() {
        return new ErrorEvent();
    }

}
