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
package org.apache.myriad.state;

import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

/**
 * Unit test cases for NodeTask
 */
public class NodeTaskTest {
  NodeTask task;

  @Before
  public void setUp() throws Exception {
    TreeMap<String, Long> ports = new TreeMap<>();
    task = new NodeTask(new ServiceResourceProfile("profile", 0.1, 1024.0, ports), new LikeConstraint("hostname", "host-[0-9]*.example.com"));
    
    task.setHostname("localhost");
    task.setTaskPrefix("prefix");
    task.setProfile(new ServiceResourceProfile("ServiceResourceProfile", 0.1, 1024.0, ports));
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