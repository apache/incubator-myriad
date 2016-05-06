/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import org.apache.myriad.configuration.OptionalSerializer.OptionalSerializerDouble;
import org.apache.myriad.configuration.OptionalSerializer.OptionalSerializerString;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Configuration for the Executor
 */
public class MyriadExecutorConfiguration {
  /**
   * Translates to -Xmx for the NodeManager JVM.
   */
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerDouble.class)
  private Double jvmMaxMemoryMB;

  @JsonProperty
  @NotEmpty
  private String path;

  @JsonProperty
  @JsonSerialize(using = OptionalSerializerString.class)
  private String nodeManagerUri;

  @JsonProperty
  @JsonSerialize(using = OptionalSerializerString.class)
  private String configUri;

  /**
   * Download URL for JRE.
   * Ex: jvmUri: http://www.apache.org/myriad/jre-1.8.99.tar.gz
   * If provided, Mesos fetcher will be used to download and extract it
   * inside sandbox.
   */
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerString.class)
  private String jvmUri;

  public Optional<Double> getJvmMaxMemoryMB() {
    return Optional.fromNullable(jvmMaxMemoryMB);
  }

  public String getPath() {
    return path;
  }

  public Optional<String> getNodeManagerUri() {
    return Optional.fromNullable(nodeManagerUri);
  }

  public Optional<String> getConfigUri() {
    return Optional.fromNullable(configUri);
  }

  public Optional<String> getJvmUri() {
    return Optional.fromNullable(jvmUri);
  }

}
