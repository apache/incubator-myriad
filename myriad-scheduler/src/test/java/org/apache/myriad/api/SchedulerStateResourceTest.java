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
package org.apache.myriad.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.api.model.GetSchedulerStateResponse;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

/**
 * Unit tests for SchedulerStateResource
 */
public class SchedulerStateResourceTest extends BaseConfigurableTest {
  SchedulerStateResource resource;
  TaskID idOne, idTwo, idThree;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    resource = new SchedulerStateResource(cfg, getSchedulerState());
  }

  private SchedulerState getSchedulerState() throws Exception {
    SchedulerState state = new SchedulerState(new MyriadFileSystemRMStateStore());
    idOne = Protos.TaskID.newBuilder().setValue("nt-1").build();
    idTwo = Protos.TaskID.newBuilder().setValue("nt-2").build();
    idThree = Protos.TaskID.newBuilder().setValue("nt-3").build();
    TreeMap<String, Long> ports = new TreeMap<>();

    state.addTask(idOne, new NodeTask(new ServiceResourceProfile("profile1", 0.2, 1024.0, ports), new LikeConstraint("localhost", "host-[0-9]*.example.com")));
    state.addTask(idTwo, new NodeTask(new ServiceResourceProfile("profile2", 0.4, 2048.0, ports), new LikeConstraint("localhost", "host-[0-9]*.example.com")));
    state.addTask(idThree, new NodeTask(new ServiceResourceProfile("profile3", 0.6, 3072.0, ports), new LikeConstraint("localhost", "host-[0-9]*.example.com")));

    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    state.makeTaskActive(idOne);
    state.makeTaskPending(idTwo);
    state.makeTaskStaging(idThree);

    return state;
  }
  
  @Test
  public void test() throws Exception {
    GetSchedulerStateResponse response = resource.getState();
    assertNotNull(response);
    assertEquals(1, response.getActiveTasks().size());
    assertEquals(1, response.getPendingTasks().size());
    assertEquals(1, response.getStagingTasks().size());
  }
}