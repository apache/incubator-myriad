/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Configuration for the Executor
 */
public class MyriadExecutorConfiguration {
    /**
     * Translates to -Xmx for the NodeManager JVM.
     */
    @JsonProperty
    private Double jvmMaxMemoryMB;

    @JsonProperty
    @NotEmpty
    private String path;

    @JsonProperty
    private String nodeManagerUri;

    public Optional<Double> getJvmMaxMemoryMB() {
        return Optional.fromNullable(jvmMaxMemoryMB);
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getNodeManagerUri() {
        return Optional.fromNullable(nodeManagerUri);
    }
}
