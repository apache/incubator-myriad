package com.ebay.myriad.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * Node Manager Configuration
 */
public class NodeManagerConfiguration {
    /**
     * Allot 10% more memory to account for JVM overhead.
     */
    public static final double JVM_OVERHEAD = 0.1;

    /**
     * Default -Xmx for NodeManager JVM.
     */
    public static final double DEFAULT_JVM_MAX_MEMORY_MB = 2048;

    /**
     * Default cpu for NodeManager JVM.
     */
    public static final double DEFAULT_NM_CPUS = 1;

    /**
     * Translates to -Xmx for the NodeManager JVM.
     */
    @JsonProperty
    private Double jvmMaxMemoryMB;

    /**
     * Amount of CPU share given to NodeManger JVM. This is critical specially
     * for NodeManager auxiliary services.
     */
    @JsonProperty
    private Double cpus;

    /**
     * Translates to JAVA_OPTS for the NodeManager JVM.
     */
    @JsonProperty
    private String jvmOpts;

    /**
     * Determines if cgroups are enabled for NM or not.
     */
    @JsonProperty
    private Boolean cgroups;

    public Optional<Double> getJvmMaxMemoryMB() {
        return Optional.fromNullable(jvmMaxMemoryMB);
    }

    public Optional<String> getJvmOpts() {
        return Optional.fromNullable(jvmOpts);
    }

    public Optional<Double> getCpus() {
        return Optional.fromNullable(cpus);
    }

    public Optional<Boolean> getCgroups() {
        return Optional.fromNullable(cgroups);
    }
}
