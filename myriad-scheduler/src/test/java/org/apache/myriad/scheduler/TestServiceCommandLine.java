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

import org.apache.commons.lang.StringUtils;
import org.apache.mesos.Protos;
import static org.junit.Assert.assertTrue;

import org.apache.mesos.Protos.CommandInfo;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.BaseConfigurableTest;
import org.junit.Test;

import java.util.*;

/**
 * Class to test CommandLine generation
 */
public class TestServiceCommandLine extends BaseConfigurableTest {

  public static final String KEY_NM_ADDRESS = "myriad.yarn.nodemanager.address";
  public static final String KEY_NM_LOCALIZER_ADDRESS = "myriad.yarn.nodemanager.localizer.address";
  public static final String KEY_NM_WEBAPP_ADDRESS = "myriad.yarn.nodemanager.webapp.address";
  public static final String KEY_NM_SHUFFLE_PORT = "myriad.mapreduce.shuffle.port";

  public static final String KEY_JHS_WEBAPP_ADDRESS = "myriad.mapreduce.jobhistory.webapp.address";
  public static final String KEY_JHS_ADMIN_ADDRESS = "myriad.mapreduce.jobhistory.admin.address";
  public static final String KEY_JHS_ADDRESS = "myriad.mapreduce.jobhistory.address";

  private static final String msgFormat = System.lineSeparator() + "%s" + System.lineSeparator() + "!="
      + System.lineSeparator() + "%s";
  protected static final String CMD_FORMAT = "echo \"%1$s\" && %1$s";
  static String toJHSCompare =
      " sudo tar -zxpf hadoop-2.7.0.tar.gz &&  sudo  cp yarnConfiguration /usr/local/hadoop/etc/hadoop/yarn-site.xml &&  " +
          "sudo -E -u hduser -H  bin/mapred historyserver";
  static String toCompare =
      " sudo tar -zxpf hadoop-2.7.0.tar.gz &&  sudo  cp yarnConfiguration /usr/local/hadoop/etc/hadoop/yarn-site.xml &&  " +
          "sudo -E -u hduser -H  $YARN_HOME/bin/yarn nodemanager";

  @Test
  public void testJHSCommandLineGeneration() throws Exception {
    Map<String, Long> portsMap = new TreeMap<>();
    portsMap.put(KEY_JHS_ADDRESS, 0L);
    portsMap.put(KEY_JHS_WEBAPP_ADDRESS, 3L);
    portsMap.put(KEY_JHS_ADMIN_ADDRESS, 0L);

    ServiceResourceProfile profile = new ServiceResourceProfile("jobhistory", 10.0, 15.0, portsMap);
    ServiceConfiguration serviceConfiguration = cfg.getServiceConfiguration("jobhistory").get();
    ServiceCommandLineGenerator serviceCommandLineGenerator = new ServiceCommandLineGenerator(cfg);
    List<Long> ports = new ArrayList<>();
    ports.add(2L);
    ports.add(1L);
    ports.add(3L);

    CommandInfo cInfo = serviceCommandLineGenerator.generateCommandLine(profile,
        serviceConfiguration,
        ports);
    String testVal =  String.format(CMD_FORMAT, toJHSCompare);
    assertTrue(String.format(msgFormat, cInfo.getValue(), testVal),
        cInfo.getValue().equals(testVal));

    List<Protos.Environment.Variable> environmentList = cInfo.getEnvironment().getVariablesList();
    String yarnOpts = "";
    for (Protos.Environment.Variable variable: environmentList) {
      if (variable.getName().equals(ServiceCommandLineGenerator.ENV_HADOOP_OPTS)){
        yarnOpts = variable.getValue();
      }
    }
    assertTrue("Environment contains " + ServiceCommandLineGenerator.ENV_HADOOP_OPTS, StringUtils.isNotEmpty(yarnOpts));
    System.out.println(yarnOpts);
    assertTrue(ServiceCommandLineGenerator.ENV_HADOOP_OPTS + " must contain -D" + KEY_JHS_WEBAPP_ADDRESS +
        "=0.0.0.0:3", yarnOpts.contains(KEY_JHS_WEBAPP_ADDRESS + "=0.0.0.0:3"));
  }

  @Test
  public void testNMCommandLineGeneration() throws Exception {
    Long[] ports = new Long[]{1L, 2L, 3L, 4L};
    List<Long> nmPorts = Arrays.asList(ports);
    Map<String, Long> portsMap = new TreeMap<>();
    portsMap.put(KEY_NM_ADDRESS, 0L);
    portsMap.put(KEY_NM_WEBAPP_ADDRESS, 0L);
    portsMap.put(KEY_NM_LOCALIZER_ADDRESS, 0L);
    portsMap.put(KEY_NM_SHUFFLE_PORT, 0L);

    ServiceResourceProfile profile = new ExtendedResourceProfile(new NMProfile("nm", 10L, 15L), 3.0, 5.0, portsMap);

    ExecutorCommandLineGenerator clGenerator = new NMExecutorCommandLineGenerator(cfg);

    CommandInfo cInfo = clGenerator.generateCommandLine(profile, null, nmPorts);
    String testVal =  String.format(CMD_FORMAT, toCompare);
    assertTrue(String.format(msgFormat, cInfo.getValue(), testVal),
        cInfo.getValue().equals(testVal));

    List<Protos.Environment.Variable> environmentList = cInfo.getEnvironment().getVariablesList();
    String yarnOpts = "";
    for (Protos.Environment.Variable variable: environmentList) {
      if (variable.getName().equals(NMExecutorCommandLineGenerator.ENV_YARN_NODEMANAGER_OPTS)){
        yarnOpts = variable.getValue();
      }
    }
    System.out.println(yarnOpts);
    assertTrue("Environment contains " + NMExecutorCommandLineGenerator.ENV_YARN_NODEMANAGER_OPTS, StringUtils.isNotEmpty(yarnOpts));
    assertTrue(NMExecutorCommandLineGenerator.ENV_YARN_NODEMANAGER_OPTS + " must contain -D" + KEY_NM_SHUFFLE_PORT +
        "=1", yarnOpts.contains(KEY_NM_SHUFFLE_PORT + "=1"));
  }

}
