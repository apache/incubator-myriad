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
package com.ebay.myriad.client.yarn;

import com.google.gson.Gson;

public class App {

	protected String appIdNum;
	protected boolean trackingUrlIsNotReady;
	protected String trackingUrlPretty;
	protected boolean amContainerLogsExist;
	protected String applicationId;

	protected String id;
	protected String user;
	protected String name;
	protected String queue;
	protected float progress;
	protected String trackingUI;
	protected String trackingUrl;
	protected String diagnostics;
	protected long clusterId;
	protected String applicationType;
	protected String applicationTags;
	protected String finalStatus;
	protected String state;
	protected long startedTime;
	protected long finishedTime;
	protected long elapsedTime;
	protected String amContainerLogs;
	protected String amHostHttpAddress;
	protected int allocatedMB;
	protected int allocatedVCores;
	protected int runningContainers;
	protected int preemptedResourceMB;
	protected int preemptedResourceVCores;
	protected int numNonAMContainerPreempted;
	protected int numAMContainerPreempted;

	public App() {
	} // JAXB needs this

	public boolean isTrackingUrlReady() {
		return !this.trackingUrlIsNotReady;
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public String getAppId() {
		return this.id;
	}

	public String getAppIdNum() {
		return this.appIdNum;
	}

	public String getUser() {
		return this.user;
	}

	public String getQueue() {
		return this.queue;
	}

	public String getName() {
		return this.name;
	}

	public String getState() {
		return this.state;
	}

	public float getProgress() {
		return this.progress;
	}

	public String getTrackingUI() {
		return this.trackingUI;
	}

	public String getNote() {
		return this.diagnostics;
	}

	public String getFinalStatus() {
		return this.finalStatus;
	}

	public String getTrackingUrl() {
		return this.trackingUrl;
	}

	public String getTrackingUrlPretty() {
		return this.trackingUrlPretty;
	}

	public long getStartTime() {
		return this.startedTime;
	}

	public long getFinishTime() {
		return this.finishedTime;
	}

	public long getElapsedTime() {
		return this.elapsedTime;
	}

	public String getAMContainerLogs() {
		return this.amContainerLogs;
	}

	public String getAMHostHttpAddress() {
		return this.amHostHttpAddress;
	}

	public boolean amContainerLogsExist() {
		return this.amContainerLogsExist;
	}

	public long getClusterId() {
		return this.clusterId;
	}

	public String getApplicationType() {
		return this.applicationType;
	}

	public String getApplicationTags() {
		return this.applicationTags;
	}

	public int getRunningContainers() {
		return this.runningContainers;
	}

	public int getAllocatedMB() {
		return this.allocatedMB;
	}

	public int getAllocatedVCores() {
		return this.allocatedVCores;
	}

	public int getPreemptedMB() {
		return preemptedResourceMB;
	}

	public int getPreemptedVCores() {
		return preemptedResourceVCores;
	}

	public int getNumNonAMContainersPreempted() {
		return numNonAMContainerPreempted;
	}

	public int getNumAMContainersPreempted() {
		return numAMContainerPreempted;
	}

	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}