package org.apache.myriad;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.net.NodeBase;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.impl.pb.ResourcePBImpl;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.ahs.RMApplicationHistoryWriter;
import org.apache.hadoop.yarn.server.resourcemanager.recovery.FileSystemRMStateStore;
import org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.AMLivelinessMonitor;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.security.RMDelegationTokenSecretManager;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.policy.LeastAMNodesFirstPolicy;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.MyriadDriverManager;
import org.apache.myriad.scheduler.MyriadOperations;
import org.apache.myriad.scheduler.yarn.MyriadCapacityScheduler;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.MockDispatcher;
import org.apache.myriad.state.MockRMContext;
import org.apache.myriad.state.MockRMNode;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.HttpConnectorProvider;
import org.apache.myriad.webapp.MyriadWebServer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

import com.google.inject.servlet.GuiceFilter;

/**
 * Factory for common standard and mock objects utilized for JUnit tests
 */
public class TestObjectFactory {
  public static SchedulerState getSchedulerState(MyriadConfiguration cfg) throws Exception {
    Configuration conf = new Configuration();
    SchedulerState state = new SchedulerState(TestObjectFactory.getStateStore(conf, false));
    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    return state;  
  }

  public static FileSystemRMStateStore getRMStateStore(Configuration conf) throws Exception {
    FileSystemRMStateStore store = new MyriadFileSystemRMStateStore();
    conf.set("yarn.resourcemanager.fs.state-store.uri", "/tmp");
    store.initInternal(conf);
    return store;
  }

  public static MyriadDriverManager getMyriadDriverManager() {
    return new MyriadDriverManager(new MyriadDriver(new MockSchedulerDriver()));
  }

  public static InterceptorRegistry getInterceptorRegistry() {
    return new CompositeInterceptor();
  }

  public static AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> getYarnScheduler() {
    MyriadCapacityScheduler scheduler = new MyriadCapacityScheduler();
    return scheduler;
  }

  private static Server getJettyServer() {
    Server server = new Server();
    ServletHandler context = new ServletHandler();
    ServletHolder holder = new ServletHolder(DefaultServlet.class);
    holder.setInitParameter("resourceBase", System.getProperty("user.dir"));
    holder.setInitParameter("dirAllowed", "true");
    context.setServer(server);
    context.addServlet(holder);
    server.setHandler(context);    

    return server;
  }

  public static MyriadWebServer getMyriadWebServer(MyriadConfiguration cfg) {
    Server server = TestObjectFactory.getJettyServer();
    HttpConnectorProvider provider = new HttpConnectorProvider(cfg);
    Connector connector = provider.get();
    return new MyriadWebServer(server, connector, new GuiceFilter());
  }
  
  public static MyriadFileSystemRMStateStore getStateStore(Configuration conf, boolean loadState) throws Exception {
    conf.set("yarn.resourcemanager.fs.state-store.uri", "file:///tmp/");
    MyriadFileSystemRMStateStore store = new MyriadFileSystemRMStateStore();
    store.init(conf);
    store.start();
    if (loadState) {
      store.loadState(); 
    }
    store.setRMDispatcher(new MockDispatcher());
    return store;
  }

  public static Offer getOffer(String host, String slaveId, String frameworkId, String offerId) {
    Protos.SlaveID sid = SlaveID.newBuilder().setValue(slaveId).build();
    Protos.FrameworkID fid = FrameworkID.newBuilder().setValue(frameworkId).build();
    return Protos.Offer.newBuilder().setHostname(host).setId(OfferID.newBuilder().setValue(offerId)).setSlaveId(sid).setFrameworkId(fid).build();  
  }

  public static RMContext getRMContext(Configuration conf) throws Exception {
    conf.set("yarn.resourcemanager.fs.state-store.uri", "file:///tmp/");
    MockRMContext context = null;
    Dispatcher dispatcher = new MockDispatcher();

    RMApplicationHistoryWriter rmApplicationHistoryWriter = new RMApplicationHistoryWriter(); 
    AMLivelinessMonitor amLivelinessMonitor = new AMLivelinessMonitor(dispatcher);
    AMLivelinessMonitor amFinishingMonitor = new AMLivelinessMonitor(dispatcher);    
    RMDelegationTokenSecretManager delegationTokenSecretManager = new RMDelegationTokenSecretManager(1, 1, 1, 1, context);

    context = new MockRMContext();
    context.setStateStore(TestObjectFactory.getStateStore(conf, false));
    context.setAmLivelinessMonitor(amLivelinessMonitor);
    context.setAmFinishingMonitor(amFinishingMonitor);
    context.setRMApplicationHistoryWriter(rmApplicationHistoryWriter);
    context.setRMDelegationTokenSecretManager(delegationTokenSecretManager);
    return context;
  }
  
  public static MyriadOperations getMyriadOperations(MyriadConfiguration cfg) throws Exception {
    AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> scheduler = TestObjectFactory.getYarnScheduler();
    SchedulerState sState = TestObjectFactory.getSchedulerState(cfg);
    sState.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());

    MyriadDriverManager manager = TestObjectFactory.getMyriadDriverManager();
    MyriadWebServer webServer = TestObjectFactory.getMyriadWebServer(cfg);
    CompositeInterceptor registry = new CompositeInterceptor();
    LeastAMNodesFirstPolicy policy = new LeastAMNodesFirstPolicy(registry, scheduler, sState);
    return new MyriadOperations(cfg, sState, policy, manager, webServer, TestObjectFactory.getRMContext(new Configuration()));
  }
  
  public static SchedulerNode getSchedulerNode(NodeId nodeId, int vCores, int memory) {
    RMNode node = getMockRMNode(nodeId, vCores, memory);
    return new FiCaSchedulerNode(node, true);
  }

  public static RMNode getMockRMNode(NodeId nodeId, int vCores, int memory) {
    MockRMNode node = new MockRMNode(nodeId, NodeState.NEW, new NodeBase("/tmp"));
    node.setCommandPort(8041);
    node.setHostName("0.0.0.0");
    node.setHttpPort(8042);
    node.setRackName("r01n07");
    node.setHttpAddress("localhost:8042");
    node.setTotalCapability(getResource(vCores, memory));
 
    return node;
  }

  public static Resource getResource(int vCores, int memory) {
    Resource resource = new ResourcePBImpl();
    resource.setVirtualCores(vCores);
    resource.setMemory(memory);
    return resource;
  }
}