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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit test cases for NMProfileManager
 */
public class NMProfileManagerTest { 
  private NMProfileManager getNMProfileManager() {
    NMProfileManager manager = new NMProfileManager();
    NMProfile profile1 = new NMProfile("profile1", 1L, 512L);
    NMProfile profile2 = new NMProfile("profile2", 2L, 1024L);
    NMProfile profile3 = new NMProfile("profile3", 3L, 2048L);
    NMProfile profile4 = new NMProfile("profile4", 4L, 3072L);
    NMProfile profile5 = new NMProfile("profile5", 5L, 4096L);

    manager.add(profile1);
    manager.add(profile2);
    manager.add(profile3);
    manager.add(profile4);
    manager.add(profile5);

    return manager;
  }

  @Test
  public void testAdd() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();
    assertEquals(5, manager.numberOfProfiles());
  }
  
  @Test 
  public void testRetrieval() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();   
    assertEquals("profile1", manager.get("profile1").getName());
    assertEquals("profile2", manager.get("profile2").getName());
    assertEquals("profile3", manager.get("profile3").getName());
    assertEquals("profile4", manager.get("profile4").getName());
    assertEquals("profile5", manager.get("profile5").getName());
  }

  @Test
  public void testExists() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();
    assertTrue(manager.exists("profile1"));
    assertTrue(manager.exists("profile2"));
    assertTrue(manager.exists("profile3"));
    assertTrue(manager.exists("profile4"));
    assertTrue(manager.exists("profile5"));
  }
  @Test
  public void testToString() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();
    String toString = manager.toString();
    assertTrue(toString.contains("\"name\":\"profile1\",\"cpus\":1,\"memory\":512"));
    assertTrue(toString.contains("\"name\":\"profile2\",\"cpus\":2,\"memory\":1024"));
    assertTrue(toString.contains("\"name\":\"profile3\",\"cpus\":3,\"memory\":2048"));
    assertTrue(toString.contains("\"name\":\"profile4\",\"cpus\":4,\"memory\":3072"));
    assertTrue(toString.contains("\"name\":\"profile5\",\"cpus\":5,\"memory\":4096"));
  }
}