package org.apache.myriad.scheduler;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.ahs.RMApplicationHistoryWriter;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.AMLivelinessMonitor;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.security.RMDelegationTokenSecretManager;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.policy.LeastAMNodesFirstPolicy;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.state.MockDispatcher;
import org.apache.myriad.state.MockRMContext;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.MyriadWebServer;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MyriadOperations class
 */
public class MyriadOperationsTest extends BaseConfigurableTest {
  MyriadOperations ops;
  ServiceResourceProfile small;
  Constraint constraint = new LikeConstraint("localhost", "host-[0-9]*.example.com");
  SchedulerState sState;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> scheduler = TestObjectFactory.getYarnScheduler();
    sState = TestObjectFactory.getSchedulerState(cfg);
    sState.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());

    MyriadDriverManager manager = TestObjectFactory.getMyriadDriverManager();
    MyriadWebServer webServer = TestObjectFactory.getMyriadWebServer(cfg);
    CompositeInterceptor registry = new CompositeInterceptor();
    LeastAMNodesFirstPolicy policy = new LeastAMNodesFirstPolicy(registry, scheduler, sState);

    manager.startDriver();

    ops = new MyriadOperations(cfg, sState, policy, manager, webServer, generateRMContext(scheduler));
    generateProfiles();
  }

  private void generateProfiles() {
    small = new ServiceResourceProfile("small", new Double(0.1), new Double(512.0), new HashMap<String, Long>());
  }

  private RMContext generateRMContext(AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> scheduler) throws Exception {
    Configuration conf = new Configuration();
    MockRMContext context = null;
    Dispatcher dispatcher = new MockDispatcher();

    RMApplicationHistoryWriter rmApplicationHistoryWriter = new RMApplicationHistoryWriter(); 
    AMLivelinessMonitor amLivelinessMonitor = new AMLivelinessMonitor(dispatcher);
    AMLivelinessMonitor amFinishingMonitor = new AMLivelinessMonitor(dispatcher);    
    RMDelegationTokenSecretManager delegationTokenSecretManager = new RMDelegationTokenSecretManager(1, 1, 1, 1, context);

    context = new MockRMContext();
    context.setStateStore(TestObjectFactory.getStateStore(conf));
    context.setAmLivelinessMonitor(amLivelinessMonitor);
    context.setAmFinishingMonitor(amFinishingMonitor);
    context.setRMApplicationHistoryWriter(rmApplicationHistoryWriter);
    context.setRMDelegationTokenSecretManager(delegationTokenSecretManager);
    return context;
  }

  @Test 
  public void testFlexUpAndFlexDownCluster() throws Exception {
    assertEquals(1, sState.getPendingTaskIds().size());
    ops.flexUpCluster(small, 1, constraint);
    assertEquals(2, sState.getPendingTaskIds().size());
    ops.flexDownCluster(small, constraint, 1);
    assertEquals(1, sState.getPendingTaskIds().size());
  }

  @Test
  public void testFlexUpAndFlexDownService() throws Exception {
    ops.flexUpAService(1, "jobhistory");
    assertEquals(2, sState.getPendingTasksByType("jobhistory").size());
    ops.flexDownAService(1, "jobhistory");
    assertEquals(1, sState.getPendingTasksByType("jobhistory").size());
  }

  @Test(expected = MyriadBadConfigurationException.class)
  public void testFlexUpAServiceOverMaxInstances() throws Exception {
    ops.flexUpAService(2, "jobhistory");
  }

  @Test
  public void testGetFlexibleInstances() throws Exception {
    ops.flexUpAService(1, "jobhistory");
    assertEquals(1, ops.getFlexibleInstances("jobhistory").intValue());
  }

  @Test
  public void testShutdownCluster() throws Exception {
    ops.shutdownFramework();
  }
}