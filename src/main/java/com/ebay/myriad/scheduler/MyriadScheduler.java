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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.state.NodeTask;
import com.ebay.myriad.state.SchedulerState;

public class MyriadScheduler implements Scheduler {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyriadScheduler.class);

	private final Lock driverOperationLock;

	private SchedulerState schedulerState;

	@Inject
	public MyriadScheduler(final MyriadConfiguration cfg,
			final SchedulerState schedulerState) {
		this.driverOperationLock = new ReentrantLock();
		this.schedulerState = schedulerState;
	}

	@Override
	public void registered(SchedulerDriver driver,
			Protos.FrameworkID frameworkId, Protos.MasterInfo mi) {
		LOGGER.info("Framework registered! ID = {}", frameworkId.getValue());
		schedulerState.setFrameworkId(frameworkId);
	}

	@Override
	public void reregistered(SchedulerDriver driver, Protos.MasterInfo mi) {
		LOGGER.info("Framework reregistered!");
	}

	@Override
	public void resourceOffers(SchedulerDriver driver, List<Protos.Offer> offers) {
		LOGGER.info("Received offers {}", offers.size());
		driverOperationLock.lock();
		try {
			Set<String> pendingTasks = schedulerState.getPendingTaskIds();
			if (CollectionUtils.isNotEmpty(pendingTasks)) {
				for (Offer offer : offers) {
					boolean offerMatch = false;
					for (String pendingTaskId : pendingTasks) {
						NodeTask taskToLaunch = schedulerState
								.getTask(pendingTaskId);
						NMProfile profile = taskToLaunch.getProfile();
						if (matches(offer, profile)
								&& SchedulerUtils.isUniqueHostname(offer,
										schedulerState.getActiveTasks())) {
							LOGGER.info("Offer {} matched profile {}", offer,
									profile);
							TaskInfo task = TaskUtils.createYARNTask(offer,
									taskToLaunch);
							List<OfferID> offerIds = new ArrayList<>();
							offerIds.add(offer.getId());
							List<TaskInfo> tasks = new ArrayList<>();
							tasks.add(task);
							LOGGER.info("Launching task: {}", task);
							driver.launchTasks(offerIds, tasks);
							schedulerState.makeTaskStaging(pendingTaskId);
							NodeTask taskLaunched = schedulerState
									.getTask(pendingTaskId);
							taskLaunched.setHostname(offer.getHostname());
							offerMatch = true;
							break;
						}
					}
					if (!offerMatch) {
						LOGGER.info(
								"Declining offer {}, as it didn't match any pending task.",
								offer);
						driver.declineOffer(offer.getId());
					}
				}
			} else {
				LOGGER.info("No pending tasks, declining all offers");
				offers.forEach(o -> driver.declineOffer(o.getId()));
			}
		} finally {
			driverOperationLock.unlock();
		}
	}

	@Override
	public void offerRescinded(SchedulerDriver sd, Protos.OfferID offerId) {
		LOGGER.info("Rescinded offer {}", offerId);
	}

	@Override
	public void statusUpdate(SchedulerDriver sd, Protos.TaskStatus status) {
		TaskID taskId = status.getTaskId();
		LOGGER.info("Status Update for task: {} | state: {}", taskId,
				status.getState());
		TaskState state = status.getState();

		String taskIdValue = taskId.getValue();
		switch (state) {
		case TASK_STAGING:
			schedulerState.makeTaskStaging(taskIdValue);
			break;
		case TASK_STARTING:
			schedulerState.makeTaskStaging(taskIdValue);
			break;
		case TASK_RUNNING:
			schedulerState.makeTaskActive(taskIdValue);
			NodeTask task = schedulerState.getTask(taskIdValue);
			schedulerState.releaseLock(task.getClusterId());
			break;
		case TASK_FINISHED:
			schedulerState.removeTask(taskIdValue);
			break;
		case TASK_FAILED:
			// Add to pending tasks
			schedulerState.makeTaskPending(taskIdValue);
			break;
		case TASK_KILLED:
			schedulerState.removeTask(taskIdValue);
			break;
		case TASK_LOST:
			schedulerState.makeTaskPending(taskIdValue);
			break;
		default:
			LOGGER.error("Invalid state: {}", state);
			break;
		}
	}

	@Override
	public void frameworkMessage(SchedulerDriver sd,
			Protos.ExecutorID executorId, Protos.SlaveID slaveId, byte[] bytes) {
		LOGGER.info("Received framework message from executor {} of slave {}",
				executorId, slaveId);
	}

	@Override
	public void disconnected(SchedulerDriver sd) {
		LOGGER.info("Framework disconnected!");
	}

	@Override
	public void slaveLost(SchedulerDriver sd, Protos.SlaveID slaveId) {
		LOGGER.info("Slave {} lost!", slaveId);
	}

	@Override
	public void executorLost(SchedulerDriver sd, Protos.ExecutorID executorId,
			Protos.SlaveID slaveId, int i) {
		LOGGER.info("Executor {} of slave {} lost!", executorId, slaveId);
	}

	@Override
	public void error(SchedulerDriver driver, String message) {
		LOGGER.error(message);
	}

	private boolean matches(Offer offer, NMProfile profile) {
		double cpus = -1;
		double mem = -1;

		for (Resource resource : offer.getResourcesList()) {
			if (resource.getName().equals("cpus")) {
				if (resource.getType().equals(Value.Type.SCALAR)) {
					cpus = resource.getScalar().getValue();
				} else {
					LOGGER.error("Cpus resource was not a scalar: {}", resource
							.getType().toString());
				}
			} else if (resource.getName().equals("mem")) {
				if (resource.getType().equals(Value.Type.SCALAR)) {
					mem = resource.getScalar().getValue();
				} else {
					LOGGER.error("Mem resource was not a scalar: {}", resource
							.getType().toString());
				}
			} else if (resource.getName().equals("disk")) {
				LOGGER.warn("Ignoring disk resources from offer");
			} else if (resource.getName().equals("ports")) {
				LOGGER.info("Ignoring ports resources from offer");
			} else {
				LOGGER.warn("Ignoring unknown resource type: {}",
						resource.getName());
			}
		}

		if (cpus < 0)
			LOGGER.error("No cpus resource present");
		if (mem < 0)
			LOGGER.error("No mem resource present");

		Map<String, String> requestAttributes = new HashMap<String, String>();

		if (profile.getCpus() <= cpus
				&& profile.getMemory() <= mem
				&& SchedulerUtils.isMatchSlaveAttributes(offer,
						requestAttributes)) {
			return true;
		} else {
			LOGGER.info("Offer not sufficient for profile: " + profile);
			return false;
		}
	}

}
