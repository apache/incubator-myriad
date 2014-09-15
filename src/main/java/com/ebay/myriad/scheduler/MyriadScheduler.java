/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.scheduler;

import java.util.List;

import javax.inject.Inject;

import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import com.ebay.myriad.DisruptorManager;
import com.ebay.myriad.configuration.MyriadConfiguration;

public class MyriadScheduler implements Scheduler {
	private DisruptorManager disruptorManager;

	@Inject
	public MyriadScheduler(final MyriadConfiguration cfg,
			final DisruptorManager disruptorManager) {
		this.disruptorManager = disruptorManager;
	}

	@Override
	public void registered(SchedulerDriver driver,
			Protos.FrameworkID frameworkId, Protos.MasterInfo masterInfo) {
		disruptorManager.getRegisteredEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setFrameworkId(frameworkId);
					event.setMasterInfo(masterInfo);
				});
	}

	@Override
	public void reregistered(SchedulerDriver driver,
			Protos.MasterInfo masterInfo) {
		disruptorManager.getReRegisteredEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setMasterInfo(masterInfo);
				});
	}

	@Override
	public void resourceOffers(SchedulerDriver driver, List<Protos.Offer> offers) {
		disruptorManager.getResourceOffersEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setOffers(offers);
				});
	}

	@Override
	public void offerRescinded(SchedulerDriver driver, Protos.OfferID offerId) {
		disruptorManager.getOfferRescindedEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setOfferId(offerId);
				});
	}

	@Override
	public void statusUpdate(SchedulerDriver driver, Protos.TaskStatus status) {
		disruptorManager.getStatusUpdateEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setStatus(status);
				});
	}

	@Override
	public void frameworkMessage(SchedulerDriver driver,
			Protos.ExecutorID executorId, Protos.SlaveID slaveId, byte[] bytes) {
		disruptorManager.getFrameworkMessageEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setBytes(bytes);
					event.setExecutorId(executorId);
					event.setSlaveId(slaveId);
				});
	}

	@Override
	public void disconnected(SchedulerDriver driver) {
		disruptorManager.getDisconnectedEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
				});
	}

	@Override
	public void slaveLost(SchedulerDriver driver, Protos.SlaveID slaveId) {
		disruptorManager.getSlaveLostEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setSlaveId(slaveId);
				});
	}

	@Override
	public void executorLost(SchedulerDriver driver,
			Protos.ExecutorID executorId, Protos.SlaveID slaveId, int exitStatus) {
		disruptorManager.getExecutorLostEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setExecutorId(executorId);
					event.setSlaveId(slaveId);
					event.setExitStatus(exitStatus);
				});
	}

	@Override
	public void error(SchedulerDriver driver, String message) {
		disruptorManager.getErrorEventDisruptor().publishEvent(
				(event, sequence) -> {
					event.setDriver(driver);
					event.setMessage(message);
				});
	}

}
