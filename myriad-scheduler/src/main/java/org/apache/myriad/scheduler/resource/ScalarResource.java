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
package org.apache.myriad.scheduler.resource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable POJO for handling Scalar Resources
 */
@VisibleForTesting
class ScalarResource {
  double defaultValue = 0.0;
  double roleValue = 0.0;
  String name;
  String role;

  public ScalarResource(String name, String role) {
    this.name = name;
    this.role = role;
  }

  public void incrementValue(Double value, Boolean role) {
    if (role) {
      roleValue += value;
    } else {
      defaultValue += value;
    }
  }

  public Double getTotalValue() {
    return defaultValue + roleValue;
  }

  public Boolean satisfies(Double value) {
    return defaultValue + roleValue >= value;
  }

  public List<Protos.Resource> consumeResource(Double value) {
    Preconditions.checkState(roleValue + defaultValue >= value, String.format("%s value requested: %f, greater " +
        "than amount held %f", name, value, roleValue + defaultValue));
    List<Protos.Resource> resources = new ArrayList<>();
    if (roleValue >= value) {
      roleValue -= value;
      resources.add(createResource(name, value, true));
    } else if (roleValue + defaultValue >= value && roleValue > 0) {
      resources.add(createResource(name, roleValue, true));
      resources.add(createResource(name, value - roleValue, false));
      defaultValue -= (value - roleValue);
      roleValue = 0;
    } else if (roleValue + defaultValue >= value) {
      resources.add(createResource(name, value, false));
      defaultValue -= value;
    }
    return resources;
  }

  private Protos.Resource createResource(String name, Double value, boolean withRole) {
    Protos.Resource.Builder builder = Protos.Resource.newBuilder()
        .setName(name)
        .setScalar(Protos.Value.Scalar.newBuilder().setValue(value))
        .setType(Protos.Value.Type.SCALAR);
    if (withRole) {
      builder.setRole(role);
    }
    return builder.build();
  }
}
