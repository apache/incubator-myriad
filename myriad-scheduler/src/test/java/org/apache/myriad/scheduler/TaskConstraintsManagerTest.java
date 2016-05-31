package org.apache.myriad.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.myriad.BaseConfigurableTest;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for TaskConstraintsManager
 */
public class TaskConstraintsManagerTest extends BaseConfigurableTest {
  TaskConstraintsManager manager = new TaskConstraintsManager();
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    manager.addTaskConstraints("jobhistory", new ServiceTaskConstraints(cfg, "jobhistory"));
  }

  @Test
  public void testAddConstraints() throws Exception {
    assertTrue(manager.exists("jobhistory"));
  }

  @Test
  public void testGetConstraints() throws Exception {
    TaskConstraints tCon = manager.getConstraints("jobhistory");
    assertEquals(3, tCon.portsCount());
  }
}