/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Unit tests for SchedulerUtils
 */
public class SchedulerUtilsTest extends BaseConfigurableTest {
  private SchedulerState sState;
  private TaskID idOne, idTwo, idThree;
  private NodeTask taskOne, taskTwo, taskThree;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    sState = TestObjectFactory.getSchedulerState(this.cfg, "tmp/scheduler-utils-test");
    idOne = TaskID.newBuilder().setValue("Task1").build();
    idTwo = TaskID.newBuilder().setValue("Task2").build();
    idThree = TaskID.newBuilder().setValue("Task3").build();
    taskOne = TestObjectFactory.getNodeTask("zero", "server1", 0.0, 0.0, Long.valueOf(1), Long.valueOf(2));
    taskTwo = TestObjectFactory.getNodeTask("low", "localhost", 0.2, 1024.0, Long.valueOf(1), Long.valueOf(2));
    taskThree = TestObjectFactory.getNodeTask("medium", "localhost", 0.4, 2048.0, Long.valueOf(1), Long.valueOf(2));

    sState.addTask(idOne, taskOne);
    sState.addTask(idTwo, taskTwo);  
    sState.addTask(idThree, taskThree);
    
    this.baseStateStoreDirectory = "/tmp/scheduler-utils-test";
  }
  
  @Test
  public void testIsUniqueFilenameTrue() throws Exception {
    List<NodeTask> tasks = Lists.newArrayList(taskOne, taskTwo, taskThree);
    NodeTask newTask = TestObjectFactory.getNodeTask("medium", "server1", 0.4, 2048.0, Long.valueOf(1), Long.valueOf(2));
    Offer offer = TestObjectFactory.getOffer("server2", "slave1", "mock-framework", "offer1", 0.0, 0.0);
    assertTrue(SchedulerUtils.isUniqueHostname(offer, newTask, tasks));
  }
  
  @Test
  public void testIsUniqueFilenameFalse() throws Exception {
    List<NodeTask> tasks = Lists.newArrayList(taskOne, taskTwo, taskThree);
    NodeTask newTask = TestObjectFactory.getNodeTask("medium", "localhost", 0.4, 2048.0, Long.valueOf(1), Long.valueOf(2));
    Offer offer = TestObjectFactory.getOffer("localhost", "slave1", "mock-framework", "offer1", 0.2, 512.0);
    assertFalse(SchedulerUtils.isUniqueHostname(offer, newTask, tasks));
  }
  
  @Test
  public void testIsEligibleForFineGrainedSchedulingFalse() throws Exception {
    assertFalse(SchedulerUtils.isEligibleForFineGrainedScaling("localhost", sState));
  }
  
  @Test
  public void testIsEligibleForFineGrainedSchedulingTrue() throws Exception {
    assertFalse(SchedulerUtils.isEligibleForFineGrainedScaling("server1", sState));
  }
}