package org.apache.myriad.scheduler.fgs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.hadoop.net.NodeBase;
import org.apache.hadoop.yarn.api.records.impl.pb.NodeIdPBImpl;
import org.apache.hadoop.yarn.api.records.impl.pb.ResourcePBImpl;
import org.apache.hadoop.yarn.proto.YarnProtos.NodeIdProto;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeImpl;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.mesos.Protos.Offer;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.state.MockRMContext;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for OfferLifeCycleManager
 */
public class OfferLifeCycleManagerTest {
  OfferLifecycleManager manager;

  @Before
  public void setUp() throws Exception {
    NodeStore store = new NodeStore();
    NodeIdProto nodeId = NodeIdProto.newBuilder().setHost("localhost").setPort(8000).build();
    RMNode rmNode = new RMNodeImpl(new NodeIdPBImpl(nodeId), new MockRMContext(), "localhost", 8000, 8070, new NodeBase(),
            new ResourcePBImpl(), "1.0");
    SchedulerNode node = new FiCaSchedulerNode(rmNode, false);
    store.add(node);
    manager = new OfferLifecycleManager(store, new MyriadDriver(new MockSchedulerDriver()));
  }
  
  @Test
  public void testAddOffers() throws Exception {
    manager.addOffers(TestObjectFactory.getOffer("localhost", "slave-1", "mock-framework", "offer-1", 0.0, 0.0));
    assertNotNull(manager.getOfferFeed("localhost").poll());
  }

  @Test
  public void testMarkAsConsumed() throws Exception {
    Offer offer = TestObjectFactory.getOffer("localhost-1", "slave-2", "mock-framework", "consumed-offer-1", 1.0, 1024.0);
    manager.addOffers(offer);
    manager.markAsConsumed(offer);
    ConsumedOffer cOffers = manager.getConsumedOffer("localhost-1");
    Offer cOffer = cOffers.getOffers().get(0);
    assertEquals(offer, cOffer);
  }
}