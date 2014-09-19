package com.ebay.myriad.scheduler.event;

import com.lmax.disruptor.EventFactory;

public class SlaveLostEventFactory implements EventFactory<SlaveLostEvent>{

	@Override
	public SlaveLostEvent newInstance() {
		return new SlaveLostEvent();
	}

}
