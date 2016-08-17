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
package org.apache.myriad.scheduler;

import static org.junit.Assert.assertTrue;
import org.apache.mesos.Protos;
import org.apache.myriad.BaseConfigurableTest;
import org.junit.Test;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;


/**
 * Tests for TaskUtils
 */
public class TestTaskUtils extends BaseConfigurableTest {
  static double epsilon = .0001;

  @Test
  public void testServiceResourceProfile() throws Exception {
    // testing custom deserializer

    Gson gson = new GsonBuilder().registerTypeAdapter(ServiceResourceProfile.class, new ServiceResourceProfile.CustomDeserializer())
        .create();

    ServiceResourceProfile parentProfile = new ServiceResourceProfile("abc", 1.0, 100.0, new HashMap<String, Long>());

    String parentStr = gson.toJson(parentProfile);
    ServiceResourceProfile processedProfile = gson.fromJson(parentStr, ServiceResourceProfile.class);

    assertTrue(processedProfile.getClass().equals(ServiceResourceProfile.class));
    assertTrue(processedProfile.toString().equalsIgnoreCase(parentStr));

    ServiceResourceProfile childProfile = new ExtendedResourceProfile(new NMProfile("bcd", 5L, 15L), 2.0, 7.0,
        cfg.getNodeManagerConfiguration().getPorts());

    String childStr = gson.toJson(childProfile);
    ServiceResourceProfile processedChildProfile = gson.fromJson(childStr, ServiceResourceProfile.class);

    assertTrue(processedChildProfile instanceof ExtendedResourceProfile);
    assertTrue(processedChildProfile.toString().equalsIgnoreCase(childStr));
  }

  @Test

  public void testStackTrace() {

    new Throwable().printStackTrace();
  }
  private Protos.Offer createScalarOffer(String name, double roleVal, double defaultVal) {
    Protos.Offer offer = Protos.Offer.newBuilder()
        .setId(Protos.OfferID.newBuilder().setValue("offerId"))
        .setSlaveId(Protos.SlaveID.newBuilder().setValue("slaveId"))
        .setHostname("test.com")
        .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("frameworkId"))
        .addResources(
            Protos.Resource.newBuilder()
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(roleVal))
                .setType(Protos.Value.Type.SCALAR)
                .setName(name)
                .setRole("test")
                .build())
        .addResources(
            Protos.Resource.newBuilder()
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(defaultVal))
                .setType(Protos.Value.Type.SCALAR)
                .setName("cpus")
                .build())
        .build();
    return offer;
  }

  private void checkResourceList(Iterable<Protos.Resource> resources, String name, Double roleVal, Double defaultVal) {
    int i = 0;

    Range<Double> defaultValueRange = Ranges.closed(defaultVal - epsilon, defaultVal + epsilon);
    Range<Double> roleValueRange    = Ranges.closed(roleVal - epsilon, roleVal + epsilon);

    for (Protos.Resource resource: resources) {
      if (resource.hasRole() && resource.getRole().equals("test")) {
        double v = resource.getScalar().getValue();
        assertTrue("Test Role  has " + v + " " + name + " should have " + roleVal, roleValueRange.contains(v));
        i++;
      } else {
        double v = resource.getScalar().getValue();
        assertTrue("Default Role has " + v + " " + name + " should have " + defaultVal , defaultValueRange.contains(v));
        i++;
      }
    }
    assertTrue("There should be at most 2 resources", i <= 2);
  }

  @Test
  public void testGetScalarResourcesWithRole() {
    TaskUtils taskUtils = new TaskUtils(cfgWithRole);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 3.0, 2.0), "cpus", 1.0, 0.0), "cpus", 1.0, 0.0);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 0.0, 2.0), "cpus", 1.0, 1.0), "cpus", 0.0, 1.0);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 1.5, 2.0), "cpus", 2.0, 1.0), "cpus", 0.5, 1.5);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 1.5, 2.0), "cpus", 1.5, 2.0), "cpus", 0.0, 1.5);
  }

  @Test
  public void testGetScalarResources() {
    TaskUtils taskUtils = new TaskUtils(cfg);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 0.0, 2.0), "cpus", 1.0, 0.0), "cpus", 0.0, 1.0);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 0.0, 2.0), "cpus", 1.0, 1.0), "cpus", 0.0, 1.0);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 0.0, 2.0), "cpus", 1.0, 1.0), "cpus", 0.0, 1.0);
    checkResourceList(taskUtils.getScalarResource(createScalarOffer("cpus", 0.0, 2.0), "cpus", 0.5, 1.5), "cpus", 0.0, 0.5);
  }
}