package org.apache.myriad.scheduler.fgs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.impl.pb.ContainerStatusPBImpl;
import org.apache.hadoop.yarn.server.api.protocolrecords.NodeHeartbeatResponse;
import org.apache.hadoop.yarn.server.api.protocolrecords.impl.pb.NodeHeartbeatResponsePBImpl;
import org.apache.hadoop.yarn.server.api.records.NodeHealthStatus;
import org.apache.hadoop.yarn.server.api.records.impl.pb.NodeHealthStatusPBImpl;
import org.apache.hadoop.yarn.server.resourcemanager.metrics.SystemMetricsPublisher;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEventType;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FSSchedulerNode;
import org.apache.mesos.Protos.Offer;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.ServiceResourceProfile;
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
 * Contains test cases for NMHeartBeanHandler
 */
public class NMHeartBeatHandlerTest extends BaseConfigurableTest {
  private YarnNodeCapacityManager manager;
  private NMHeartBeatHandler handler;
  private NodeStore store; 
  private ServiceResourceProfile profileZero, profileSmall;
  private RMNode nodeOne, nodeTwo;
  private NodeTask nodeTaskOne, nodeTaskTwo;
  private FSSchedulerNode sNodeOne, sNodeTwo;
  private SchedulerState state;
  private MockRMContext context;
  private OfferLifecycleManager olManager;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
   
    context = new MockRMContext();
    context.setDispatcher(TestObjectFactory.getMockDispatcher());
    context.setSystemMetricsPublisher(new SystemMetricsPublisher());
    
    profileZero = TestObjectFactory.getServiceResourceProfile("zero", Double.valueOf(0.0), Double.valueOf(0.0), 
             Long.valueOf(0), Long.valueOf(0));
    profileSmall = TestObjectFactory.getServiceResourceProfile("small", Double.valueOf(2.0), Double.valueOf(2048.0), 
            Long.valueOf(1), Long.valueOf(1024));
    
    nodeOne = TestObjectFactory.getRMNode("localhost-one", 8800, Resource.newInstance(0, 0));
    nodeTwo = TestObjectFactory.getRMNode("localhost-two", 8800, Resource.newInstance(1024, 2));

    sNodeOne = new FSSchedulerNode(nodeOne, false);
    sNodeTwo = new FSSchedulerNode(nodeTwo, false);
    nodeTaskOne = TestObjectFactory.getNodeTask("localhost-one", profileZero);
    nodeTaskTwo = TestObjectFactory.getNodeTask("localhost-two", profileSmall);
    
    ConcurrentMap<NodeId, RMNode> rmNodes = new ConcurrentHashMap<NodeId, RMNode>();
    rmNodes.put(nodeOne.getNodeID(), nodeOne);
    rmNodes.put(nodeTwo.getNodeID(), nodeTwo);
    context.setRMNodes(rmNodes);
    
    store = new NodeStore();
    store.add(sNodeOne);
    store.add(sNodeTwo);
    
    MyriadDriver driver = TestObjectFactory.getMyriadDriver(new MockSchedulerDriver());
    olManager = new OfferLifecycleManager(store, driver);
    
    state = TestObjectFactory.getSchedulerState(new MyriadConfiguration());
    state.addNodes(Lists.newArrayList(nodeTaskOne, nodeTaskTwo));
    MyriadFairScheduler scheduler = TestObjectFactory.getMyriadFairScheduler(context);
   
    scheduler.addNode(sNodeOne);
    scheduler.addNode(sNodeTwo);
    
    manager = new YarnNodeCapacityManager(new CompositeInterceptor(), scheduler, 
            context, driver, olManager, store, state, new TaskUtils(this.cfg));
    handler = new NMHeartBeatHandler(new CompositeInterceptor(), scheduler, 
            driver, manager, olManager, store, state, cfg.getNodeManagerConfiguration());
  }

  @Test
  public void testZeroNodeStartedEvent() throws Exception {
    NMHeartBeatHandler.CallBackFilter filter = handler.getCallBackFilter();
    filter.allowCallBacksForNode(nodeOne.getNodeID());
    RMNodeEvent event = new RMNodeEvent(nodeOne.getNodeID(), RMNodeEventType.STARTED);
    handler.beforeRMNodeEventHandled(event, context);
    assertEquals(0, nodeOne.getTotalCapability().getVirtualCores());
    assertEquals(0, nodeOne.getTotalCapability().getMemory());
  }
  
  @Test
  public void testNonZeroNodeStartedEvent() throws Exception {
    NMHeartBeatHandler.CallBackFilter filter = handler.getCallBackFilter();
    filter.allowCallBacksForNode(nodeTwo.getNodeID());
    RMNodeEvent event = new RMNodeEvent(nodeTwo.getNodeID(), RMNodeEventType.STARTED);
    handler.beforeRMNodeEventHandled(event, context);
    /*
     * Confirm that, since fine-grained scaling does not work for non-zero nodes, the
     * capacity is set to zero for cores and memory
     */
    assertEquals(0, nodeTwo.getTotalCapability().getVirtualCores());
    assertEquals(0, nodeTwo.getTotalCapability().getMemory());
  }
  
  @Test 
  public void testOfferWithinResourceLimits() throws Exception {
    Resource resourcesOne = Resource.newInstance(512, 1);
    Resource offerOne = Resource.newInstance(1024, 2);
    Resource offerTwo = Resource.newInstance(4096, 2);
    Resource offerThree = Resource.newInstance(1024, 8);
 
    assertTrue(handler.offerWithinResourceLimits(resourcesOne, offerOne));
    assertFalse(handler.offerWithinResourceLimits(resourcesOne, offerTwo));
    assertFalse(handler.offerWithinResourceLimits(resourcesOne, offerThree));
  }
  
  @Test 
  public void testGetNewResourcesOfferedByMesos() throws Exception {
    Offer offerOne = TestObjectFactory.getOffer("localhost-one", "slave-one", "mock", "offer-one", 1.0, 512.0);
    Offer offerTwo = TestObjectFactory.getOffer("localhost-two", "slave-two", "mock", "offer-two", 2.0, 1024.0);
    olManager.addOffers(offerOne);
    olManager.addOffers(offerTwo);
    Resource resourcesOne = handler.getNewResourcesOfferedByMesos("localhost-one");
    assertEquals(1.0, resourcesOne.getVirtualCores(), 0.0);
    assertEquals(512.0, resourcesOne.getMemory(), 0.0);
    Resource resourcesTwo = handler.getNewResourcesOfferedByMesos("localhost-two");
    assertEquals(2.0, resourcesTwo.getVirtualCores(), 0.0);
    assertEquals(1024.0, resourcesTwo.getMemory(), 0.0);
  }
  
  @Test
  public void testIncrementNodeCapacityUnderCapacity() throws Exception {
    resetNodeTotalCapability(nodeOne, 0, 0);
    resetNodeTotalCapability(nodeTwo, 2, 512);
    Offer offerOne = TestObjectFactory.getOffer("localhost-one", "slave-one", "mock", "offer-one", 1.0, 512.0);
    Offer offerTwo = TestObjectFactory.getOffer("localhost-two", "slave-two", "mock", "offer-two", 3.0, 1024.0);
    olManager.addOffers(offerOne);
    olManager.addOffers(offerTwo);
        
    RMNodeStatusEvent eventOne = getRMStatusEvent(nodeOne);
    handler.beforeRMNodeEventHandled(eventOne, context);
    RMNodeStatusEvent eventTwo = getRMStatusEvent(nodeTwo);
    handler.beforeRMNodeEventHandled(eventTwo, context);
    
    assertEquals(512, nodeOne.getTotalCapability().getMemory());
    assertEquals(1, nodeOne.getTotalCapability().getVirtualCores());
    assertEquals(1024, nodeTwo.getTotalCapability().getMemory());
    assertEquals(3, nodeTwo.getTotalCapability().getVirtualCores());
  }
  
  @Test
  public void testIncrementNodeCapacityOverCapacity() throws Exception {
    resetNodeTotalCapability(nodeOne, 1, 512);
    resetNodeTotalCapability(nodeTwo, 2, 2048);
    
    //Test over memory upper limit
    Offer offerOne = TestObjectFactory.getOffer("localhost-one", "slave-one", "mock", "offer-one", 0.2, 3072.0);
    //Test over CPU cores upper limit
    Offer offerTwo = TestObjectFactory.getOffer("localhost-two", "slave-two", "mock", "offer-two", 8.0, 1024.0);
    olManager.addOffers(offerOne);
    olManager.addOffers(offerTwo);

    RMNodeStatusEvent eventOne = getRMStatusEvent(nodeOne);  
    handler.beforeRMNodeEventHandled(eventOne, context);
    RMNodeStatusEvent eventTwo = getRMStatusEvent(nodeTwo);
    handler.beforeRMNodeEventHandled(eventTwo, context);
    
    assertEquals(512, nodeOne.getTotalCapability().getMemory());
    assertEquals(1, nodeOne.getTotalCapability().getVirtualCores()); 
    assertEquals(2048, nodeTwo.getTotalCapability().getMemory());
    assertEquals(2, nodeTwo.getTotalCapability().getVirtualCores());
  }
    
  private RMNodeStatusEvent getRMStatusEvent(RMNode node) {
    NodeId id = node.getNodeID();
    NodeHealthStatus hStatus = NodeHealthStatusPBImpl.newInstance(true, "HEALTHY", System.currentTimeMillis());
    List<ContainerStatus> cStatus = Lists.newArrayList(getContainerStatus(node));
    List<ApplicationId> keepAliveIds = Lists.newArrayList(getApplicationId(node.getHttpPort()));
    NodeHeartbeatResponse response = new NodeHeartbeatResponsePBImpl();
    
    return new RMNodeStatusEvent(id, hStatus, cStatus, keepAliveIds, response);
  }
  
  private ContainerStatus getContainerStatus(RMNode node) {
    ContainerStatus status = new ContainerStatusPBImpl();
    return status;
  }

  private ApplicationId getApplicationId(int id) {
    return ApplicationId.newInstance(System.currentTimeMillis(), id);
  }
  
  private void resetNodeTotalCapability(RMNode node, int cpuCores, int memory) {
    node.getTotalCapability().setVirtualCores(cpuCores);
    node.getTotalCapability().setMemory(memory);
  }
}