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

import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.executor.MyriadExecutorDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * utility class for working with tasks and node manager profiles
 */
public class TaskUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskUtils.class);


  private MyriadConfiguration cfg;

  @Inject
  public TaskUtils(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }


  public double getNodeManagerMemory() {
    return cfg.getNodeManagerConfiguration().getJvmMaxMemoryMB();
  }

  public double getNodeManagerMaxCpus() {
    return cfg.getNodeManagerConfiguration().getMaxCpus();
  }

  public double getNodeManagerCpus() {
    return cfg.getNodeManagerConfiguration().getCpus();
  }

  public Map<String, Long> getNodeManagerPorts() {
    return cfg.getNodeManagerConfiguration().getPorts();
  }

  public double getExecutorCpus() {
    return MyriadExecutorDefaults.DEFAULT_CPUS;
  }

  public double getExecutorMemory() {
    return cfg.getMyriadExecutorConfiguration().getJvmMaxMemoryMB();
  }

  public TaskUtils() {
    super();
  }

  /**
   * Helper function that returns all scalar resources of a given name in an offer up to a given value.  Attempts to
   * take resource from the prescribed role first and then from the default role.  The variable used indicated any
   * resources previously requested.   Assumes enough resources are present.
   *
   * @param offer - An offer by Mesos, assumed to have enough resources.
   * @param name  - The name of the SCALAR resource, i.e. cpus or mem
   * @param value - The amount of SCALAR resources needed.
   * @param used  - The amount of SCALAR resources already removed from this offer.
   * @return An Iterable containing one or two scalar resources of a given name in an offer up to a given value.
   */
  public Iterable<Protos.Resource> getScalarResource(Protos.Offer offer, String name, Double value, Double used) {
    String role = cfg.getFrameworkRole();
    List<Protos.Resource> resources = new ArrayList<Protos.Resource>();

    double resourceDifference = 0; //used to determine the resource difference of value and the resources requested from role *
    //Find role by name, must loop through resources
    for (Protos.Resource r : offer.getResourcesList()) {
      if (r.getName().equals(name) && r.hasRole() && r.getRole().equals(role) && r.hasScalar()) {
        //Use Math.max in case used>resourceValue
        resourceDifference = Math.max(r.getScalar().getValue() - used, 0.0);
        if (resourceDifference > 0) {
          resources.add(Protos.Resource.newBuilder().setName(name).setType(Protos.Value.Type.SCALAR)
              .setScalar(Protos.Value.Scalar.newBuilder().setValue(Math.min(value, resourceDifference)).build())
              .setRole(role).build());
        }
        break;
      } else if (r.getName().equals(name) && r.hasRole() && r.getRole().equals(role)) {
        //Should never get here, there must be a miss configured slave
        LOGGER.warn("Resource with name: " + name + "expected type to be SCALAR check configuration on: " + offer.getHostname());
      }
    }
    //Assume enough resources are present in default value, if not we shouldn't have gotten to this function.
    if (value - resourceDifference > 0) {
      resources.add(Protos.Resource.newBuilder().setName(name).setType(Protos.Value.Type.SCALAR)
          .setScalar(Protos.Value.Scalar.newBuilder().setValue(value - resourceDifference).build())
          .build()); //no role assumes default
    }
    return resources;
  }
}
