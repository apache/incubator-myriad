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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.myriad.TestObjectFactory;
import org.junit.Test;

/**
 * Unit tests for NodeStore class
 */
public class NodeStoreTest {
  NodeStore store = new NodeStore();
  SchedulerNode sNode = TestObjectFactory.getSchedulerNode("0.0.0.0", 8888, 2, 4096);

  @Test
  public void testAddNode() throws Exception {
    store.add(sNode);
    assertTrue(store.isPresent("0.0.0.0"));
    assertNotNull(store.getNode("0.0.0.0"));
  }

  @Test
  public void testRemoveNode() throws Exception {
    if (!store.isPresent("0.0.0.0")) {
      store.add(sNode);
    }
    store.remove("0.0.0.0");
    assertFalse(store.isPresent("0.0.0.0"));
    assertNull(store.getNode("0.0.0.0"));
  }
}