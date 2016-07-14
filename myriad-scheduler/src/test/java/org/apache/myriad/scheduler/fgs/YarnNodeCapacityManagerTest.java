package org.apache.myriad.scheduler.fgs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.TaskUtils;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Unit tests for YarnNodeCapacityManager
 */
public class YarnNodeCapacityManagerTest extends BaseConfigurableTest {
  YarnNodeCapacityManager manager;
  NodeStore store;
  NodeId zNodeId = NodeId.newInstance("0.0.0.1", 8041);
  TaskID zTaskId = TaskID.newBuilder().setValue("nm").build();
  NodeTask ntZero;
  SchedulerNode zNode;
  SchedulerState sState;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
 
    AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> scheduler = TestObjectFactory.getYarnScheduler();
    InterceptorRegistry registry = TestObjectFactory.getInterceptorRegistry();
    sState = TestObjectFactory.getSchedulerState(cfg);
   
    RMContext context = TestObjectFactory.getRMContext(new Configuration());
    MyriadDriver driver = new MyriadDriver(new MockSchedulerDriver());
    store = new NodeStore();
    OfferLifecycleManager oManager = new OfferLifecycleManager(store, driver);

    zNode  = TestObjectFactory.getSchedulerNode(zNodeId, 0, 0);
    
    manager = new YarnNodeCapacityManager(registry, scheduler, context, driver, oManager, store, sState, new TaskUtils(cfg));
  }
  
  private Set<NodeTask> getNodeTasks() {
    Constraint cZero = new LikeConstraint("0.0.0.1", "host-[0-9]*.example.com");
    ServiceResourceProfile zProfile = new ServiceResourceProfile("zProfile", 0.0, 0.0, 0.0, 0.0);

    ntZero = new NodeTask(zProfile, cZero);
    ntZero.setTaskPrefix("nm");
    ntZero.setHostname("0.0.0.1");
 
    return Sets.newHashSet(ntZero);
  }

  @Test
  public void testAllowCallBacksForNode() throws Exception {
    store.add(zNode);
    sState.addNodes(getNodeTasks());
    sState.addTask(zTaskId, ntZero);
      
    sState.makeTaskActive(zTaskId);
    assertEquals(1, sState.getActiveTasks().size());

    YarnSchedulerInterceptor.CallBackFilter filter = manager.getCallBackFilter();
    assertTrue(filter.allowCallBacksForNode(zNodeId));
  }
  
  public void testIncrementNodeCapacity() throws Exception {
    manager.incrementNodeCapacity(zNode.getRMNode(), TestObjectFactory.getResource(2, 2048));
    assertEquals(6, zNode.getTotalResource().getVirtualCores());
  }
}