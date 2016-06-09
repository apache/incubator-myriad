package org.apache.myriad.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.SchedulerDriver;
import org.junit.Test;

/**
 * Unit test for MyriadDriver class
 */
public class MyriadDriverTest {
  @Test
  public void testStart() throws Exception {
    MyriadDriver driver = new MyriadDriver(new MockSchedulerDriver());
    Status status = driver.start();
    assertEquals(Protos.Status.DRIVER_RUNNING_VALUE, status.getNumber());
  }

  @Test
  public void testAbort() throws Exception {
    MyriadDriver driver = new MyriadDriver(new MockSchedulerDriver());
    Status status = driver.abort();
    assertEquals(Protos.Status.DRIVER_ABORTED_VALUE, status.getNumber());
  }
  
  @Test
  public void testStop() throws Exception {
    MyriadDriver driver = new MyriadDriver(new MockSchedulerDriver());
    Status status = driver.stop(true);
    assertEquals(Protos.Status.DRIVER_STOPPED_VALUE, status.getNumber());
  }

  @Test
  public void testGetDriver() throws Exception {
    MyriadDriver driver = new MyriadDriver(new MockSchedulerDriver());
    SchedulerDriver sDriver = driver.getDriver();
    
    assertTrue(sDriver instanceof MockSchedulerDriver);
  }
}