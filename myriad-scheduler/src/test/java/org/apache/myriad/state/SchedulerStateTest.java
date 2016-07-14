package org.apache.myriad.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Unit tests for SchedulerState
 */
public class SchedulerStateTest {
  SchedulerState state;

  @Before
  public void setUp() throws Exception {
    MyriadFileSystemRMStateStore store = TestObjectFactory.getStateStore(getConfiguration(), false);
    state = new SchedulerState(store);    
  }

  @Test
  public void testSetFrameworkID() throws Exception {
    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    assertEquals("mock-framework", state.getFrameworkID().get().getValue());
  }
  
  @Test
  public void testAddAndRemoveTask() throws Exception {
    NodeTask task1 = new NodeTask(new ServiceResourceProfile("profile1", 0.1, 1024.0), new LikeConstraint("hostname", "host-[0-9]*.example.com"));
    NodeTask task2 = new NodeTask(new ServiceResourceProfile("profile2", 0.1, 1024.0), new LikeConstraint("hostname", "host-[0-9]*.example.com"));
    TaskID id1 = TaskID.newBuilder().setValue("mock-task-1").build();
    TaskID id2 = TaskID.newBuilder().setValue("mock-task-2").build();
    
    Set<TaskID> taskIds = Sets.newHashSet(id1, id2);
    state.addTask(id1, task1);
    assertNotNull(state.getTask(id1));
    state.addTask(id2, task2);
    assertNotNull(state.getTask(id2));
    assertEquals(2, state.getTasks(taskIds).size());
    state.removeTask(id1);   
    assertEquals(1, state.getTasks(taskIds).size());
    assertNull(state.getTask(id1)); 
    state.removeTask(id2);   
    assertEquals(0, state.getTasks(taskIds).size());
    assertNull(state.getTask(id2)); 
  }

  private Configuration getConfiguration() {
    Configuration conf = new Configuration();
    conf.set("yarn.resourcemanager.fs.state-store.uri", "file:///tmp/");
    return conf;
  }
}