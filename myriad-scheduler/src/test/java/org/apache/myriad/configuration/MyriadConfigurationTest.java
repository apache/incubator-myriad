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

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * AuxServices/tasks test
 */
public class MyriadConfigurationTest {

  static MyriadConfiguration cfg;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
        MyriadConfiguration.class);

  }

  @Test
  public void serviceConfigurationTest() throws Exception {  
    Map<String, ServiceConfiguration> auxConfigs = cfg.getServiceConfigurations();

    assertEquals(auxConfigs.size(), 2);

    for (Map.Entry<String, ServiceConfiguration> entry : auxConfigs.entrySet()) {
      String taskName = entry.getKey();
      ServiceConfiguration config = entry.getValue();
      String outTaskname = config.getTaskName();
      assertEquals(taskName, outTaskname);
    }
  }

  @Test
  public void coreConfigurationTest() throws Exception {
    assertEquals("MyriadTest", cfg.getFrameworkName());

    //authorization parameters
    assertEquals("*", cfg.getFrameworkRole());
    assertEquals("hduser", cfg.getFrameworkUser().get());
    assertEquals("root", cfg.getFrameworkSuperUser().get());

    //ports and directory paths
    assertEquals("10.0.2.15:5050", cfg.getMesosMaster());
    assertEquals("/usr/local/lib/libmesos.so", cfg.getNativeLibrary());
    assertEquals(new Integer(8192), cfg.getRestApiPort());
    assertEquals("10.0.2.15:2181", cfg.getZkServers());
  
    //timeouts
    assertEquals(new Double(44200000), cfg.getFrameworkFailoverTimeout());
    assertEquals(new Integer(25000), cfg.getZkTimeout());
  
    //checkpoints
    assertEquals(false, cfg.isCheckpoint());
    assertEquals(true, cfg.isHAEnabled());
    assertEquals(true, cfg.isRebalancerEnabled());
  }
  
  @Test
  public void executorConfigurationTest() throws Exception {
    MyriadExecutorConfiguration conf = cfg.getMyriadExecutorConfiguration();

    assertEquals(new Double(256), conf.getJvmMaxMemoryMB());
    assertEquals("hdfs://namenode:port/dist/hadoop-2.7.0.tar.gz", conf.getNodeManagerUri().get());
    assertEquals("file:///usr/local/libexec/mesos/myriad-executor-runnable-0.1.0.jar", conf.getPath());
  }

  @Test
  public void nodeManagerConfigurationTest() throws Exception {
    NodeManagerConfiguration config = cfg.getNodeManagerConfiguration();

    assertFalse(config.getCgroups());
    assertEquals(new Double(0.2), config.getCpus());
    assertEquals(new Double(1024.0), config.getJvmMaxMemoryMB());
  }
  
  @Test
  public void profilesConfigurationTest() throws Exception {
    Map<String, Map<String, String>> profiles = cfg.getProfiles();

    for (Map.Entry<String, Map<String, String>> profile : profiles.entrySet()) {
      assertTrue(validateProfile(profile));
    }
  }
  
  private boolean validateProfile(Map.Entry<String, Map<String, String>> entry) {
    String key                 = entry.getKey();
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