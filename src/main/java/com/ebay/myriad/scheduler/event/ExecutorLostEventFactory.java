package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class ExecutorLostEventFactory implements
        EventFactory<ExecutorLostEvent> {

    @Override
    public ExecutorLostEvent newInstance() {
        return new ExecutorLostEvent();
    }

}
