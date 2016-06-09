package org.apache.myriad.state;

import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test cases for NodeTask
 */
public class NodeTaskTest {
  NodeTask task;

  @Before
  public void setUp() throws Exception {    
    task = new NodeTask(new ServiceResourceProfile("profile", 0.1, 1024.0), new LikeConstraint("hostname", "host-[0-9]*.example.com"));
    
    task.setHostname("localhost");
    task.setTaskPrefix("prefix");
    task.setProfile(new ServiceResourceProfile("ServiceResourceProfile", 0.1, 1024.0, 0.1, 512.0));
  }
  
  @Test
  public void testCoreState() throws Exception {
    Assert.assertEquals("prefix", task.getTaskPrefix());
    Assert.assertEquals("localhost", task.getHostname());
  }
  
  @Test
  public void testConstraintState() throws Exception {
    Assert.assertEquals("LIKE", task.getConstraint().getType().toString());
  }
  
  @Test
  public void testServiceResourceProfileState() throws Exception {
    Assert.assertEquals(new Double(1024.0), task.getProfile().getMemory());
    Assert.assertEquals(new Double(0.1), task.getProfile().getCpus());
  }
}