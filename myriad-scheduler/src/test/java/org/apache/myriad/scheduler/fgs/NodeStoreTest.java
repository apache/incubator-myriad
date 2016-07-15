package org.apache.myriad.scheduler.fgs;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.myriad.TestObjectFactory;
import org.junit.Test;

/**
 * Unit tests for NodeStore class
 */
public class NodeStoreTest {
  NodeStore store = new NodeStore();
  SchedulerNode sNode = TestObjectFactory.getSchedulerNode(NodeId.newInstance("0.0.0.0", 8888), 2, 4096);

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