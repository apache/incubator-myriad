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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.MyriadContainerConfiguration;
import org.apache.myriad.configuration.MyriadDockerConfiguration;
import org.apache.myriad.executor.MyriadExecutorDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * utility class for working with tasks and node manager profiles
 */
public class TaskUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(TaskUtils.class);
  private static final String CONTAINER_PATH_KEY = "containerPath";
  private static final String HOST_PATH_KEY = "hostPath";
  private static final String RW_MODE = "mode";
  private static final String PARAMETER_KEY_KEY = "key";
  private static final String PARAMETER_VALUE_KEY = "value";

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

  public Iterable<Protos.Volume> getVolumes(Iterable<Map<String, String>> volume) {
    return Iterables.transform(volume, new Function<Map<String, String>, Protos.Volume>() {
      @Nullable
      @Override
      public Protos.Volume apply(Map<String, String> map) {
        Preconditions.checkArgument(map.containsKey(HOST_PATH_KEY) && map.containsKey(CONTAINER_PATH_KEY));
        Protos.Volume.Mode mode = Protos.Volume.Mode.RO;
        if (map.containsKey(RW_MODE) && map.get(RW_MODE).toLowerCase().equals("rw")) {
          mode = Protos.Volume.Mode.RW;
        }
        return Protos.Volume.newBuilder()
            .setContainerPath(map.get(CONTAINER_PATH_KEY))
            .setHostPath(map.get(HOST_PATH_KEY))
            .setMode(mode)
            .build();
      }
    });
  }

  public Iterable<Protos.Parameter> getParameters(Iterable<Map<String, String>> params) {
    Preconditions.checkNotNull(params);
    return Iterables.transform(params, new Function<Map<String, String>, Protos.Parameter>() {
      @Override
      public Protos.Parameter apply(Map<String, String> parameter) {
        Preconditions.checkNotNull(parameter, "Null parameter");
        Preconditions.checkState(parameter.containsKey(PARAMETER_KEY_KEY), "Missing key");
        Preconditions.checkState(parameter.containsKey(PARAMETER_VALUE_KEY), "Missing value");
        return Protos.Parameter.newBuilder()
            .setKey(parameter.get(PARAMETER_KEY_KEY))
            .setValue(PARAMETER_VALUE_KEY)
            .build();
      }
    });
  }

  private Protos.ContainerInfo.DockerInfo getDockerInfo(MyriadDockerConfiguration dockerConfiguration) {
    Preconditions.checkArgument(dockerConfiguration.getNetwork().equals("HOST"), "Currently only host networking supported");
    Protos.ContainerInfo.DockerInfo.Builder dockerBuilder = Protos.ContainerInfo.DockerInfo.newBuilder()
        .setImage(dockerConfiguration.getImage())
        .setForcePullImage(dockerConfiguration.getForcePullImage())
        .setNetwork(Protos.ContainerInfo.DockerInfo.Network.valueOf(dockerConfiguration.getNetwork()))
        .setPrivileged(dockerConfiguration.getPrivledged())
        .addAllParameters(getParameters(dockerConfiguration.getParameters()));
    return dockerBuilder.build();
  }

  /**
   * Builds a ContainerInfo Object
   *
   * @return ContainerInfo
   */
  public Protos.ContainerInfo getContainerInfo() {
    Preconditions.checkArgument(cfg.getContainerInfo().isPresent(), "ContainerConfiguration doesn't exist!");
    MyriadContainerConfiguration containerConfiguration = cfg.getContainerInfo().get();
    Protos.ContainerInfo.Builder containerBuilder = Protos.ContainerInfo.newBuilder()
        .setType(Protos.ContainerInfo.Type.valueOf(containerConfiguration.getType()))
        .addAllVolumes(getVolumes(containerConfiguration.getVolumes()));
    if (containerConfiguration.getDockerInfo().isPresent()) {
      MyriadDockerConfiguration dockerConfiguration = containerConfiguration.getDockerInfo().get();
      containerBuilder.setDocker(getDockerInfo(dockerConfiguration));
    }
    return containerBuilder.build();
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
