package org.apache.myriad;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.recovery.MyriadFileSystemRMStateStore;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerApp;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.common.fica.FiCaSchedulerNode;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.TaskID;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.MockSchedulerDriver;
import org.apache.myriad.scheduler.MyriadDriver;
import org.apache.myriad.scheduler.MyriadDriverManager;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.scheduler.yarn.MyriadCapacityScheduler;
import org.apache.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import org.apache.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import org.apache.myriad.state.MockDispatcher;
import org.apache.myriad.state.NodeTask;
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
 * Factory for common objects utilized over 1..n Junit tests
 */
public class TestObjectFactory {
  public static SchedulerState getMockSchedulerState(MyriadConfiguration cfg) {
    SchedulerState state = new SchedulerState(new MyriadFileSystemRMStateStore());
    TaskID idOne = Protos.TaskID.newBuilder().setValue("nt-1").build();
    TaskID idTwo = Protos.TaskID.newBuilder().setValue("nt-2").build();
    TaskID idThree = Protos.TaskID.newBuilder().setValue("nt-3").build();

    state.addTask(idOne, new NodeTask(new ServiceResourceProfile("profile1", 0.2, 1024.0), new LikeConstraint("localhost", "host-[0-9]*.example.com")));
    state.addTask(idTwo, new NodeTask(new ServiceResourceProfile("profile2", 0.4, 2048.0), new LikeConstraint("localhost", "host-[0-9]*.example.com")));
    state.addTask(idThree, new NodeTask(new ServiceResourceProfile("profile3", 0.6, 3072.0), new LikeConstraint("localhost", "host-[0-9]*.example.com")));

    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());
    state.makeTaskActive(idOne);
    state.makeTaskPending(idTwo); 
    state.makeTaskStaging(idThree);

    return state;  
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
    store.loadState();   
    store.loadMyriadState();
    store.setRMDispatcher(new MockDispatcher());
    return store;
  }

  public static Offer getOffer(String host, String slaveId, String frameworkId, String offerId) {
    Protos.SlaveID sid = SlaveID.newBuilder().setValue(slaveId).build();
    Protos.FrameworkID fid = FrameworkID.newBuilder().setValue(frameworkId).build();
    return Protos.Offer.newBuilder().setHostname(host).setId(OfferID.newBuilder().setValue(offerId)).setSlaveId(sid).setFrameworkId(fid).build();  
  }
}