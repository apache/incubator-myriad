package org.apache.myriad.scheduler;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.scheduler.offer.OfferBuilder;
import org.apache.myriad.scheduler.resource.ResourceOfferContainer;
import org.apache.myriad.state.NodeTask;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Tests for Configuration with vcore ratio
 *
 */
public class TestConfigurationWithVcoreRatio {
  protected MyriadConfiguration cfg;
  //protected String baseStateStoreDirectory = StringUtils.EMPTY;
  static Protos.FrameworkID frameworkId = Protos.FrameworkID.newBuilder()
      .setValue("test").build();

  /**
   * This is normally overridden in derived classes. Be sure to invoke this
   * implementation; otherwise, cfg, cfgWithRole, and cfgWithDocker will all be
   * null.
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(
        getConfURL("myriad-config-test-default-with-vcoreratio.yml"),
        MyriadConfiguration.class);
  }

  private URL getConfURL(String file) throws MalformedURLException {
    return Thread.currentThread().getContextClassLoader().getResource(file);
    //return new URL("file:///home/luodi/hadoop/myriad1/incubator-myriad/myriad-scheduler/src/test/resources/myriad-config-test-default-with-vcoreratio.yml");
  }

  @Test
  public void testNMTaskFactory() {
    NMExecutorCommandLineGenerator clGenerator = new NMExecutorCommandLineGenerator(
        cfg);
    TaskUtils taskUtils = new TaskUtils(cfg);
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 10.0).addScalarResource("mem", 16000)
        .addRangeResource("ports", 3500, 3505).build();
    ServiceResourceProfile profile = new ExtendedResourceProfile(new NMProfile(
        "large NM", 7L, 8000L, 0.3), taskUtils.getNodeManagerCpus(),
        taskUtils.getNodeManagerMemory(), taskUtils.getNodeManagerPorts());
    NodeTask nodeTask = new NodeTask(profile, null);
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile,
        null);
    NMTaskFactory taskFactory = new NMTaskFactory(cfg, taskUtils, clGenerator);
    Protos.TaskInfo taskInfo = taskFactory.createTask(roc, frameworkId,
        makeTaskId("nm.zero"), nodeTask);
    // check cpu resource
    assertEquals(2.9, taskInfo.getResources(0).getScalar().getValue(), 0.1);
    // check mem resource
    assertEquals(10048, taskInfo.getResources(1).getScalar().getValue(), 0.1);
  }
  
  @Test
  public void testJobHistoryServerTask() {
    ServiceCommandLineGenerator clGenerator = new ServiceCommandLineGenerator(
        cfg);
    TaskUtils taskUtils = new TaskUtils(cfg);
    ServiceConfiguration serviceConfiguration = cfg.getServiceConfigurations()
        .get("jobhistory");
    ServiceResourceProfile profile = new ServiceResourceProfile("jobhistory",
        serviceConfiguration.getCpus(),
        serviceConfiguration.getJvmMaxMemoryMB(),
        serviceConfiguration.getPorts());
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 10.0).addScalarResource("mem", 16000)
        .addRangeResource("ports", 3500, 3505).build();
    NodeTask nodeTask = new NodeTask(profile, null);
    nodeTask.setTaskPrefix("jobhistory");
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile,
        null);
    System.out.print(roc.getPorts());
    ServiceTaskFactory taskFactory = new ServiceTaskFactory(cfg, taskUtils,
        clGenerator);
    Protos.TaskInfo taskInfo = taskFactory.createTask(roc, frameworkId,
        makeTaskId("jobhistory"), nodeTask);
    // check cpu resource
    assertEquals(1.0, taskInfo.getResources(0).getScalar().getValue(), 0.1);
    // check mem resource
    assertEquals(1024, taskInfo.getResources(1).getScalar().getValue(), 0.1);
  }

  private Protos.TaskID makeTaskId(String taskId) {
    return Protos.TaskID.newBuilder().setValue(taskId).build();
  }

}
