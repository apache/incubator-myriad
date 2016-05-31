package org.apache.myriad.health;

import java.net.ServerSocket;

import org.junit.Test;

/**
 * Unit tests for HealthCheckUtils class
 */
public class HealthCheckUtilsTest {
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidHost() throws Exception {
    HealthCheckUtils.checkHostPort("localhost-8000");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidPort() throws Exception {
    HealthCheckUtils.checkHostPort("localhost:ab12");
  }

  @Test
  public void testValidHostPortString() throws Exception {
    ServerSocket socket = new ServerSocket(8000);
    HealthCheckUtils.checkHostPort("localhost:8000");
    socket.close();
  }
}