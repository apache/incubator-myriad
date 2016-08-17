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

package org.apache.myriad.scheduler;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.MyriadContainerConfiguration;
import org.apache.myriad.configuration.MyriadDockerConfiguration;
import org.apache.myriad.scheduler.resource.ResourceOfferContainer;
import org.apache.myriad.state.NodeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base class to create Tasks based upon Mesos offers
 */
public abstract class TaskFactory {
  public static final String EXECUTOR_NAME = "myriad_task";
  public static final String EXECUTOR_PREFIX = "myriad_executor";

  protected static final Logger LOGGER = LoggerFactory.getLogger(TaskFactory.class);

  static final String YARN_RESOURCEMANAGER_HOSTNAME = "yarn.resourcemanager.hostname";
  static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";
  static final String YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS = "yarn.resourcemanager.webapp.https.address";
  static final String YARN_HTTP_POLICY = "yarn.http.policy";
  static final String YARN_HTTP_POLICY_HTTPS_ONLY = "HTTPS_ONLY";

  private static final String CONTAINER_PATH_KEY = "containerPath";
  private static final String HOST_PATH_KEY = "hostPath";
  private static final String RW_MODE = "mode";
  private static final String PARAMETER_KEY_KEY = "key";
  private static final String PARAMETER_VALUE_KEY = "value";

  protected MyriadConfiguration cfg;
  protected TaskUtils taskUtils;
  protected ExecutorCommandLineGenerator clGenerator;

  public TaskFactory() {

  }

  @Inject
  public TaskFactory(MyriadConfiguration cfg, TaskUtils taskUtils, ExecutorCommandLineGenerator clGenerator) {
    this.cfg = cfg;
    this.taskUtils = taskUtils;
    this.clGenerator = clGenerator;
  }

  public abstract Protos.TaskInfo createTask(ResourceOfferContainer resourceOfferContainer, Protos.FrameworkID frameworkId,
                                             Protos.TaskID taskId, NodeTask nodeTask);

  // TODO(Santosh): This is needed because the ExecutorInfo constructed
  // to launch NM needs to be specified to launch placeholder tasks for
  // yarn containers (for fine grained scaling).
  // If mesos supports just specifying the 'ExecutorId' without the full
  // ExecutorInfo, we wouldn't need this interface method.
  public abstract Protos.ExecutorInfo getExecutorInfoForSlave(ResourceOfferContainer resourceOfferContainer, Protos.FrameworkID frameworkId, Protos.CommandInfo commandInfo);

  protected Iterable<Protos.Volume> getVolumes(Iterable<Map<String, String>> volume) {
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

  protected Iterable<Protos.Parameter> getParameters(Iterable<Map<String, String>> params) {
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

  protected Protos.ContainerInfo.DockerInfo getDockerInfo(MyriadDockerConfiguration dockerConfiguration) {
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
  protected Protos.ContainerInfo getContainerInfo() {
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
   * Simple helper to convert Mesos Range Resource to a list of longs.
   */
  protected List<Long> rangesConverter(List<Protos.Resource> rangeResources) {
    List<Long> ret = new ArrayList<Long>();
    for (Protos.Resource range : rangeResources) {
      ret.add(range.getRanges().getRange(0).getBegin());
    }
    return ret;
  }
}
