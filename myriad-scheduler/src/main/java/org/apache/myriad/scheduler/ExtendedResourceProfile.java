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
package org.apache.myriad.scheduler;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Extended ServiceResourceProfile for services that need to pass set of resources downstream
 * currently the only such service is NodeManager
 */
public class ExtendedResourceProfile extends ServiceResourceProfile {

  private NMProfile childProfile;

  /**
   * @param childProfile - should be null
   * @param cpu
   * @param mem          will throw NullPoiterException if childProfile is null
   */
  public ExtendedResourceProfile(NMProfile childProfile, Double cpu, Double mem, Map<String, Long> ports) {
    super(childProfile.getName(), cpu, mem, ports);
    this.childProfile = childProfile;
    this.className = ExtendedResourceProfile.class.getName();
  }

  @Override
  public String getName() {
    return childProfile.getName();
  }

  @Override
  public Double getCpus() {
    return childProfile.getCpus().doubleValue();
  }

  @Override
  public Double getMemory() {
    return childProfile.getMemory().doubleValue();
  }

  @Override
  public Double getAggregateMemory() {
    return memory + childProfile.getMemory();
  }

  @Override
  public Double getAggregateCpu() {
    return cpus + childProfile.getCpus();
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((childProfile == null) ? 0 : childProfile.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    ExtendedResourceProfile other = (ExtendedResourceProfile) obj;

    if (childProfile == null) {
      if (other.childProfile != null) {
        return false;
      }
    } else if (!childProfile.equals(other.childProfile)) {
      return false;
    }
    return true;
  }
}