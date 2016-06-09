package org.apache.hadoop.yarn.server.resourcemanager.recovery;

import static org.junit.Assert.assertTrue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.Service.STATE;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppState;
import org.apache.myriad.state.MockDispatcher;
import org.apache.myriad.state.MockRMApp;
import org.junit.Test;

/**
 * Unit tests for MyriadFileSystemRMStateStore
 */
public class MyriadFileSystemRMStateStoreTest {

  @Test
  public void testInit() throws Exception {
    Configuration conf = getConfiguration();
    MyriadFileSystemRMStateStore store = new MyriadFileSystemRMStateStore();
    assertTrue(store.isInState(STATE.NOTINITED));
    store.init(conf);
    assertTrue(store.isInState(STATE.INITED));
    store.close();
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
    Configuration conf = getConfiguration();
    MyriadFileSystemRMStateStore store = new MyriadFileSystemRMStateStore();
    store.init(conf);
    store.loadState();   
    store.loadMyriadState();
    store.setRMDispatcher(new MockDispatcher());
    return store;
  }
  
  private Configuration getConfiguration() {
    Configuration conf = new Configuration();
    conf.set("yarn.resourcemanager.fs.state-store.uri", "file:///tmp/");
    return conf;
  }
}