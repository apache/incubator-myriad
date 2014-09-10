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
package com.ebay.myriad.configuration;

import io.dropwizard.Configuration;

import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

public class MyriadConfiguration extends Configuration {
	/**
	 * By default framework checkpointing is turned off.
	 */
	public static final Boolean DEFAULT_CHECKPOINT = false;

	/**
	 * By default rebalancer is turned off.
	 */
	public static final Boolean DEFAULT_REBALANCER = false;

	/**
	 * By default framework failover timeout is 1 day.
	 */
	public static final Double DEFAULT_FRAMEWORK_FAILOVER_TIMEOUT_MS = 86400000.0;

	public static final String DEFAULT_FRAMEWORK_NAME = "Myriad Scheduler";

	@JsonProperty
	private @NotEmpty String mesosMaster;

	@JsonProperty
	private Boolean checkpoint;

	@JsonProperty
	private Double frameworkFailoverTimeout;

	@JsonProperty
	private String frameworkName;

	@JsonProperty
	private @NotEmpty Map<String, Map<String, String>> profiles;

	@JsonProperty
	private Boolean rebalancer;

	public String getMesosMaster() {
		return mesosMaster;
	}

	public Boolean getCheckpoint() {
		return this.checkpoint != null ? checkpoint : DEFAULT_CHECKPOINT;
	}

	public Double getFrameworkFailoverTimeout() {
		return this.frameworkFailoverTimeout != null ? this.frameworkFailoverTimeout
				: DEFAULT_FRAMEWORK_FAILOVER_TIMEOUT_MS;
	}

	public String getFrameworkName() {
		return Strings.isNullOrEmpty(this.frameworkName) ? DEFAULT_FRAMEWORK_NAME
				: this.frameworkName;
	}

	public Map<String, Map<String, String>> getProfiles() {
		return profiles;
	}

	public Boolean isCheckpoint() {
		return checkpoint != null ? checkpoint : DEFAULT_CHECKPOINT;
	}

	public Boolean isRebalancer() {
		return rebalancer != null ? rebalancer : DEFAULT_REBALANCER;
	}
}