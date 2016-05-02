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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for TaskUtils
 */
public class TestTaskUtils {

  static MyriadConfiguration cfg;
  static MyriadConfiguration cfgWithRole;
  static MyriadConfiguration cfgWithDocker;
  static double epsilon = .0001;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
        MyriadConfiguration.class);
    cfgWithRole = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default-with-framework-role.yml"),
        MyriadConfiguration.class);
    cfgWithDocker = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default-with-docker-info.yml"),
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

    Gson gson = new GsonBuilder().registerTypeAdapter(ServiceResourceProfile.class, new ServiceResourceProfile.CustomDeserializer())
        .create();

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
    Range defaultValueRange = Ranges.closed(defaultVal - epsilon, defaultVal + epsilon);
    Range roleValueRange = Ranges.closed(roleVal - epsilon, roleVal + epsilon);

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

  @Test
  public void testContainerInfo() {
    TaskUtils taskUtils = new TaskUtils(cfgWithDocker);
    Protos.ContainerInfo containerInfo = taskUtils.getContainerInfo();
    assertTrue("The container should have a docker", containerInfo.hasDocker());
    assertTrue("There should be two volumes", containerInfo.getVolumesCount() == 2);
    assertTrue("The first volume should be read only", containerInfo.getVolumes(0).getMode().equals(Protos.Volume.Mode.RO));
    assertTrue("The first volume should be read write", containerInfo.getVolumes(1).getMode().equals(Protos.Volume.Mode.RW));
  }

  @Test public void testDockerInfo() {
    TaskUtils taskUtils = new TaskUtils(cfgWithDocker);
    Protos.ContainerInfo containerInfo = taskUtils.getContainerInfo();
    assertTrue("The container should have a docker", containerInfo.hasDocker());
    assertTrue("There should be two volumes", containerInfo.getVolumesList().size() == 2);
    assertTrue("There should be a docker image", containerInfo.getDocker().hasImage());
    assertTrue("The docker image should be mesos/myraid", containerInfo.getDocker().getImage().equals("mesos/myriad"));
    assertTrue("Should be using host networking", containerInfo.getDocker().getNetwork().equals(Protos.ContainerInfo.DockerInfo.Network.HOST));
    assertTrue("There should be two parameters", containerInfo.getDocker().getParametersList().size() == 2);
    assertTrue("Privledged mode should be false", containerInfo.getDocker().getPrivileged() == false);
  }
}
