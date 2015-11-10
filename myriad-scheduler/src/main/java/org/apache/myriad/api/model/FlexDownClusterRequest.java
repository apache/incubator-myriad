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
package org.apache.myriad.api.model;

import com.google.gson.Gson;
import java.util.List;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Flex down request parameters
 */
public class FlexDownClusterRequest {

  @NotEmpty
  public String profile;

  @NotEmpty
  public Integer instances;

  public List<String> constraints;

  public FlexDownClusterRequest() {
  }

  public FlexDownClusterRequest(String profile, Integer instances, List<String> constraints) {
    this.instances = instances;
    this.profile = profile;
    this.constraints = constraints;
  }

  public Integer getInstances() {
    return instances;
  }

  public void setInstances(Integer instances) {
    this.instances = instances;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public List<String> getConstraints() {
    return constraints;
  }

  public void setConstraints(List<String> constraints) {
    this.constraints = constraints;
  }

  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
