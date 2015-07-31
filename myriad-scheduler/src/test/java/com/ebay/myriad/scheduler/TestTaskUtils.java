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
package com.ebay.myriad.scheduler;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ebay.myriad.configuration.MyriadBadConfigurationException;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests for TaskUtils
 *
 */
public class TestTaskUtils {

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
  public void testGetResource() {
    TaskUtils taskUtils = new TaskUtils(cfg);
    
    NMProfile fooProfile = new NMProfile("abc", 1L, 1000L);
    try {
      taskUtils.getAuxTaskCpus(fooProfile, "foo");
      fail("Should not complete sucessfully for foo");
    } catch (MyriadBadConfigurationException e) {
      // success
    }
    
    try {
      double cpu = taskUtils.getAuxTaskCpus(fooProfile, "jobhistory");
      assertTrue(cpu > 0.0);
    } catch (MyriadBadConfigurationException e) {
      fail("cpu should be defined for jobhistory");
    }    
  }
  
  @Test
  public void testServiceResourceProfile() throws Exception {
    // testing custom deserializer
    
    Gson gson = new GsonBuilder().registerTypeAdapter(ServiceResourceProfile.class, new ServiceResourceProfile.CustomDeserializer()).create();
    

    ServiceResourceProfile parentProfile = new ServiceResourceProfile("abc", 1.0, 100.0);

    String parentStr = gson.toJson(parentProfile);
    ServiceResourceProfile processedProfile = gson.fromJson(parentStr, ServiceResourceProfile.class);
    
    assertTrue(processedProfile.getClass().equals(ServiceResourceProfile.class));
    assertTrue(processedProfile.toString().equalsIgnoreCase(parentStr));
    
    ServiceResourceProfile childProfile = new ExtendedResourceProfile(new NMProfile("bcd", 5L, 15L), 2.0, 7.0);
    
    String childStr = gson.toJson(childProfile);
    ServiceResourceProfile processedChildProfile = gson.fromJson(childStr, ServiceResourceProfile.class);

    assertTrue(processedChildProfile instanceof ExtendedResourceProfile);
    assertTrue(processedChildProfile.toString().equalsIgnoreCase(childStr));
  }

  @Test
  
  public void testStackTrace() {
    
    new Throwable().printStackTrace();
  }
}
