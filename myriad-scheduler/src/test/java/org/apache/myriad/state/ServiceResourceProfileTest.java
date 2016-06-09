package org.apache.myriad.state;

import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test cases for ServiceResourceProfile
 *
 */
public class ServiceResourceProfileTest {
  ServiceResourceProfile profile;

  @Before
  public void setUp() throws Exception {
    profile = new ServiceResourceProfile("ServiceResourceProfile", 0.1, 1024.0, 0.1, 512.0);
  }

  @Test
  public void testRequestedResources() throws Exception {
    Assert.assertEquals(new Double(0.1), profile.getCpus());
    Assert.assertEquals(new Double(1024.0), profile.getMemory());
    Assert.assertEquals(new Double(0.1), profile.getExecutorCpu());
    Assert.assertEquals(new Double(512.0), profile.getExecutorMemory());
  }
  
  @Test
  public void testName() throws Exception {
    Assert.assertEquals("ServiceResourceProfile", profile.getName());
  }
}