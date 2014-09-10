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

public class ClusterMetrics {

	protected int appsSubmitted;
	protected int appsCompleted;
	protected int appsPending;
	protected int appsRunning;
	protected int appsFailed;
	protected int appsKilled;

	protected long reservedMB;
	protected long availableMB;
	protected long allocatedMB;

	protected long reservedVirtualCores;
	protected long availableVirtualCores;
	protected long allocatedVirtualCores;

	protected int containersAllocated;
	protected int containersReserved;
	protected int containersPending;

	protected long totalMB;
	protected long totalVirtualCores;
	protected int totalNodes;
	protected int lostNodes;
	protected int unhealthyNodes;
	protected int decommissionedNodes;
	protected int rebootedNodes;
	protected int activeNodes;

	public ClusterMetrics() {
	} // JAXB needs this

	public int getAppsSubmitted() {
		return this.appsSubmitted;
	}

	public int getAppsCompleted() {
		return appsCompleted;
	}

	public int getAppsPending() {
		return appsPending;
	}

	public int getAppsRunning() {
		return appsRunning;
	}

	public int getAppsFailed() {
		return appsFailed;
	}

	public int getAppsKilled() {
		return appsKilled;
	}

	public long getReservedMB() {
		return this.reservedMB;
	}

	public long getAvailableMB() {
		return this.availableMB;
	}

	public long getAllocatedMB() {
		return this.allocatedMB;
	}

	public long getReservedVirtualCores() {
		return this.reservedVirtualCores;
	}

	public long getAvailableVirtualCores() {
		return this.availableVirtualCores;
	}

	public long getAllocatedVirtualCores() {
		return this.allocatedVirtualCores;
	}

	public int getContainersAllocated() {
		return this.containersAllocated;
	}

	public int getReservedContainers() {
		return this.containersReserved;
	}

	public int getPendingContainers() {
		return this.containersPending;
	}

	public long getTotalMB() {
		return this.totalMB;
	}

	public long getTotalVirtualCores() {
		return this.totalVirtualCores;
	}

	public int getTotalNodes() {
		return this.totalNodes;
	}

	public int getActiveNodes() {
		return this.activeNodes;
	}

	public int getLostNodes() {
		return this.lostNodes;
	}

	public int getRebootedNodes() {
		return this.rebootedNodes;
	}

	public int getUnhealthyNodes() {
		return this.unhealthyNodes;
	}

	public int getDecommissionedNodes() {
		return this.decommissionedNodes;
	}

	public void setAppsSubmitted(int appsSubmitted) {
		this.appsSubmitted = appsSubmitted;
	}

	public void setAppsCompleted(int appsCompleted) {
		this.appsCompleted = appsCompleted;
	}

	public void setAppsPending(int appsPending) {
		this.appsPending = appsPending;
	}

	public void setAppsRunning(int appsRunning) {
		this.appsRunning = appsRunning;
	}

	public void setAppsFailed(int appsFailed) {
		this.appsFailed = appsFailed;
	}

	public void setAppsKilled(int appsKilled) {
		this.appsKilled = appsKilled;
	}

	public void setReservedMB(long reservedMB) {
		this.reservedMB = reservedMB;
	}

	public void setAvailableMB(long availableMB) {
		this.availableMB = availableMB;
	}

	public void setAllocatedMB(long allocatedMB) {
		this.allocatedMB = allocatedMB;
	}

	public void setReservedVirtualCores(long reservedVirtualCores) {
		this.reservedVirtualCores = reservedVirtualCores;
	}

	public void setAvailableVirtualCores(long availableVirtualCores) {
		this.availableVirtualCores = availableVirtualCores;
	}

	public void setAllocatedVirtualCores(long allocatedVirtualCores) {
		this.allocatedVirtualCores = allocatedVirtualCores;
	}

	public void setContainersAllocated(int containersAllocated) {
		this.containersAllocated = containersAllocated;
	}

	public void setContainersReserved(int containersReserved) {
		this.containersReserved = containersReserved;
	}

	public void setContainersPending(int containersPending) {
		this.containersPending = containersPending;
	}

	public void setTotalMB(long totalMB) {
		this.totalMB = totalMB;
	}

	public void setTotalVirtualCores(long totalVirtualCores) {
		this.totalVirtualCores = totalVirtualCores;
	}

	public void setTotalNodes(int totalNodes) {
		this.totalNodes = totalNodes;
	}

	public void setLostNodes(int lostNodes) {
		this.lostNodes = lostNodes;
	}

	public void setUnhealthyNodes(int unhealthyNodes) {
		this.unhealthyNodes = unhealthyNodes;
	}

	public void setDecommissionedNodes(int decommissionedNodes) {
		this.decommissionedNodes = decommissionedNodes;
	}

	public void setRebootedNodes(int rebootedNodes) {
		this.rebootedNodes = rebootedNodes;
	}

	public void setActiveNodes(int activeNodes) {
		this.activeNodes = activeNodes;
	}

	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}