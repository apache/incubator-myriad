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

package org.apache.myriad.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.myriad.BaseConfigurableTest;
import org.junit.Test;

import com.google.common.collect.Sets;

/**
 * Unit tests for MyriadConfiguration
 */
public class MyriadConfigurationTest extends BaseConfigurableTest {

  public void testMyriadContainerConfiguration() throws Exception {
    MyriadContainerConfiguration conf = cfgWithDocker.getContainerInfo().get();
    assertTrue(conf.getDockerInfo().isPresent());

    MyriadDockerConfiguration dConf = conf.getDockerInfo().get();
    assertEquals(false, dConf.getForcePullImage());
    assertEquals("mesos/myriad", dConf.getImage());

    assertNotNull(conf.getVolumes());
    
    Set<String> keys = Sets.newHashSet("hostPath", "containerPath", "mode");
    Set<String> modes = Sets.newHashSet("RO", "RW");
    Iterator<Map<String, String>> iter = conf.getVolumes().iterator();
    
    while (iter.hasNext()) {
      Map<String, String> mcConf = iter.next();
      assertEquals(keys, mcConf.keySet());
      assertTrue(modes.contains(mcConf.get("mode")));
    }
  }
  
  @Test
  public void testRoles() throws Exception {
    assertEquals("test", cfgWithRole.getFrameworkRole());
    assertEquals("*", cfg.getFrameworkRole());
  }

  @Test
  public void testExecutorConfiguration() throws Exception {
    MyriadExecutorConfiguration conf = cfg.getMyriadExecutorConfiguration();

    assertEquals(new Double(256), conf.getJvmMaxMemoryMB());
    assertEquals("hdfs://namenode:port/dist/hadoop-2.7.0.tar.gz", conf.getNodeManagerUri().get());
    assertEquals("file:///usr/local/libexec/mesos/myriad-executor-runnable-0.1.0.jar", conf.getPath());
  }

  @Test
  public void testServiceConfigurations() throws Exception {
    Map<String, ServiceConfiguration> confs = cfg.getServiceConfigurations();
    Set<String> configKeys = Sets.newHashSet("jobhistory", "timelineserver");

    assertEquals(configKeys, confs.keySet());
    ServiceConfiguration sConfig = confs.get("jobhistory");
    assertEquals(new Double(1.0), sConfig.getCpus());
    assertEquals("jobhistory", sConfig.getTaskName());
  }

  @Test
  public void testNodeManagerConfiguration() throws Exception {
    NodeManagerConfiguration config = cfg.getNodeManagerConfiguration();

    assertFalse(config.getCgroups());
    assertEquals(new Double(0.8), config.getCpus());
    assertEquals(new Double(2048.0), config.getJvmMaxMemoryMB());
    assertEquals(new Double(4.0), config.getMaxCpus());
  }
  
  @Test
  public void testProfilesConfiguration() throws Exception {
    Map<String, Map<String, String>> profiles = cfg.getProfiles();

    for (Map.Entry<String, Map<String, String>> profile : profiles.entrySet()) {
      assertTrue(validateProfile(profile));
    }
  }
  
  private boolean validateProfile(Map.Entry<String, Map<String, String>> entry) {
    String key = entry.getKey();
    Map<String, String> value = entry.getValue();

    switch (key) {
      case "small" : {
        return value.get("cpu").equals("1") && value.get("mem").equals("1100");
      }

      case "medium" : {
        return value.get("cpu").equals("2") && value.get("mem").equals("2048");
      }

      case "large" : {
        return value.get("cpu").equals("4") && value.get("mem").equals("4096");
      } 

      default : {
        return true;
      }
    }
  }
}