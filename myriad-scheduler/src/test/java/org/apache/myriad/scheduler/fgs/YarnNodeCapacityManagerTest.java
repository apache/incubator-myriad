package org.apache.myriad.scheduler.fgs;

import static org.junit.Assert.assertEquals;

import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.server.resourcemanager.metrics.SystemMetricsPublisher;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FSSchedulerNode;
import org.apache.mesos.Protos.Offer;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.TaskUtils;
import org.apache.myriad.scheduler.yarn.MyriadFairScheduler;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.state.MockRMContext;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Contains test cases for YarnNodeCapacityManager
 */
public class YarnNodeCapacityManagerTest extends BaseConfigurableTest {
  private YarnNodeCapacityManager manager;
  private NodeStore store; 
  private RMNode nodeOne, nodeTwo;
  private FSSchedulerNode sNodeOne, sNodeTwo;
  private SchedulerState state;
  private MockRMContext context;
  private RMContainer containerOne;
  private OfferLifecycleManager olManager;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    context = new MockRMContext();
    context.setDispatcher(TestObjectFactory.getMockDispatcher());
    context.setSystemMetricsPublisher(new SystemMetricsPublisher());
    
    nodeOne = TestObjectFactory.getRMNode("localhost-one", 8800, Resource.newInstance(2048, 4));
    nodeTwo = TestObjectFactory.getRMNode("localhost-two", 8800, Resource.newInstance(1024, 2));
    sNodeOne = new FSSchedulerNode(nodeOne, false);
    sNodeTwo = new FSSchedulerNode(nodeTwo, false);

    containerOne = TestObjectFactory.getRMContainer(nodeOne, context, 1, 2, 1024);
    store = new NodeStore();
    store.add(sNodeOne);
    store.add(sNodeTwo);
    
    MyriadDriver driver = TestObjectFactory.getMyriadDriver(new MockSchedulerDriver());
    olManager = new OfferLifecycleManager(store, driver);
    state = TestObjectFactory.getSchedulerState(new MyriadConfiguration());
    MyriadFairScheduler scheduler = TestObjectFactory.getMyriadFairScheduler(context);
    
    scheduler.addNode(sNodeOne);
    scheduler.addNode(sNodeTwo);
    manager = new YarnNodeCapacityManager(new CompositeInterceptor(), scheduler, 
              context, driver, olManager, store, state, new TaskUtils(this.cfg));
  }
    
  @Test
  public void testIncrementNodeCapacity() throws Exception {
    manager.incrementNodeCapacity(nodeTwo, Resource.newInstance(2048, 4));
    assertEquals(3072, nodeTwo.getTotalCapability().getMemory());
    assertEquals(6, nodeTwo.getTotalCapability().getVirtualCores());
  }
  
  @Test
  public void testDecrementNodeCapacity() throws Exception {
    manager.decrementNodeCapacity(nodeOne, Resource.newInstance(1024, 2));
    assertEquals(1024, nodeOne.getTotalCapability().getMemory());
    assertEquals(2, nodeOne.getTotalCapability().getVirtualCores());
  }
  
  @Test
  public void testHandleContainerAllocation() throws Exception {
    Offer offer = TestObjectFactory.getOffer("zero-localhost-one", "slave-one", "mock-framework", "offer-one", 0.1, 512.0);
    sNodeOne.allocateContainer(containerOne);
    NodeTask task = TestObjectFactory.getNodeTask("small", "localhost-one", Double.valueOf(0.1), Double.valueOf(512.0), 
        Long.parseLong("1"), Long.parseLong("256"));
    state.addNodes(Lists.newArrayList(task));
    olManager.addOffers(offer); 
    olManager.markAsConsumed(offer);
    manager.handleContainerAllocation(nodeOne);
    store.getNode("localhost-one").snapshotRunningContainers();
    assertEquals(1, store.getNode("localhost-one").getNode().getRunningContainers().size());
    assertEquals(1, store.getNode("localhost-one").getContainerSnapshot().size());
  }
}