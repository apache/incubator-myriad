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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler;


import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.Value.Range;
import org.apache.mesos.Protos.Value.Ranges;
import org.apache.myriad.scheduler.TaskFactory.NMTaskFactoryImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test Class to test NM ports randomization
 *
 */
public class TestRandomPorts {

  
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testRandomPorts() {
    Range range1 = Range.newBuilder().setBegin(100).setEnd(200).build();
    Range range2 = Range.newBuilder().setBegin(250).setEnd(300).build();
    Range range3 = Range.newBuilder().setBegin(310).setEnd(500).build();
    Range range4 = Range.newBuilder().setBegin(520).setEnd(720).build();
    Range range5 = Range.newBuilder().setBegin(750).setEnd(1000).build();
    
    Ranges ranges = Ranges.newBuilder().addRange(range1)
        .addRange(range2)
        .addRange(range3)
        .addRange(range4)
        .addRange(range5).build();
    
    
    Resource resource = Resource.newBuilder().setType(Protos.Value.Type.RANGES).setRanges(ranges).setName("ports").build();
    
    Set<Long> ports = NMTaskFactoryImpl.getNMPorts(resource);
    
    assertEquals(NMPorts.expectedNumPorts(), ports.size());
    List<Long> sortedList = Lists.newArrayList(ports);
    
    Collections.sort(sortedList);
    
    for (Long port : sortedList) {
      assertTrue((port >= 100 && port <= 200) ||
          (port >= 250 && port <= 300) ||
          (port >= 310 && port <= 500) ||
          (port >= 520 && port <= 720) ||
          (port >= 750 && port <= 1000));
    }
  }

  @Test
  public void testRandomPortsNotEnough() {
    Range range1 = Range.newBuilder().setBegin(100).setEnd(200).build();
    Range range2 = Range.newBuilder().setBegin(250).setEnd(300).build();
    
    Ranges ranges = Ranges.newBuilder().addRange(range1)
        .addRange(range2)
        .build();
    
    
    Resource resource = Resource.newBuilder().setType(Protos.Value.Type.RANGES).setRanges(ranges).setName("ports").build();
    
    Set<Long> ports = NMTaskFactoryImpl.getNMPorts(resource);
    
    assertEquals(NMPorts.expectedNumPorts(), ports.size());
    List<Long> sortedList = Lists.newArrayList(ports);
    
    Collections.sort(sortedList);

    for (Long port : sortedList) {
      assertTrue((port >= 100 && port <= 200) ||
          (port >= 250 && port <= 300));
    }    
  }

  @Test
  public void testRandomPortsNotEnoughPercentKickIn() {
    Range range1 = Range.newBuilder().setBegin(100).setEnd(200).build();
    Range range2 = Range.newBuilder().setBegin(250).setEnd(335).build();
    
    Ranges ranges = Ranges.newBuilder().addRange(range1)
        .addRange(range2)
        .build();
    
    
    Resource resource = Resource.newBuilder().setType(Protos.Value.Type.RANGES).setRanges(ranges).setName("ports").build();
    
    Set<Long> ports = NMTaskFactoryImpl.getNMPorts(resource);
    
    assertEquals(NMPorts.expectedNumPorts(), ports.size());
    List<Long> sortedList = Lists.newArrayList(ports);
    
    Collections.sort(sortedList);

    for (int i = 0; i < sortedList.size(); i++) {
      assertTrue((sortedList.get(i) >= 100 && sortedList.get(i) <= 200) ||
          (sortedList.get(i) >= 250 && sortedList.get(i) <= 335));
    }
  }
  
  @Test
  public void testRandomPortsLargeRange() {
    Range range1 = Range.newBuilder().setBegin(100).setEnd(500).build();
    Range range2 = Range.newBuilder().setBegin(550).setEnd(835).build();
    
    Ranges ranges = Ranges.newBuilder().addRange(range1)
        .addRange(range2)
        .build();
    
    
    Resource resource = Resource.newBuilder().setType(Protos.Value.Type.RANGES).setRanges(ranges).setName("ports").build();
    
    Set<Long> ports = NMTaskFactoryImpl.getNMPorts(resource);
    
    assertEquals(NMPorts.expectedNumPorts(), ports.size());
    List<Long> sortedList = Lists.newArrayList(ports);
    
    Collections.sort(sortedList);

    for (int i = 0; i < sortedList.size(); i++) {
      assertTrue((sortedList.get(i) >= 100 && sortedList.get(i) <= 500) || 
          (sortedList.get(i) >= 550 && sortedList.get(i) <= 835));
    }
  }

  @Test
  public void testRandomPortsSmallRange() {
    Range range1 = Range.newBuilder().setBegin(100).setEnd(100).build();
    Range range2 = Range.newBuilder().setBegin(110).setEnd(115).build();
    
    Ranges ranges = Ranges.newBuilder().addRange(range1)
        .addRange(range2)
        .build();
    
    Resource resource = Resource.newBuilder().setType(Protos.Value.Type.RANGES).setRanges(ranges).setName("ports").build();
    
    Set<Long> ports = NMTaskFactoryImpl.getNMPorts(resource);
    
    assertEquals(NMPorts.expectedNumPorts(), ports.size());
    List<Long> sortedList = Lists.newArrayList(ports);
    
    Collections.sort(sortedList);

    for (int i = 0; i < sortedList.size(); i++) {
      assertTrue(sortedList.get(i) == 100 || (sortedList.get(i) <= 115 && sortedList.get(i) >= 110));
    }
  }
  
  @Test
  public void notEnoughPorts() throws Exception {
    Range range1 = Range.newBuilder().setBegin(100).setEnd(100).build();
    Range range2 = Range.newBuilder().setBegin(110).setEnd(111).build();
    
    Ranges ranges = Ranges.newBuilder().addRange(range1)
        .addRange(range2)
        .build();
    
    Resource resource = Resource.newBuilder().setType(Protos.Value.Type.RANGES).setRanges(ranges).setName("ports").build();
    
    try {
      NMTaskFactoryImpl.getNMPorts(resource);
      fail("Should fail, as number of ports is not enough");
    } catch (IllegalStateException ise) {
      // should get here
    }

  }
}
