package com.ebay.myriad.scheduler;

import static org.junit.Assert.*;

import org.apache.mesos.Protos.CommandInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Class to test CommandLine generation
 *
 */
public class TestServiceCommandLine {

  static MyriadConfiguration cfg;
  
  static String toJHSCompare = "echo \"sudo tar -zxpf hadoop-2.5.0.tar.gz && sudo chown hduser . &&" +
      " cp conf /usr/local/hadoop/etc/hadoop/yarn-site.xml; sudo -E -u hduser -H $YARN_HOME/bin/mapred historyserver\";" +
      "sudo tar -zxpf hadoop-2.5.0.tar.gz && sudo chown hduser . && cp conf /usr/local/hadoop/etc/hadoop/yarn-site.xml; sudo -E -u hduser -H $YARN_HOME/bin/mapred historyserver";
  
  static String toCompare = "echo \"sudo tar -zxpf hadoop-2.5.0.tar.gz && sudo chown hduser . && cp conf /usr/local/hadoop/etc/hadoop/yarn-site.xml;";
  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(
            Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
            MyriadConfiguration.class);

  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testJHSCommandLineGeneration() throws Exception {
    ServiceTaskFactoryImpl jhs = new ServiceTaskFactoryImpl(cfg, null);
    String executorCmd = "$YARN_HOME/bin/mapred historyserver";
    ServiceResourceProfile profile = new ServiceResourceProfile("jobhistory", 10.0, 15.0);
    
    CommandInfo cInfo = jhs.createCommandInfo(profile, executorCmd);
     
    assertTrue(cInfo.getValue().startsWith(toCompare));
  }

  @Test
  public void testNMCommandLineGeneration() throws Exception {
    Long [] ports = new Long [] {1L, 2L, 3L, 4L};
    NMPorts nmPorts = new NMPorts(ports);
    
    ServiceResourceProfile profile = new ExtendedResourceProfile(new NMProfile("nm", 10L, 15L), 3.0, 5.0);
    
    ExecutorCommandLineGenerator clGenerator = new DownloadNMExecutorCLGenImpl(cfg, "hdfs://namenode:port/dist/hadoop-2.5.0.tar.gz");
    NMTaskFactoryImpl nms = new NMTaskFactoryImpl(cfg, null, clGenerator);
    
    CommandInfo cInfo = nms.getCommandInfo(profile, nmPorts);
    
    assertTrue(cInfo.getValue().startsWith(toCompare));

  }
}
