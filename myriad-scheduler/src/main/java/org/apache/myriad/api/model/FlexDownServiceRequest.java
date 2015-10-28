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

package org.apache.myriad.api.model;

import com.google.gson.Gson;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Flex down an auxtask/service
 */
public class FlexDownServiceRequest {

  @NotEmpty
  public Integer instances;

  @NotEmpty
  public String serviceName;

  public FlexDownServiceRequest() {
  }

  public FlexDownServiceRequest(Integer instances, String serviceName) {
    this.instances = instances;
    this.serviceName = serviceName;
  }

  public Integer getInstances() {
    return instances;
  }

  public void setInstances(Integer instances) {
    this.instances = instances;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
