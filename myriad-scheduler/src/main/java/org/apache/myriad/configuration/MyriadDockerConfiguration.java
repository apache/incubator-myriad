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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myriad.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MyriadDockerConfiguration
 * Provides deserialization support for dockerInfo data stored in yaml config
 */
public class MyriadDockerConfiguration {
  @JsonProperty
  @NotEmpty
  String image;

  @JsonProperty
  String network;

  @JsonProperty
  Boolean privledged;

  @JsonProperty
  Boolean forcePullImage;

  @JsonProperty
  List<Map<String, String>> parameters;

  public String getImage() {
    return image;
  }

  public Boolean getForcePullImage() {
    return forcePullImage != null ? forcePullImage : false;
  }

  public String getNetwork() {
    return network != null ? network : "HOST";
  }

  public Boolean getPrivledged() {
    return privledged != null ? privledged : false;
  }

  public List<Map<String, String>> getParameters() {
    if (parameters == null) {
      return new ArrayList<>();
    } else {
      return parameters;
    }
  }
}