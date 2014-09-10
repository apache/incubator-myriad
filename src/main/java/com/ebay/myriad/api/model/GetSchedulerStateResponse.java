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
package com.ebay.myriad.api.model;

import java.util.Set;

public class GetSchedulerStateResponse {
	private Set<String> pendingTasks;
	private Set<String> stagingTasks;
	private Set<String> activeTasks;
	private Set<String> killableTasks;

	public GetSchedulerStateResponse() {

	}

	public GetSchedulerStateResponse(Set<String> pendingTasks,
			Set<String> stagingTasks, Set<String> activeTasks,
			Set<String> killableTasks) {
		super();
		this.pendingTasks = pendingTasks;
		this.stagingTasks = stagingTasks;
		this.activeTasks = activeTasks;
		this.killableTasks = killableTasks;
	}

	public Set<String> getPendingTasks() {
		return pendingTasks;
	}

	public Set<String> getStagingTasks() {
		return stagingTasks;
	}

	public Set<String> getActiveTasks() {
		return activeTasks;
	}

	public Set<String> getKillableTasks() {
		return killableTasks;
	}

}
