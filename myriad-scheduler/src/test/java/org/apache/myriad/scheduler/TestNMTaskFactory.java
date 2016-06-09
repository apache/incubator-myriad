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
import org.apache.myriad.scheduler.offer.OfferBuilder;
import org.apache.myriad.scheduler.resource.ResourceOfferContainer;
import org.apache.myriad.state.NodeTask;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests for NMTaskFactory Class
 */
public class TestNMTaskFactory extends BaseConfigurableTest {
  static Protos.FrameworkID frameworkId = Protos.FrameworkID.newBuilder().setValue("test").build();

  @Test
  public void testNMTaskFactory() {
    NMExecutorCommandLineGenerator clGenerator = new NMExecutorCommandLineGenerator(cfgWithDocker);
    TaskUtils taskUtils = new TaskUtils(cfgWithDocker);
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 10.0)
        .addScalarResource("mem", 16000)
        .addRangeResource("ports", 3500, 3505)
        .build();
    ServiceResourceProfile profile = new ExtendedResourceProfile(new NMProfile("tooMuchCpu", 7L, 8000L), taskUtils.getNodeManagerCpus(),
        taskUtils.getNodeManagerMemory(), taskUtils.getNodeManagerPorts());
    NodeTask nodeTask = new NodeTask(profile, null);
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile, null);
    NMTaskFactory taskFactory = new NMTaskFactory(cfgWithDocker, taskUtils, clGenerator);
    Protos.TaskInfo taskInfo = taskFactory.createTask(roc, frameworkId, makeTaskId("nm.zero"), nodeTask);
    assertFalse("taskInfo should not have a container", taskInfo.hasContainer());
    assertTrue("The container should have an executor", taskInfo.hasExecutor());
    Protos.ExecutorInfo executorInfo = taskInfo.getExecutor();
    assertTrue("executorInfo should have container", executorInfo.hasContainer());
    Protos.ContainerInfo containerInfo = executorInfo.getContainer();
    assertTrue("There should be two volumes", containerInfo.getVolumesCount() == 2);
    assertTrue("The first volume should be read only", containerInfo.getVolumes(0).getMode().equals(Protos.Volume.Mode.RO));
    assertTrue("The first volume should be read write", containerInfo.getVolumes(1).getMode().equals(Protos.Volume.Mode.RW));
    assertTrue("There should be a docker image", containerInfo.getDocker().hasImage());
    assertTrue("The docker image should be mesos/myraid", containerInfo.getDocker().getImage().equals("mesos/myriad"));
    assertTrue("Should be using host networking", containerInfo.getDocker().getNetwork().equals(Protos.ContainerInfo.DockerInfo.Network.HOST));
    assertTrue("There should be two parameters", containerInfo.getDocker().getParametersList().size() == 2);
    assertTrue("Privledged mode should be false", !containerInfo.getDocker().getPrivileged());
  }

  private Protos.TaskID makeTaskId(String taskId) {
    return Protos.TaskID.newBuilder().setValue(taskId).build();
  }
}
