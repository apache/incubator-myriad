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
package org.apache.myriad.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.junit.Test;

/**
 * Unit tests for SchedulerState
 */
public class SchedulerStateTest extends BaseConfigurableTest {
  private NodeTask taskOne;
  private NodeTask taskTwo;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.baseStateStoreDirectory = "/tmp/scheduler-state-test";
  }

  private SchedulerState initialize() throws Exception {
    resetStoreState();
    taskOne = TestObjectFactory.getNodeTask("zero", "localhost", 0.0, 0.0, Long.valueOf(1), Long.valueOf(2));
    taskTwo = TestObjectFactory.getNodeTask("low", "localhost", 0.1, 1024.0, Long.valueOf(1), Long.valueOf(2));
    SchedulerState sState = TestObjectFactory.getSchedulerState(this.cfg, "tmp/scheduler-state-test");
    return sState;
  }
  
  @Test 
  public void testGetFrameworkID() throws Exception {
    SchedulerState sState = initialize();
    assertEquals("mock-framework", sState.getFrameworkID().get().getValue());
  }
  
  @Test
  public void testAddTask() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.addTask(idOne, taskOne);
    sState.addTask(idTwo, taskTwo);
    assertEquals("zero", sState.getTask(idOne).getProfile().getName());
    assertEquals("low", sState.getTask(idTwo).getProfile().getName());
  }
  
  @Test
  public void testMakeTestActive() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.addTask(idOne, taskOne);
    sState.addTask(idTwo, taskTwo);
    sState.makeTaskActive(idOne);
    sState.makeTaskActive(idTwo);
    assertTrue(sState.getActiveTasks().contains(taskOne));
    assertTrue(sState.getActiveTasks().contains(taskTwo));
  }
  
  @Test 
  public void testMakeTestPending() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.makeTaskPending(idOne);
    sState.makeTaskPending(idTwo);
    assertEquals(2, sState.getPendingTaskIds().size());
    assertTrue(sState.getPendingTaskIds().contains(idOne));
    assertTrue(sState.getPendingTaskIds().contains(idTwo));    
  }

  @Test 
  public void testMakeTestKillable() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.makeTaskKillable(idOne);
    sState.makeTaskKillable(idTwo);
    assertEquals(2, sState.getKillableTaskIds().size());
    assertTrue(sState.getKillableTaskIds().contains(idOne));  
    assertTrue(sState.getKillableTaskIds().contains(idTwo));    
  }

  @Test 
  public void testMakeTestStaging() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.addTask(idOne, taskOne);
    sState.addTask(idTwo, taskTwo);
    sState.makeTaskStaging(idOne);
    sState.makeTaskStaging(idTwo);
    assertEquals(2, sState.getStagingTasks().size());
    assertTrue(sState.getStagingTasks().contains(taskOne));
    assertTrue(sState.getStagingTasks().contains(taskTwo));    
  }

  @Test 
  public void testMakeTestLost() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.makeTaskLost(idOne);
    sState.makeTaskLost(idTwo);
    assertEquals(2, sState.getLostTaskIds().size());
    assertTrue(sState.getLostTaskIds().contains(idOne));
    assertTrue(sState.getLostTaskIds().contains(idTwo));    
  }  

  @Test
  public void testRemoveTask() throws Exception {
    SchedulerState sState = initialize();
    TaskID idOne = TaskID.newBuilder().setValue("Task1").build();
    TaskID idTwo = TaskID.newBuilder().setValue("Task2").build();
    sState.removeTask(idOne);
    assertNull(sState.getTask(idOne));
    sState.removeTask(idTwo);
    assertNull(sState.getTask(idTwo));
  }
}