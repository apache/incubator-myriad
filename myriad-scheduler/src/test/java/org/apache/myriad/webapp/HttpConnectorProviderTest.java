package org.apache.myriad.webapp;

import static org.junit.Assert.assertEquals;

import org.apache.myriad.BaseConfigurableTest;
import org.junit.Test;
import org.mortbay.jetty.Connector;

/**
 * Unit tests for HttpConnectionProvider
 */
public class HttpConnectorProviderTest extends BaseConfigurableTest {

  @Test
  public void testConnector() throws Exception {
    HttpConnectorProvider provider = new HttpConnectorProvider(cfg);
    Connector connector = provider.get();
    assertEquals(8192, connector.getPort());
    assertEquals("0.0.0.0", connector.getHost());
    assertEquals("Myriad", connector.getName());
  }
}