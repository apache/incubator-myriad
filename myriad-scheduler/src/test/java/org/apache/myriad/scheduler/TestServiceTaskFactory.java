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
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.scheduler.offer.OfferBuilder;
import org.apache.myriad.scheduler.resource.ResourceOfferContainer;
import org.apache.myriad.state.NodeTask;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests for ServiceTaskFactory Class
 */
public class TestServiceTaskFactory extends BaseConfigurableTest {
  static Protos.FrameworkID frameworkId = Protos.FrameworkID.newBuilder().setValue("test").build();

  @Test
  public void testServiceTaskFactory() {
    ServiceCommandLineGenerator clGenerator = new ServiceCommandLineGenerator(cfgWithDocker);
    TaskUtils taskUtils = new TaskUtils(cfgWithDocker);
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 10.0)
        .addScalarResource("mem", 16000)
        .addRangeResource("ports", 3400, 3410)
        .build();
    Map<String, ServiceConfiguration> stringServiceConfigurationMap = cfgWithDocker.getServiceConfigurations();
    System.out.print(stringServiceConfigurationMap);
    ServiceConfiguration serviceConfiguration = cfgWithDocker.getServiceConfigurations().get("jobhistory");
    ServiceResourceProfile profile = new ServiceResourceProfile("jobhistory", serviceConfiguration.getCpus(),
        serviceConfiguration.getJvmMaxMemoryMB(), serviceConfiguration.getPorts());
    NodeTask nodeTask = new NodeTask(profile, null);
    nodeTask.setTaskPrefix("jobhistory");
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile, null);
    System.out.print(roc.getPorts());
    ServiceTaskFactory taskFactory = new ServiceTaskFactory(cfgWithDocker, taskUtils, clGenerator);
    Protos.TaskInfo taskInfo = taskFactory.createTask(roc, frameworkId, makeTaskId("jobhistory"), nodeTask);
    assertTrue("taskInfo should have a container", taskInfo.hasContainer());
    assertFalse("The container should not have an executor", taskInfo.hasExecutor());
    Protos.ContainerInfo containerInfo = taskInfo.getContainer();
    assertTrue("There should be two volumes", containerInfo.getVolumesCount() == 2);
    assertTrue("The first volume should be read only", containerInfo.getVolumes(0).getMode().equals(Protos.Volume.Mode.RO));
    assertTrue("The first volume should be read write", containerInfo.getVolumes(1).getMode().equals(Protos.Volume.Mode.RW));
    assertTrue("There should be a docker image", containerInfo.getDocker().hasImage());
    assertTrue("The docker image should be mesos/myraid", containerInfo.getDocker().getImage().equals("mesos/myriad"));
    assertTrue("Should be using host networking", containerInfo.getDocker().getNetwork().equals(Protos.ContainerInfo.DockerInfo.Network.HOST));
    assertTrue("There should be two parameters", containerInfo.getDocker().getParametersList().size() == 2);
    assertTrue("Privledged mode should be false", containerInfo.getDocker().getPrivileged() == false);
  }

  private Protos.TaskID makeTaskId(String taskId) {
    return Protos.TaskID.newBuilder().setValue(taskId).build();
  }
}
