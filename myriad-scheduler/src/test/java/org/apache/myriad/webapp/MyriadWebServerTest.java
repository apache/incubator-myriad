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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.TestObjectFactory;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * Unit test cases for MyriadWebServer class
 */
public class MyriadWebServerTest extends BaseConfigurableTest {
  MyriadWebServer webServer;

  static String tmpDir = System.getProperty("java.io.tmpdir");
  static File tmpKeystore = new File(tmpDir, "ssl_keystore");
  static File tmpTruststore = new File(tmpDir, "ssl_truststore");

  @Before
  public void setUp() throws Exception {
    super.setUp();

    try (InputStream keystore = MyriadWebServerTest.class.getResourceAsStream("/ssl_keystore")) {
      try (InputStream truststore = MyriadWebServerTest.class.getResourceAsStream("/ssl_truststore")) {
        if (keystore != null && truststore != null) {
          try (OutputStream keyStoreOS = new FileOutputStream(tmpKeystore)) {
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = keystore.read(bytes)) != -1) {
              keyStoreOS.write(bytes, 0, length);
            }
          }
          try (OutputStream trustStoreOS = new FileOutputStream(tmpTruststore)) {
            byte[] bytes = new byte[1024];
            int length = 0;
            while ((length = truststore.read(bytes)) != -1) {
              trustStoreOS.write(bytes, 0, length);
            }
          }
        }
      }
    }
    System.setProperty("tmptest.dir", tmpDir);
  }

  @After
  public void tearDown() throws Exception {
    assertTrue(tmpKeystore.delete());
    assertTrue(tmpTruststore.delete());
  }
  @Test
  public void testStartStopMyriadWebServer() throws Exception {
    webServerStartStop();
  }

  @Test
  public void testsStartStopMyriadWebServerWithSSL() throws Exception {
    Field[] fields = MyriadConfiguration.class.getDeclaredFields();
    for (Field field : fields) {
      if ("isSSLEnabled".equalsIgnoreCase(field.getName())) {
        field.setAccessible(true);
        field.set(cfg, true);
        break;
      }
    }
    assertTrue(cfg.isSSLEnabled());
    webServerStartStop();
  }

  private void webServerStartStop() throws Exception {
    webServer = TestObjectFactory.getMyriadWebServer(cfg);
    webServer.start();
    assertEquals(MyriadWebServer.Status.STARTED, webServer.getStatus());
    webServer.stop();
    assertEquals(MyriadWebServer.Status.STOPPED, webServer.getStatus());
  }
}