package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

/**
 * executor lost event factory
 */
public class ExecutorLostEventFactory implements
        EventFactory<ExecutorLostEvent> {

    @Override
    public ExecutorLostEvent newInstance() {
        return new ExecutorLostEvent();
    }

}
