package org.apache.myriad.scheduler;

import static org.junit.Assert.assertEquals;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.policy.LeastAMNodesFirstPolicy;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.MyriadWebServer;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MyriadOperations class
 */
public class MyriadOperationsTest extends BaseConfigurableTest {
  ServiceResourceProfile small;
  Constraint constraint = new LikeConstraint("localhost", "host-[0-9]*.example.com");
  MyriadWebServer webServer;

  private SchedulerState getSchedulerState() throws Exception {
    SchedulerState state = TestObjectFactory.getSchedulerState(this.cfg);
    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    return state;
  }
  
  private MyriadOperations getMyriadOperations(SchedulerState state) throws Exception {
    MyriadDriverManager manager = TestObjectFactory.getMyriadDriverManager();

    AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> scheduler = TestObjectFactory.getYarnScheduler();
    CompositeInterceptor registry = new CompositeInterceptor();
    LeastAMNodesFirstPolicy policy = new LeastAMNodesFirstPolicy(registry, scheduler, state);
    manager.startDriver();

    return new MyriadOperations(cfg, state, policy, manager, webServer, TestObjectFactory.getRMContext(new Configuration()));    
  }
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    webServer = TestObjectFactory.getMyriadWebServer(cfg);
    generateProfiles();
  }

  private void generateProfiles() {
    small = new ServiceResourceProfile("small", 0.1, 512.0);
  }

  @Test 
  public void testFlexUpAndFlexDownCluster() throws Exception {
    SchedulerState sState = this.getSchedulerState();
    MyriadOperations ops = this.getMyriadOperations(sState);
    assertEquals(0, sState.getPendingTaskIds().size());
    ops.flexUpCluster(small, 1, constraint);
    assertEquals(1, sState.getPendingTaskIds().size());
    ops.flexDownCluster(small, constraint, 1);
    assertEquals(0, sState.getPendingTaskIds().size());
  }
  
  @Test
  public void testFlexUpAndFlexDownService() throws Exception {
    SchedulerState sState = this.getSchedulerState();
    MyriadOperations ops = this.getMyriadOperations(sState);
    ops.flexUpAService(1, "jobhistory");
    assertEquals(1, sState.getPendingTasksByType("jobhistory").size());
    ops.flexDownAService(1, "jobhistory");
    assertEquals(0, sState.getPendingTasksByType("jobhistory").size());
  }

  @Test(expected = MyriadBadConfigurationException.class)
  public void testFlexUpAServiceOverMaxInstances() throws Exception {
    SchedulerState sState = this.getSchedulerState();
    MyriadOperations ops = this.getMyriadOperations(sState);
    /*
     * There is 1 jobhhistory task loaded from configuration file, so flexing up
     * by two should result in MyriadBadConfigurationException
     */
    ops.flexUpAService(2, "jobhistory");
  }

  @Test
  public void testGetFlexibleInstances() throws Exception {
    SchedulerState sState = this.getSchedulerState();
    MyriadOperations ops = this.getMyriadOperations(sState);
    assertEquals(0, ops.getFlexibleInstances("jobhistory").intValue());
    ops.flexUpAService(1, "jobhistory");
    assertEquals(1, ops.getFlexibleInstances("jobhistory").intValue());
  }

  @Test
  public void testShutdownCluster() throws Exception {
    SchedulerState sState = this.getSchedulerState();
    MyriadOperations ops  = this.getMyriadOperations(sState);
    ops.shutdownFramework();
  }
}