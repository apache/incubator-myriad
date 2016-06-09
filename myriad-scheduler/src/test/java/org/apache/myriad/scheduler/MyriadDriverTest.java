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