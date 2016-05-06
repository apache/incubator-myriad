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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Class to test CommandLine generation
 */
public class TestServiceCommandLine {

  static MyriadConfiguration cfg;

  static String toJHSCompare =
      "echo \" sudo tar -zxpf hadoop-2.7.0.tar.gz &&  sudo  cp conf /usr/local/hadoop/etc/hadoop/yarn-site.xml; " +
      "export TASK_DIR=`basename $PWD`; sudo  chmod +wx /sys/fs/cgroup/cpu/mesos/$TASK_DIR;" +
      "sudo -E -u hduser -H  $YARN_HOME/bin/mapred historyserver\"; sudo tar -zxpf hadoop-2.5.0.tar.gz &&  sudo  cp" +
      " conf /usr/local/hadoop/etc/hadoop/yarn-site.xml; sudo -E -u hduser -H $YARN_HOME/bin/mapred historyserver";
  static String toCompare =
      "echo \" sudo tar -zxpf hadoop-2.7.0.tar.gz &&  sudo  cp conf /usr/local/hadoop/etc/hadoop/yarn-site.xml;";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
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
    System.out.println(toJHSCompare);
    System.out.println(cInfo.getValue());

    assertTrue(cInfo.getValue().startsWith(toCompare));
  }

  @Test
  public void testNMCommandLineGeneration() throws Exception {
    Long[] ports = new Long[]{1L, 2L, 3L, 4L};
    NMPorts nmPorts = new NMPorts(ports);

    ServiceResourceProfile profile = new ExtendedResourceProfile(new NMProfile("nm", 10L, 15L), 3.0, 5.0);

    ExecutorCommandLineGenerator clGenerator = new DownloadNMExecutorCLGenImpl(cfg,
        "hdfs://namenode:port/dist/hadoop-2.7.0.tar.gz");
    NMTaskFactoryImpl nms = new NMTaskFactoryImpl(cfg, null, clGenerator);

    CommandInfo cInfo = nms.getCommandInfo(profile, nmPorts);
    System.out.println(toCompare);
    System.out.println(cInfo.getValue());
    assertTrue(cInfo.getValue().startsWith(toCompare));

  }
}
