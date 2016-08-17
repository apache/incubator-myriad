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
  ServiceResourceProfile small;
  Constraint constraint = new LikeConstraint("localhost", "host-[0-9]*.example.com");

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.baseStateStoreDirectory = "/tmp/myriad-operations-test";
    generateProfiles();
  }

  private MyriadOperations initialize() throws Exception {
    resetStoreState();
    SchedulerState sState = TestObjectFactory.getSchedulerState(cfg, "tmp/myriad-operations-test");
    sState.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> scheduler = TestObjectFactory.getYarnScheduler();
    MyriadDriverManager manager = TestObjectFactory.getMyriadDriverManager();
    MyriadWebServer webServer = TestObjectFactory.getMyriadWebServer(cfg);
    CompositeInterceptor registry = new CompositeInterceptor();
    LeastAMNodesFirstPolicy policy = new LeastAMNodesFirstPolicy(registry, scheduler, sState);

    manager.startDriver();

    return new MyriadOperations(cfg, sState, policy, manager, webServer, generateRMContext(scheduler));
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
    context.setStateStore(TestObjectFactory.getStateStore(conf, "tmp/myriad-operations-test"));
    context.setAmLivelinessMonitor(amLivelinessMonitor);
    context.setAmFinishingMonitor(amFinishingMonitor);
    context.setRMApplicationHistoryWriter(rmApplicationHistoryWriter);
    context.setRMDelegationTokenSecretManager(delegationTokenSecretManager);
    return context;
  }

  @Test 
  public void testFlexUpAndFlexDownCluster() throws Exception {
    MyriadOperations ops = initialize();
    assertEquals(0, ops.getSchedulerState().getPendingTaskIds().size());
    ops.flexUpCluster(small, 1, constraint);
    assertEquals(1, ops.getSchedulerState().getPendingTaskIds().size());
    ops.flexDownCluster(small, constraint, 1);
    assertEquals(0, ops.getSchedulerState().getPendingTaskIds().size());
  }

  @Test
  public void testFlexUpAndFlexDownService() throws Exception {
    MyriadOperations ops = initialize();
    ops.flexUpAService(1, "jobhistory");
    assertEquals(1, ops.getSchedulerState().getPendingTasksByType("jobhistory").size());
    ops.flexDownAService(1, "jobhistory");
    assertEquals(0, ops.getSchedulerState().getPendingTasksByType("jobhistory").size());
  }

  @Test(expected = MyriadBadConfigurationException.class)
  public void testFlexUpAServiceOverMaxInstances() throws Exception {
    MyriadOperations ops = initialize();
    ops.flexUpAService(3, "jobhistory");
  }

  @Test
  public void testGetFlexibleInstances() throws Exception {
    MyriadOperations ops = initialize();
    ops.flexUpAService(1, "jobhistory");
    assertEquals(1, ops.getFlexibleInstances("jobhistory").intValue());
  }

  @Test
  public void testShutdownCluster() throws Exception {
    MyriadOperations ops = initialize();
    ops.shutdownFramework();
  }
}