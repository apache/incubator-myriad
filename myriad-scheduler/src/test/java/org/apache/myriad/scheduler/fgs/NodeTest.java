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
package org.apache.myriad.scheduler.fgs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.server.resourcemanager.metrics.SystemMetricsPublisher;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FSSchedulerNode;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.state.MockRMContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Node and FSSchedulerNode classes
 */
public class NodeTest extends BaseConfigurableTest {
  private NodeStore store; 
  private RMNode nodeOne, nodeTwo;
  private FSSchedulerNode sNodeOne, sNodeTwo;
  private MockRMContext context;
  private RMContainer containerOne;
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    context = new MockRMContext();
    context.setDispatcher(TestObjectFactory.getMockDispatcher());
    context.setSystemMetricsPublisher(new SystemMetricsPublisher());
    
    nodeOne = TestObjectFactory.getRMNode("localhost-one", 8800, Resource.newInstance(1024, 2));
    nodeTwo = TestObjectFactory.getRMNode("localhost-two", 8800, Resource.newInstance(2048, 4));
    sNodeOne = new FSSchedulerNode(nodeOne, false);
    sNodeTwo = new FSSchedulerNode(nodeTwo, false);

    store = new NodeStore();
    store.add(sNodeOne);
    store.add(sNodeTwo);

    containerOne = TestObjectFactory.getRMContainer(nodeOne, context, 1, 2, 1024);
  }
  
  @Test
  public void testAllocateAndReleaseContainer() throws Exception {
    sNodeOne.allocateContainer(containerOne);
    assertEquals(1, sNodeOne.getNumContainers());
    sNodeOne.releaseContainer(containerOne.getContainer());
    assertEquals(0, sNodeOne.getNumContainers());
  }
  
  @Test
  public void testTotalCapability() throws Exception {
    assertEquals(1024, nodeOne.getTotalCapability().getMemory());
    assertEquals(2, nodeOne.getTotalCapability().getVirtualCores());
    assertEquals(2048, nodeTwo.getTotalCapability().getMemory());
    assertEquals(4, nodeTwo.getTotalCapability().getVirtualCores());
  }
  
  @Test
  public void testGetAndRemoveContainerSnapshot() throws Exception {
    sNodeOne.allocateContainer(containerOne);
    store.getNode("localhost-one").snapshotRunningContainers();
    assertEquals(1, store.getNode("localhost-one").getContainerSnapshot().size());
    store.getNode("localhost-one").removeContainerSnapshot();
    assertNull(store.getNode("localhost-one").getContainerSnapshot());
  }
}