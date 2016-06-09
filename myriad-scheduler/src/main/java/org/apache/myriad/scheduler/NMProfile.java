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

/**
 * Node Manager Profile
 */
public class NMProfile {
  private String name;

  /**
   * Number of CPU advertised to YARN Resource Manager.
   */
  private Long cpus;

  /**
   * Memory in MB advertised to YARN Resource Manager.
   */
  private Long memory;

  public NMProfile(String name, Long cpus, Long memory) {
    this.name = name;
    this.cpus = cpus;
    this.memory = memory;
  }

  public String getName() {
    return name;
  }

  public Long getCpus() {
    return cpus;
  }

  public Long getMemory() {
    return memory;
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cpus == null) ? 0 : cpus.hashCode());
    result = prime * result + ((memory == null) ? 0 : memory.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NMProfile other = (NMProfile) obj;
    if (cpus == null) {
      if (other.cpus != null) {
        return false;
      }
    } else if (!cpus.equals(other.cpus)) {
      return false;
    }
    if (memory == null) {
      if (other.memory != null) {
        return false;
      }
    } else if (!memory.equals(other.memory)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }
}