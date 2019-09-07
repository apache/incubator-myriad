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
package org.apache.myriad.webapp;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.hadoop.security.ssl.SslSocketConnectorSecure;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.Test;
import org.mortbay.jetty.Connector;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

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

  @Test
  public void testConnectorSSLOn() throws Exception {
    Field[] fields = MyriadConfiguration.class.getDeclaredFields();
    for (Field field : fields) {
      if ("isSSLEnabled".equalsIgnoreCase(field.getName())) {
        field.setAccessible(true);
        field.set(cfg, true);
        break;
      }
    }
    assertTrue(cfg.isSSLEnabled());

    try (InputStream keystore = MyriadWebServerTest.class.getResourceAsStream("/ssl_keystore")) {
      try (InputStream truststore = MyriadWebServerTest.class.getResourceAsStream("/ssl_truststore")) {
        if (keystore != null && truststore != null) {
          try (OutputStream keyStoreOS = new FileOutputStream(MyriadWebServerTest.tmpKeystore)) {
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = keystore.read(bytes)) != -1) {
              keyStoreOS.write(bytes, 0, length);
            }
          }
          try (OutputStream trustStoreOS = new FileOutputStream(MyriadWebServerTest.tmpTruststore)) {
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = truststore.read(bytes)) != -1) {
              trustStoreOS.write(bytes, 0, length);
            }
          }
        }
      }
    }
    System.setProperty("tmptest.dir", MyriadWebServerTest.tmpDir);
    try {
      HttpConnectorProvider provider = new HttpConnectorProvider(cfg);
      Connector connector = provider.get();
      assertTrue(connector instanceof SslSocketConnectorSecure);
      SslSocketConnectorSecure secureConnector = (SslSocketConnectorSecure) connector;
      assertNotNull(secureConnector.getKeystore());
      assertEquals(secureConnector.getKeystore(), MyriadWebServerTest.tmpDir + "/ssl_keystore");

      assertNotNull(secureConnector.getTruststore());
      assertEquals(secureConnector.getTruststore(), MyriadWebServerTest.tmpDir + "/ssl_truststore");

      assertEquals(8192, connector.getPort());
      assertEquals("0.0.0.0", connector.getHost());
      assertEquals("Myriad", connector.getName());
    } finally {
      assertTrue(MyriadWebServerTest.tmpKeystore.delete());
      assertTrue(MyriadWebServerTest.tmpTruststore.delete());
    }
  }
}