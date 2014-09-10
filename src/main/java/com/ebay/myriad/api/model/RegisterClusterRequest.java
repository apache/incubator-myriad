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

import org.hibernate.validator.constraints.NotEmpty;

import com.google.gson.Gson;

public class RegisterClusterRequest {
	@NotEmpty
	private String clusterName;
	@NotEmpty
	private String resourceManagerHost;
	@NotEmpty
	private String resourceManagerPort;

	public RegisterClusterRequest() {
	}

	public RegisterClusterRequest(String clusterName,
			String resourceManagerHost, String resourceManagerPort) {
		super();
		this.clusterName = clusterName;
		this.resourceManagerHost = resourceManagerHost;
		this.resourceManagerPort = resourceManagerPort;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getResourceManagerHost() {
		return resourceManagerHost;
	}

	public void setResourceManagerHost(String resourceManagerHost) {
		this.resourceManagerHost = resourceManagerHost;
	}

	public String getResourceManagerPort() {
		return resourceManagerPort;
	}

	public void setResourceManagerPort(String resourceManagerPort) {
		this.resourceManagerPort = resourceManagerPort;
	}

	public String toString() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
