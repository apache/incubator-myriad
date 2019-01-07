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

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Unit test cases for ExtendedResourceProfile
 *
 */
public class ExtendedResourceProfileTest {
  @Test
  public void testExendedResourceProfile() throws Exception {
    double nmCpus = 0.2;
    double nmMem = 512;

    NMProfile profile1 = new NMProfile("profile1", 2L, 2048L);
    NMProfile profile2 = new NMProfile("profile2", 5L, 5120L, 1);
    NMProfile profile3 = new NMProfile("profile3", 5L, 5120L, 0.5);

    ExtendedResourceProfile serviceProfile1 = new ExtendedResourceProfile(
        profile1, nmCpus, nmMem, null);
    ExtendedResourceProfile serviceProfile2 = new ExtendedResourceProfile(
        profile2, nmCpus, nmMem, null);
    ExtendedResourceProfile serviceProfile3 = new ExtendedResourceProfile(
        profile3, nmCpus, nmMem, null);

    assertEquals(2.2, serviceProfile1.getAggregateCpu(), 0.01);
    assertEquals(2.0, serviceProfile1.getCpus(), 0.01);
    assertEquals(2, serviceProfile1.getVcores());

    assertEquals(5.2, serviceProfile2.getAggregateCpu(), 0.01);
    assertEquals(5.0, serviceProfile2.getCpus(), 0.01);
    assertEquals(5, serviceProfile2.getVcores());

    assertEquals(2.7, serviceProfile3.getAggregateCpu(), 0.01);
    assertEquals(2.5, serviceProfile3.getCpus(), 0.01);
    assertEquals(5, serviceProfile3.getVcores());
  }
}