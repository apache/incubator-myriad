package org.apache.myriad.scheduler;

import org.junit.Assert;
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
    Assert.assertEquals(5, manager.numberOfProfiles());
  }
  
  @Test 
  public void testRetrieval() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();   
    Assert.assertEquals("profile1", manager.get("profile1").getName());
    Assert.assertEquals("profile2", manager.get("profile2").getName());
    Assert.assertEquals("profile3", manager.get("profile3").getName());
    Assert.assertEquals("profile4", manager.get("profile4").getName());
    Assert.assertEquals("profile5", manager.get("profile5").getName());
  }

  @Test
  public void testExists() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();
    Assert.assertTrue(manager.exists("profile1"));
    Assert.assertTrue(manager.exists("profile2"));
    Assert.assertTrue(manager.exists("profile3"));
    Assert.assertTrue(manager.exists("profile4"));
    Assert.assertTrue(manager.exists("profile5"));
  }
  @Test
  public void testToString() throws Exception {
    NMProfileManager manager = this.getNMProfileManager();
    String toString = manager.toString();
    Assert.assertTrue(toString.contains("\"name\":\"profile1\",\"cpus\":1,\"memory\":512"));
    Assert.assertTrue(toString.contains("\"name\":\"profile2\",\"cpus\":2,\"memory\":1024"));
    Assert.assertTrue(toString.contains("\"name\":\"profile3\",\"cpus\":3,\"memory\":2048"));
    Assert.assertTrue(toString.contains("\"name\":\"profile4\",\"cpus\":4,\"memory\":3072"));
    Assert.assertTrue(toString.contains("\"name\":\"profile5\",\"cpus\":5,\"memory\":4096"));
  }
}