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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;
import org.apache.myriad.BaseConfigurableTest;
import org.apache.myriad.MyriadTestModule;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Test
 */
public class MyriadAuthenticationFilterTest extends BaseConfigurableTest {

  private MyriadWebServer webServer;

  @Test
  public void filterTest() throws Exception {
    try {
      MyriadAuthenticationFilter.setConfiguration(null);
      Injector injector = Guice.createInjector(new MyriadTestModule(), new WebAppGuiceTestModule(cfg));
      webServer = injector.getInstance(MyriadWebServer.class);
      webServer.start();
      assertEquals(MyriadWebServer.Status.STARTED, webServer.getStatus());

      String path = "api/config";
      String jsonRequest = "";

      createHttpRequest(path, jsonRequest, "GET", null, 200);

    } finally {
      webServer.stop();
      assertEquals(MyriadWebServer.Status.STOPPED, webServer.getStatus());
    }
  }

  @Test
  public void filterTestWithAuth() throws Exception {
    try {
      Configuration conf = new Configuration();
      Boolean anonAllowed = conf.getBoolean("hadoop.http.authentication.simple.anonymous.allowed", false);
      if (anonAllowed) {
        conf.set("hadoop.http.authentication.simple.anonymous.allowed", "false");
      }
      assertTrue(!conf.getBoolean("hadoop.http.authentication.simple.anonymous.allowed", false));

      MyriadAuthenticationFilter.setConfiguration(conf);

      Field[] fields = MyriadConfiguration.class.getDeclaredFields();
      for (Field field : fields) {
        if ("isSecurityEnabled".equalsIgnoreCase(field.getName())) {
          field.setAccessible(true);
          field.set(cfg, true);
          break;
        }
      }

      Injector injector = Guice.createInjector(new MyriadTestModule(), new WebAppGuiceTestModule(cfg));
      MyriadConfiguration myCfg = injector.getInstance(MyriadConfiguration.class);

      for (Field field : fields) {
        if ("isSecurityEnabled".equalsIgnoreCase(field.getName())) {
          field.setAccessible(true);
          field.set(myCfg, true);
          break;
        }
      }

      webServer = injector.getInstance(MyriadWebServer.class);
      webServer.start();
      assertEquals(MyriadWebServer.Status.STARTED, webServer.getStatus());

      String path = "api/config";
      String jsonRequest = "";

      createHttpRequest(path, jsonRequest, "GET", null, 401);

      String query = "user.name=test";
      Map<String, List<String>> headers = createHttpRequest(path, jsonRequest, "GET", query, 200);
      List<String> header = headers.get("Set-Cookie");
      assertNotNull(header);
      assertTrue(header.get(0).startsWith("hadoop.auth=\"u=test&p=test&t=simple&e="));

    } finally {
      webServer.stop();
      assertEquals(MyriadWebServer.Status.STOPPED, webServer.getStatus());
    }
  }

  private Map<String, List<String>> createHttpRequest(final String path, final String jsonRequest,
                                   final String method, final String query,
                                   int targetResponseCode) throws Exception {
    final StringBuilder strB = new StringBuilder();

    assertNotNull(path);
    assertNotNull(method);
    assertNotNull(jsonRequest);

    final String requestString = "http://0.0.0.0:" + cfg.getRestApiPort() + "/" + path + (query == null ? "" : "?" + query);

    URL webURL = new URL(requestString);
    HttpURLConnection connection = (HttpURLConnection) webURL.openConnection();

    connection.setDoInput(true);
    connection.setDoOutput(true);
    connection.setRequestMethod(method);

    if (!"GET".equalsIgnoreCase(method)) {
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Content-length", String.valueOf(jsonRequest.length()));

      DataOutputStream output = new DataOutputStream(connection.getOutputStream());
      output.writeBytes(jsonRequest);
      output.close();
    }
    int responseCode = connection.getResponseCode();
    assertTrue(responseCode == targetResponseCode);

    if (responseCode == HttpServletResponse.SC_OK) {
      BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

      String line;
      while ((line = br.readLine()) != null) {
        strB.append(line);
      }
      br.close();
      assertTrue(!strB.toString().isEmpty());
    }
    Map<String, List<String>> headers = connection.getHeaderFields();
    connection.disconnect();
    return headers;
  }

}
