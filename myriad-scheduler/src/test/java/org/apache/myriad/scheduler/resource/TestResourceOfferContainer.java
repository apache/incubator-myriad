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
package org.apache.myriad.scheduler.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.ExtendedResourceProfile;
import org.apache.myriad.scheduler.NMProfile;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.offer.OfferBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ResourceOfferContainerClass
 */
public class TestResourceOfferContainer {
  static MyriadConfiguration cfg;
  static double epsilon = .0001;


  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
        MyriadConfiguration.class);
  }

  @Test
  public void testResouceOfferContainerForNMWithOutRole() {
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 4.0)
        .addScalarResource("mem", 8000)
        .addRangeResource("ports", 3500, 3600)
        .build();
    Map<String, Long> ports = new HashMap<>(4);
    ports.put("test1.address", 0L);
    ports.put("test2.address", 0L);
    ports.put("test3.address", 0L);
    ports.put("test4.port", 3501L);
    ServiceResourceProfile profile1 = new ExtendedResourceProfile(new NMProfile("small", 2L, 6000L), .2, 1024.0, ports);
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile1, "");
    System.out.print(roc.getCpus());
    System.out.print(roc.getMem());
    System.out.print(roc.getPorts());
    assertTrue(roc.getHostName().equals("test.com"));
    assertTrue("Should be satisfied if offer contains request", roc.satisfies(profile1));
    ServiceResourceProfile profile2 = new ExtendedResourceProfile(new NMProfile("tooMuchCpu", 7L, 8000L), .2, 1024.0, ports);
    roc = new ResourceOfferContainer(offer, profile2, "");
    assertFalse("Should be unsatisfied if too much cpu requested", roc.satisfies(profile2));
    ServiceResourceProfile profile3 = new ExtendedResourceProfile(new NMProfile("tooMuchMem", 3L, 50000L), .2, 1024.0, ports);
    roc = new ResourceOfferContainer(offer, profile3, "");
    assertFalse("Should be unsatisfied if too much memory requested", roc.satisfies(profile3));
    ports.put("test.bad.address", 1500L);
    ServiceResourceProfile profile4 = new ExtendedResourceProfile(new NMProfile("portOutOfRange", 3L, 50000L), .2, 1024.0, ports);
    roc = new ResourceOfferContainer(offer, profile4, "");
    assertFalse("Should be unsatisfied if port not in range", roc.satisfies(profile4));
    List<Protos.Resource> resourcesCpu = roc.consumeCpus(3.0);
    assertTrue("Should get a list of resources of size 1", resourcesCpu.size() == 1.0);
    assertTrue("Cpus should be decreased", roc.getCpus() == 1.0);
    List<Protos.Resource> resourcesMem = roc.consumeMem(7000.0);
    assertTrue("Should get a list of resources of size 1", resourcesMem.size() == 1);
    assertTrue("Mem should be decreased", roc.getMem() == 1000.0);
  }

  @Test
  public void testResouceOfferContainerForNMWithRole() {
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 2.0)
        .addScalarResource("mem", 8000)
        .addScalarResource("cpus", "test", 4.0)
        .addScalarResource("mem", "test", 32000.0)
        .addRangeResource("ports", 3500, 3600)
        .addRangeResource("ports", "test", 1500, 1600)
        .build();
    Map<String, Long> ports = new HashMap<>(4);
    ports.put("test1.address", 0L);
    ports.put("test2.address", 0L);
    ports.put("test3.address", 1500L);
    ports.put("test4.port", 3502L);
    ServiceResourceProfile profile1 = new ExtendedResourceProfile(new NMProfile("small", 2L, 8000L), .2, 1024.0, ports);
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile1, "test");
    assertTrue(roc.getHostName().equals("test.com"));
    assertTrue("Should be satisfied if offer contains request", roc.satisfies(profile1));
    ServiceResourceProfile profile2 = new ExtendedResourceProfile(new NMProfile("tooMuchCpu", 7L, 8000L), .2, 1024.0, ports);
    roc = new ResourceOfferContainer(offer, profile2, "test");
    assertFalse("Should be unsatisfied if too much cpu requested", roc.satisfies(profile2));
    ServiceResourceProfile profile3 = new ExtendedResourceProfile(new NMProfile("tooMuchMem", 3L, 50000L), .2, 1024.0, ports);
    roc = new ResourceOfferContainer(offer, profile3, "test");
    assertFalse("Should be unsatisfied if too much memory requested", roc.satisfies(profile3));
    ports.put("test.bad.address", 32000L);
    ServiceResourceProfile profile4 = new ExtendedResourceProfile(new NMProfile("portOutOfRange", 3L, 50000L), .2, 1024.0, ports);
    roc = new ResourceOfferContainer(offer, profile4, "test");
    assertFalse("Should be unsatisfied if port not in range", roc.satisfies(profile4));
    List<Protos.Resource> resources = roc.consumeCpus(4.5);
    assertTrue("Resource List should be of size to when requesting 4.1 cpus", (resources.size() == 2));
    assertTrue("Cpus should be decreased", roc.getCpus() <= 1.5);
    List<Protos.Resource> resources1 = roc.consumeCpus(1.5);
    assertTrue("Resource List should be of size 1", resources1.size() == 1);
    assertTrue("All cpu resources should be consumed", roc.getCpus() <= 0.0);
  }

  @Test
  public void testResourceOfferContainerForAuxServiceWithOutRole() {
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 2.0)
        .addScalarResource("mem", 8000)
        .addRangeResource("ports", 3500, 3600)
        .build();
    Map<String, Long> ports = new HashMap<>(4);
    ports.put("test1.address", 0L);
    ports.put("test2.address", 0L);
    ports.put("test3.address", 0L);
    ports.put("test4.port", 3501L);
    ServiceResourceProfile profile = new ServiceResourceProfile("jobhistory", 2.0, 8000.0, ports);
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile, null);
    assertTrue(roc.getHostName().equals("test.com"));
    assertTrue("Should be satisfied if offer contains request", roc.satisfies(profile));
  }

  @Test
  public void testResourceOfferContainerForAuxServiceWithRole() {
    Protos.Offer offer = new OfferBuilder("test.com")
        .addScalarResource("cpus", 2.0)
        .addScalarResource("mem", 8000)
        .addScalarResource("cpus", "test", 4.0)
        .addScalarResource("mem", "test", 32000.0)
        .addRangeResource("ports", 3500, 3600)
        .addRangeResource("ports", "test", 1500, 1600)
        .build();
    Map<String, Long> ports = new HashMap<>(4);
    ports.put("test1.address", 0L);
    ports.put("test2.address", 0L);
    ports.put("test3.address", 1500L);
    ports.put("test4.port", 3501L);
    ServiceResourceProfile profile = new ServiceResourceProfile("jobhistory", 2.0, 8000.0, ports);
    ResourceOfferContainer roc = new ResourceOfferContainer(offer, profile, null);
    assertTrue(roc.getHostName().equals("test.com"));
    assertTrue("Should be satisfied if offer contains request", roc.satisfies(profile));
  }
}
