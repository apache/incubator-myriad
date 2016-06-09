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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource Profile for any service
 */
public class ServiceResourceProfile {

  protected final String name;

  /**
   * Number of CPU needed to run a service
   */
  protected final Double cpus;

  /**
   * Memory in MB needed to run a service
   */
  protected final Double memory;

  protected Double executorCpu = 0.0;

  protected Double executorMemory = 0.0;

  protected String className = ServiceResourceProfile.class.getName();

  protected Map<String, Long> ports;

  public ServiceResourceProfile(String name, Double cpus, Double mem, Map<String, Long> ports) {
    this.name = name;
    this.cpus = cpus;
    this.memory = mem;
    this.ports = ports;
    this.className = ServiceResourceProfile.class.getName();
  }

  public String getName() {
    return name;
  }

  public Double getCpus() {
    return cpus;
  }

  public Double getMemory() {
    return memory;
  }

  public Double getAggregateMemory() {
    return memory;
  }

  public Double getAggregateCpu() {
    return cpus;
  }

  public Map<String, Long> getPorts() {
    return ports;
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }

  /**
   * Custom serializer to help with deserialization of class hierarchy
   * since at the point of deserialization we don't know class of the data
   * that is being deserialized
   */
  public static class CustomDeserializer implements JsonDeserializer<ServiceResourceProfile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDeserializer.class);

    @Override
    public ServiceResourceProfile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      String type = json.getAsJsonObject().get("className").getAsString();
      try {
        @SuppressWarnings("rawtypes") Class c = Class.forName(type);
        if (ServiceResourceProfile.class.equals(c)) {
          return new Gson().fromJson(json, typeOfT);
        }
        ServiceResourceProfile profile = context.deserialize(json, c);
        return profile;
      } catch (ClassNotFoundException e) {
        LOGGER.error("Classname is not found", e);
      }
      return null;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + ((cpus == null) ? 0 : cpus.hashCode());
    result = prime * result + ((executorCpu == null) ? 0 : executorCpu.hashCode());
    result = prime * result + ((executorMemory == null) ? 0 : executorMemory.hashCode());
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
    ServiceResourceProfile other = (ServiceResourceProfile) obj;
    if (className == null) {
      if (other.className != null) {
        return false;
      }
    } else if (!className.equals(other.className)) {
      return false;
    }
    if (cpus == null) {
      if (other.cpus != null) {
        return false;
      }
    } else if (!cpus.equals(other.cpus)) {
      return false;
    }
    if (executorCpu == null) {
      if (other.executorCpu != null) {
        return false;
      }
    } else if (!executorCpu.equals(other.executorCpu)) {
      return false;
    }
    if (executorMemory == null) {
      if (other.executorMemory != null) {
        return false;
      }
    } else if (!executorMemory.equals(other.executorMemory)) {
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