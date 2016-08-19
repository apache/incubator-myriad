package org.apache.myriad.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Unit tests for SchedulerState
 */
public class SchedulerStateTest extends BaseConfigurableTest {
  private SchedulerState sState;
  TaskID idOne;
  TaskID idTwo;
  NodeTask taskOne;
  NodeTask taskTwo;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    sState = TestObjectFactory.getSchedulerState(this.cfg);
    idOne = TaskID.newBuilder().setValue("Task1").build();
    idTwo = TaskID.newBuilder().setValue("Task2").build();
    taskOne = TestObjectFactory.getNodeTask("zero", "localhost", 0.0, 0.0, Long.valueOf(1), Long.valueOf(2));
    taskTwo = TestObjectFactory.getNodeTask("low", "localhost", 0.1, 1024.0, Long.valueOf(1), Long.valueOf(2));

    sState.addTask(idOne, taskOne);
    sState.addTask(idTwo, taskTwo);    
  }

  private void initialize() {
    if (sState.getTasks(Sets.newHashSet(idOne, idTwo)).size() == 0) {
      sState.addTask(idOne, taskOne);
      sState.addTask(idTwo, taskTwo);          
    }
  }
  
  private void zeroOutTasks() {
    sState.removeTask(idOne);
    sState.removeTask(idTwo);
  }
  
  @Test 
  public void testGetFrameworkID() throws Exception {
    assertEquals("mock-framework", sState.getFrameworkID().get().getValue());
  }
  
  @Test
  public void testAddTask() throws Exception {
    zeroOutTasks();

    sState.addTask(idOne, taskOne);
    sState.addTask(idTwo, taskTwo);
    assertEquals("zero", sState.getTask(idOne).getProfile().getName());
    assertEquals("low", sState.getTask(idTwo).getProfile().getName());
  }
  
  @Test
  public void testMakeTestActive() throws Exception {
    sState.makeTaskActive(idOne);
    sState.makeTaskActive(idTwo);
    assertTrue(sState.getActiveTasks().contains(taskOne));
    assertTrue(sState.getActiveTasks().contains(taskTwo));
  }
  
  @Test 
  public void testMakeTestPending() throws Exception {
    sState.makeTaskPending(idOne);
    sState.makeTaskPending(idTwo);
    //TODO (kjyost) determine why jobhistoryserver is pending task
    assertEquals(18, sState.getPendingTaskIds().size());
    assertTrue(sState.getPendingTaskIds().contains(idOne));
    assertTrue(sState.getPendingTaskIds().contains(idTwo));    
  }

  @Test 
  public void testMakeTestKillable() throws Exception {
    sState.makeTaskKillable(idOne);
    sState.makeTaskKillable(idTwo);
    //TODO (kjyost) determine why small node task is in test
    assertEquals(3, sState.getKillableTaskIds().size());
    
    assertTrue(sState.getKillableTaskIds().contains(idOne));  
    assertTrue(sState.getKillableTaskIds().contains(idTwo));    
  }

  @Test 
  public void testMakeTestStaging() throws Exception {
    sState.makeTaskStaging(idOne);
    sState.makeTaskStaging(idTwo);
    assertEquals(2, sState.getStagingTasks().size());
    assertTrue(sState.getStagingTasks().contains(taskOne));
    assertTrue(sState.getStagingTasks().contains(taskTwo));    
  }

  @Test 
  public void testMakeTestLost() throws Exception {
    sState.makeTaskLost(idOne);
    sState.makeTaskLost(idTwo);
    assertEquals(2, sState.getLostTaskIds().size());
    assertTrue(sState.getLostTaskIds().contains(idOne));
    assertTrue(sState.getLostTaskIds().contains(idTwo));    
  }  

  @Test
  public void testRemoveTask() throws Exception {
    initialize();
    sState.removeTask(idOne);
    assertNull(sState.getTask(idOne));
    sState.removeTask(idTwo);
    assertNull(sState.getTask(idTwo));
  }
}