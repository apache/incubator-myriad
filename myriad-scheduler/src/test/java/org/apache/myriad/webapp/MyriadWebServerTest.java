package org.apache.myriad.webapp;

import static org.junit.Assert.assertEquals;

import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test cases for MyriadWebServer class
 */
public class MyriadWebServerTest extends BaseConfigurableTest {
  MyriadWebServer webServer;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    webServer = TestObjectFactory.getMyriadWebServer(cfg);
  }

  @Test
  public void testStartStopMyriadWebServer() throws Exception {
    webServer.start();
    assertEquals(MyriadWebServer.Status.STARTED, webServer.getStatus());
    webServer.stop();
    assertEquals(MyriadWebServer.Status.STOPPED, webServer.getStatus());
  }
}