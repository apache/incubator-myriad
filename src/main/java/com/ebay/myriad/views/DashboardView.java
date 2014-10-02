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
package com.ebay.myriad.views;


import java.util.Collection;

import com.ebay.myriad.state.Cluster;
import com.ebay.myriad.state.SchedulerState;

public class DashboardView {
	public static final String TEMPLATE_NAME = "master.mustache";
	public SchedulerState schedulerState;

	public DashboardView(SchedulerState schedulerState) {
		this.schedulerState = schedulerState;
	}

	public Collection<String> getPendingTasks() {
		return this.schedulerState.getPendingTaskIds();
	}

	public Collection<String> getStagingTasks() {
		return this.schedulerState.getStagingTaskIds();
	}

	public Collection<String> getKillableTasks() {
		return this.schedulerState.getKillableTasks();
	}

	public Collection<Cluster> getClusters() {
		return this.schedulerState.getClusters().values();
	}

	public Collection<String> getActiveTasks() {
		return this.schedulerState.getActiveTaskIds();
	}
}
