package org.apache.myriad;

import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.net.NodeBase;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.impl.pb.ContainerStatusPBImpl;
import org.apache.hadoop.yarn.event.Dispatcher;
import org.apache.hadoop.yarn.server.api.protocolrecords.NodeHeartbeatResponse;
import org.apache.hadoop.yarn.server.api.protocolrecords.impl.pb.NodeHeartbeatResponsePBImpl;
import org.apache.hadoop.yarn.server.api.records.NodeHealthStatus;
import org.apache.hadoop.yarn.server.api.records.impl.pb.NodeHealthStatusPBImpl;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainerImpl;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeImpl;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.Value.Type;
import org.apache.mesos.SchedulerDriver;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.ExtendedResourceProfile;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.MyriadDriverManager;
import org.apache.myriad.scheduler.NMProfile;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.scheduler.yarn.MyriadCapacityScheduler;
import org.apache.myriad.scheduler.yarn.MyriadFairScheduler;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.MockDispatcher;
import org.apache.myriad.state.MockRMContext;
import org.apache.myriad.state.MyriadStateStore;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.HttpConnectorProvider;
import org.apache.myriad.webapp.MyriadWebServer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.DefaultServlet;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

import com.google.common.collect.Lists;
import com.google.inject.servlet.GuiceFilter;

/**
 * Factory for common objects utilized over 1..n JUnit tests
 */
public class TestObjectFactory {

  /**
   * Returns a new RMContainer corresponding to the RMNode and RMContext. The RMContainer is the 
   * ResourceManager's view of an application container per the Hadoop docs
   * 
   * @param node
   * @param context
   * @param appId
   * @param cores
   * @param memory
   * @return RMContainer
   */
  public static RMContainer getRMContainer(RMNode node, RMContext context, int appId, int cores, int memory) {
    ContainerId containerId = ContainerId.newContainerId(ApplicationAttemptId.newInstance(
        ApplicationId.newInstance(123456789, 1), 1), appId);

    Container container = Container.newInstance(containerId, node.getNodeID(), node.getHttpAddress(),
        Resources.createResource(memory, cores), null, null);
    return new RMContainerImpl(container, containerId.getApplicationAttemptId(), node.getNodeID(), "user1", context);
  }

  public static RMNodeStatusEvent getRMStatusEvent(RMNode node) {
    NodeId id = node.getNodeID();
    NodeHealthStatus hStatus = NodeHealthStatusPBImpl.newInstance(true, "HEALTHY", System.currentTimeMillis());
    List<ContainerStatus> cStatus = Lists.newArrayList(getContainerStatus(node));
    List<ApplicationId> keepAliveIds = Lists.newArrayList(getApplicationId(node.getHttpPort()));
    NodeHeartbeatResponse response = new NodeHeartbeatResponsePBImpl();
    
    return new RMNodeStatusEvent(id, hStatus, cStatus, keepAliveIds, response);
  }
  
  private static ContainerStatus getContainerStatus(RMNode node) {
    ContainerStatus status = new ContainerStatusPBImpl();
    return status;
  }

  private static ApplicationId getApplicationId(int id) {
    return ApplicationId.newInstance(System.currentTimeMillis(), id);
  }
  /**
   * Returns a ServiceResourceProfile or ExtendedResourceProfile object depending upon
   * whether the execCores and execMemory parameters are null or non-null, respectively
   * 
   * @param profileName
   * @param cores
   * @param memory
   * @param execCores
   * @param execMemory
   * @return ServiceResourceProfile if execCores and execMemory are null, ExtendedResourceProfile otherwise
   */
  public static ServiceResourceProfile getServiceResourceProfile(String profileName, Double cores, Double memory, 
      Long execCores, Long execMemory) {
    if (isExtendedResource(execCores, execMemory)) {
      NMProfile nmProfile = new NMProfile(profileName, execCores, execMemory);
      return new ExtendedResourceProfile(nmProfile, cores, memory, new HashMap<String, Long>());
    }
    return new ServiceResourceProfile(profileName, cores, memory, new HashMap<String, Long>());
  }
  
  private static boolean isExtendedResource(Long execCores, Long execMemory) {
    return execCores != null && execMemory != null;
  }
  
  /**
   * Returns a NodeTask with either a ServiceResourceProfile or an ExtendedResourceProfile, 
   * depending upon whether execCores and execMemory are null or non-null, respectively
   * 
   * @param profileName
   * @param hostName
   * @param cores
   * @param memory
   * @param execCores
   * @param execMemory
   * @return NodeTask
   */
  public static NodeTask getNodeTask(String profileName, String hostName, Double cores, Double memory, 
      Long execCores, Long execMemory) {
    NodeTask task = new NodeTask(getServiceResourceProfile(profileName, cores, memory, execCores, execMemory), 
        new LikeConstraint(hostName, "host-[0-9]*.example.com"));
    task.setHostname(hostName);
    task.setTaskPrefix("nm");
    task.setSlaveId(SlaveID.newBuilder().setValue(profileName + "-" + hostName).build());
    task.setExecutorInfo(ExecutorInfo.newBuilder().setExecutorId(ExecutorID.newBuilder().setValue("exec")).
        setCommand(org.apache.mesos.Protos.CommandInfo.newBuilder().setValue("command")).build());
    return task;
  }

 /**
  * Returns a NodeTask given a ServiceResourceProfile and hostname
  * 
  * @param hostName
  * @param profile
  * @return
  */
  public static NodeTask getNodeTask(String hostName, ServiceResourceProfile profile) {
    NodeTask task = new NodeTask(profile, new LikeConstraint(hostName, "host-[0-9]*.example.com"));
    task.setHostname(hostName);
    task.setTaskPrefix("nm");
    task.setSlaveId(SlaveID.newBuilder().setValue(profile.getName() + "-" + hostName).build());
    task.setExecutorInfo(ExecutorInfo.newBuilder().setExecutorId(ExecutorID.newBuilder().setValue("exec")).
         setCommand(org.apache.mesos.Protos.CommandInfo.newBuilder().setValue("command")).build());
    return task;
  }  
  
  public static RMNode getRMNode(String host, int port, Resource resource) {
    NodeId id = NodeId.newInstance(host, port);
    RMContext context = new MockRMContext();
    return new RMNodeImpl(id, context, id.getHost(), id.getPort(), id.getPort(), new NodeBase(host, "/tmp"), resource, "version-one");
  }

  public static RMNode getRMNode(String host, int port, int memory, int cores) {
    Resource resource = Resource.newInstance(memory, cores);
    return getRMNode(host, port, resource);
  }
  
  public static Dispatcher getMockDispatcher() {
    return new MockDispatcher();
  }
  
  public static SchedulerState getSchedulerState(MyriadConfiguration cfg) throws Exception {
    MyriadStateStore store = TestObjectFactory.getStateStore(new Configuration());
    SchedulerState state = new SchedulerState(store);
    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    return state;  
  }

  public static MyriadFairScheduler getMyriadFairScheduler(RMContext context) {
    MyriadFairScheduler scheduler = new MyriadFairScheduler();
    scheduler.setRMContext(context); 
    return scheduler;
  }
  
  public static SchedulerNode getSchedulerNode(String host, int port, int cores, int memory) {
    RMNode node = TestObjectFactory.getRMNode(host, port, cores, memory);
    return new FiCaSchedulerNode(node, false);
  }
  
  public static MyriadFairScheduler getYarnFairScheduler() {
    RMContext context = new MockRMContext();
    return getMyriadFairScheduler(context);
  }  
  
  public static MyriadDriverManager getMyriadDriverManager(MyriadDriver driver) {
    return new MyriadDriverManager(driver);
  }
  
  public static MyriadDriverManager getMyriadDriverManager() {
    return getMyriadDriverManager(new MyriadDriver(new MockSchedulerDriver()));
  }
  
  public static MyriadDriver getMyriadDriver(SchedulerDriver driver) {
    return new MyriadDriver(driver);
  }

  public static InterceptorRegistry getInterceptorRegistry() {
    return new CompositeInterceptor();
  }

  public static AbstractYarnScheduler<FiCaSchedulerApp, FiCaSchedulerNode> getYarnScheduler() {
    MyriadCapacityScheduler scheduler = new MyriadCapacityScheduler();
    return scheduler;
  }

  public static Server getJettyServer() {
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
  
  public static MyriadFileSystemRMStateStore getStateStore(Configuration conf) throws Exception {
    conf.set("yarn.resourcemanager.fs.state-store.uri", "file:///tmp/");
    MyriadFileSystemRMStateStore store = new MyriadFileSystemRMStateStore();
    store.init(conf);
    store.start();
    store.loadState();   
    store.setRMDispatcher(new MockDispatcher());
    return store;
  }

  public static Offer getOffer(String host, String slaveId, String frameworkId, String offerId, double cpuCores, double memory) {
    Protos.SlaveID sid = SlaveID.newBuilder().setValue(slaveId).build();
    Protos.FrameworkID fid = FrameworkID.newBuilder().setValue(frameworkId).build();
    Protos.Value.Scalar cores = Protos.Value.Scalar.newBuilder().setValue(cpuCores).build();
    Protos.Value.Scalar mem = Protos.Value.Scalar.newBuilder().setValue(memory).build();
    Protos.Resource cpuResource = Protos.Resource.newBuilder().setName("cpus").setScalar(cores).setType(Type.SCALAR).build();
    Protos.Resource memResource  = Protos.Resource.newBuilder().setName("mem").setScalar(mem).setType(Type.SCALAR).build();
    return Protos.Offer.newBuilder().setHostname(host).setId(OfferID.newBuilder().setValue(offerId)).
        setSlaveId(sid).setFrameworkId(fid).addResources(cpuResource).addResources(memResource).build();  
  }
}