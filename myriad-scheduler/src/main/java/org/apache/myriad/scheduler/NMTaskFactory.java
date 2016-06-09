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

import com.google.inject.Inject;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.resource.ResourceOfferContainer;
import org.apache.myriad.state.NodeTask;

import java.util.List;

/**
 * Creates Node Manager Tasks based upon Mesos offers
 */
public class NMTaskFactory extends TaskFactory {


  @Inject
  NMTaskFactory(MyriadConfiguration cfg, TaskUtils taskUtils, ExecutorCommandLineGenerator clGenerator) {
    super(cfg, taskUtils, clGenerator);
  }

  @Override
  public Protos.TaskInfo createTask(ResourceOfferContainer resourceOfferContainer, Protos.FrameworkID frameworkId, Protos.TaskID taskId, NodeTask nodeTask) {
    ServiceResourceProfile serviceProfile = nodeTask.getProfile();
    Double taskMemory = serviceProfile.getAggregateMemory();
    Double taskCpus = serviceProfile.getAggregateCpu();
    List<Protos.Resource> portResources = resourceOfferContainer.consumePorts(serviceProfile.getPorts().values());
    Protos.CommandInfo commandInfo = clGenerator.generateCommandLine(serviceProfile, null, rangesConverter(portResources));
    Protos.ExecutorInfo executorInfo = getExecutorInfoForSlave(resourceOfferContainer, frameworkId, commandInfo);
    Protos.TaskInfo.Builder taskBuilder = Protos.TaskInfo.newBuilder().setName(cfg.getFrameworkName() + "-" + taskId.getValue()).setTaskId(taskId).setSlaveId(
        resourceOfferContainer.getSlaveId());

    return taskBuilder
        .addAllResources(resourceOfferContainer.consumeCpus(taskCpus))
        .addAllResources(resourceOfferContainer.consumeMem(taskMemory))
        .addAllResources(portResources)
        .setExecutor(executorInfo)
        .build();
  }

  @Override
  public Protos.ExecutorInfo getExecutorInfoForSlave(ResourceOfferContainer resourceOfferContainer, Protos.FrameworkID frameworkId, Protos.CommandInfo commandInfo) {
    Protos.ExecutorID executorId = Protos.ExecutorID.newBuilder()
        .setValue(EXECUTOR_PREFIX + frameworkId.getValue() + resourceOfferContainer.getOfferId() +
            resourceOfferContainer.getSlaveId().getValue())
        .build();
    Protos.ExecutorInfo.Builder executorInfo = Protos.ExecutorInfo.newBuilder().setCommand(commandInfo).setName(EXECUTOR_NAME).setExecutorId(executorId)
        .addAllResources(resourceOfferContainer.consumeCpus(taskUtils.getExecutorCpus()))
        .addAllResources(resourceOfferContainer.consumeMem(taskUtils.getExecutorMemory()));
    if (cfg.getContainerInfo().isPresent()) {
      executorInfo.setContainer(getContainerInfo());
    }
    return executorInfo.build();

  }
}
