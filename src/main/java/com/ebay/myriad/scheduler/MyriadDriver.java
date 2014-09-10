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

import javax.inject.Inject;

import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.FrameworkInfo.Builder;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.state.SchedulerState;

public class MyriadDriver {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(MyriadDriver.class);

	private final MesosSchedulerDriver driver;
	private final FrameworkInfo myriadFrameworkInfo;
	private final MyriadScheduler scheduler;

	@Inject
	public MyriadDriver(final MyriadScheduler scheduler,
			final MyriadConfiguration cfg, final SchedulerState schedulerState) {
		this.scheduler = scheduler;
		FrameworkID frameworkId = schedulerState.getFrameworkId();
		Builder frameworkInfoBuilder = FrameworkInfo.newBuilder().setUser("")
				.setName(cfg.getFrameworkName())
				.setCheckpoint(cfg.getCheckpoint())
				.setFailoverTimeout(cfg.getFrameworkFailoverTimeout());
		if (frameworkId != null) {
			frameworkInfoBuilder.setId(frameworkId);
		}
		this.myriadFrameworkInfo = frameworkInfoBuilder.build();
		this.driver = new MesosSchedulerDriver(this.scheduler,
				this.myriadFrameworkInfo, cfg.getMesosMaster());
	}

	public Status start() {
		LOGGER.info("Starting driver");
		Status status = driver.start();
		LOGGER.info("Driver started with status: {}", status);
		return status;
	}

	public Status kill(final TaskID taskId) {
		LOGGER.info("Killing task {}", taskId);
		Status status = driver.killTask(taskId);
		LOGGER.info("Task {} killed with status: {}", taskId, status);
		return status;
	}

	public Status abort() {
		LOGGER.info("Aborting driver");
		Status status = driver.abort();
		LOGGER.info("Driver aborted with status: {}", status);
		return status;
	}
}
