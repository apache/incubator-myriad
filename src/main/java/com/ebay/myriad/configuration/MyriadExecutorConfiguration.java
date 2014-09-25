package com.ebay.myriad.configuration;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class MyriadExecutorConfiguration {
	/**
	 * Translates to -Xmx for the NodeManager JVM.
	 */
	@JsonProperty
	private Double jvmMaxMemoryMB;

	@JsonProperty
	@NotEmpty
	private String path;

	public Optional<Double> getJvmMaxMemoryMB() {
		return Optional.fromNullable(jvmMaxMemoryMB);
	}

	public String getPath() {
		return path;
	}

}
