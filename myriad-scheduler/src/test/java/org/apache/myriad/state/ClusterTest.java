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

import static org.junit.Assert.assertEquals;

import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

/**
 * Unit tests for Cluster
 */
public class ClusterTest {
  Cluster cluster;
  NodeTask task1, task2, task3;

  @Before
  public void setUp() throws Exception {
    TreeMap<String, Long> ports = new TreeMap<>();
    cluster = new Cluster();
    cluster.setClusterName("test-cluster");
    cluster.setMinQuota(5.0);
    cluster.setResourceManagerHost("localhost");
    cluster.setResourceManagerPort("8192");

    task1 = new NodeTask(new ServiceResourceProfile("profile1", 0.1, 1024.0, ports), new LikeConstraint("hostname1", "host-[0-9]*.example1.com"));
    task2 = new NodeTask(new ServiceResourceProfile("profile2", 0.2, 1024.0, ports), new LikeConstraint("hostname2", "host-[0-9]*.example2.com"));
    task3 = new NodeTask(new ServiceResourceProfile("profile3", 0.3, 1024.0, ports), new LikeConstraint("hostname3", "host-[0-9]*.example3.com"));
  }

  private void resetCluster() throws Exception {
    cluster.removeAllNodes();
  }

  @Test
  public void testCoreAttributes() throws Exception {
    assertEquals("test-cluster", cluster.getClusterName());
    assertEquals(5.0, cluster.getMinQuota(), 0.0001);
    assertEquals("localhost", cluster.getResourceManagerHost());
    assertEquals("8192", cluster.getResourceManagerPort());  
  }

  @Test
  public void testAddNode() throws Exception {
    resetCluster();
    cluster.addNode(task1);
    assertEquals(1, cluster.getNodes().size());
    cluster.addNode(task2);
    assertEquals(2, cluster.getNodes().size());
  }

  @Test
  public void testRemoveNode() throws Exception {
    resetCluster();
    cluster.addNode(task1);
    cluster.addNode(task2);
    cluster.addNode(task3);
    cluster.removeNode(task1);
    assertEquals(2, cluster.getNodes().size());
    cluster.removeNode(task2);
    assertEquals(1, cluster.getNodes().size());
  }
}