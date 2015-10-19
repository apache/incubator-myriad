/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ebay.myriad.configuration;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * AuxServices/tasks test
 *
 */
public class MyriadConfigurationTest {

  static MyriadConfiguration cfg;
  
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
  public void additionalPropertiestest() throws Exception {
    
    Map<String, ServiceConfiguration> auxConfigs = cfg.getServiceConfigurations();
    
    assertNotNull(auxConfigs);
    assertEquals(auxConfigs.size(), 2);
    
    for (Map.Entry<String, ServiceConfiguration> entry : auxConfigs.entrySet()) {
      String taskName = entry.getKey();
      ServiceConfiguration config = entry.getValue();
      String outTaskname = config.getTaskName();
      assertEquals(taskName, outTaskname);
    }
  }

}
