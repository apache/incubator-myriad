package org.apache.hadoop.yarn.server.resourcemanager.recovery;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import static org.junit.Assert.assertTrue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.Service.STATE;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppState;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.state.MockRMApp;
import org.junit.Test;

/**
 * Unit tests for MyriadFileSystemRMStateStore
 */
public class MyriadFileSystemRMStateStoreTest {

  @Test
  public void testInit() throws Exception {
    Configuration conf = new Configuration();
    conf.set("yarn.resourcemanager.fs.state-store.uri", "file:///" + "/tmp/myriad-file-system-rm-state-store-test");
    MyriadFileSystemRMStateStore store = new MyriadFileSystemRMStateStore();
    assertTrue(store.isInState(STATE.NOTINITED));
    store.init(conf);
    assertTrue(store.isInState(STATE.INITED));
    store.start();
    assertTrue(store.isInState(STATE.STARTED));
    store.close();
    assertTrue(store.isInState(STATE.STOPPED));
  }

  @Test
  public void testStartStop() throws Exception {
    MyriadFileSystemRMStateStore store = getInitializedStore();
    store.start();    
    assertTrue(store.isInState(STATE.STARTED));
    store.stop();
    assertTrue(store.isInState(STATE.STOPPED));
    store.close();
  }

  @Test
  public void testStoreAndRemoveApplication() throws Exception {
    MyriadFileSystemRMStateStore store = getInitializedStore();
    store.start();
    RMApp appOne = new MockRMApp(0, 0, RMAppState.NEW);
    RMApp appTwo = new MockRMApp(0, 0, RMAppState.NEW);
  
    store.storeNewApplication(appOne);
    store.storeNewApplication(appTwo);
    store.removeApplication(appOne);
    store.removeApplication(appTwo);
    store.close();
  }

  private MyriadFileSystemRMStateStore getInitializedStore() throws Exception {
    MyriadFileSystemRMStateStore store = TestObjectFactory.getStateStore(new Configuration(), "/tmp/myriad-file-system-rm-state-store-test");
    store.loadMyriadState();
    return store;
  }
}
