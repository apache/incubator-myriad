package com.ebay.myriad.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.event.AsyncDispatcher;
import org.apache.hadoop.yarn.server.resourcemanager.MockNodes;
import org.apache.hadoop.yarn.server.resourcemanager.ResourceManager;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeRemovedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairSchedulerConfiguration;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ebay.myriad.Main;
import com.ebay.myriad.scheduler.yarn.MyriadFairScheduler;
import com.google.inject.Injector;

/**
 * Tests myriad scheduler.
 */
public class TestMyriadScheduler {
  protected Configuration conf;
  protected FairScheduler scheduler;
  protected ResourceManager resourceManager;

  @Before
  public void setUp() throws IOException {
    scheduler = new MyriadFairScheduler();
    conf = createConfiguration();
    resourceManager = new ResourceManager();
    resourceManager.init(conf);

    // TODO: This test should really be using MockRM. For now starting stuff
    // that is needed at a bare minimum.
    ((AsyncDispatcher) resourceManager.getRMContext().getDispatcher()).start();
    resourceManager.getRMContext().getStateStore().start();

    // to initialize the master key
    resourceManager.getRMContext().getContainerTokenSecretManager().rollMasterKey();

    scheduler.setRMContext(resourceManager.getRMContext());
    scheduler.init(conf);
    scheduler.start();
  }

  @After
  public void tearDown() {
    if (scheduler != null) {
      scheduler.stop();
      scheduler = null;
    }

    if (resourceManager != null) {
      resourceManager.stop();
      resourceManager = null;
    }
  }

  public Configuration createConfiguration() {
    Configuration conf = new YarnConfiguration();
    conf.setClass(YarnConfiguration.RM_SCHEDULER, FairScheduler.class,
        ResourceScheduler.class);
    conf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 0);
    conf.setInt(FairSchedulerConfiguration.RM_SCHEDULER_INCREMENT_ALLOCATION_MB,
        1024);
    conf.setInt(YarnConfiguration.RM_SCHEDULER_MAXIMUM_ALLOCATION_MB, 10240);
    return conf;
  }

  @Test
  public void testNodeStore() throws Exception {
    Injector injector = Main.getInjector();
    // TODO (Kannan Rajah) Find a better way to inject instances
    NodeStore nodeStore = injector.getInstance(NodeStore.class);

    // Add a node
    RMNode node1 =
        MockNodes
            .newNodeInfo(1, Resources.createResource(1024), 1, "127.0.0.1");
    NodeAddedSchedulerEvent nodeEvent1 = new NodeAddedSchedulerEvent(node1);
    scheduler.handle(nodeEvent1);
    assertEquals(1024, scheduler.getClusterResource().getMemory());

    assertNotNull(nodeStore.getNode("127.0.0.1"));

    // Add another node
    RMNode node2 =
        MockNodes.newNodeInfo(1, Resources.createResource(512), 2, "127.0.0.2");
    NodeAddedSchedulerEvent nodeEvent2 = new NodeAddedSchedulerEvent(node2);
    scheduler.handle(nodeEvent2);
    assertEquals(1536, scheduler.getClusterResource().getMemory());

    // Remove the first node
    NodeRemovedSchedulerEvent nodeEvent3 = new NodeRemovedSchedulerEvent(node1);
    scheduler.handle(nodeEvent3);
    assertEquals(512, scheduler.getClusterResource().getMemory());
  }
}

