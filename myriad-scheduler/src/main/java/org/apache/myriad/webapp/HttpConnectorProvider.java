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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.webapp;

import com.google.inject.Provider;
import javax.inject.Inject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.ssl.SslSocketConnectorSecure;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.hadoop.yarn.webapp.util.WebAppUtils.WEB_APP_KEYSTORE_PASSWORD_KEY;
import static org.apache.hadoop.yarn.webapp.util.WebAppUtils.WEB_APP_KEY_PASSWORD_KEY;
import static org.apache.hadoop.yarn.webapp.util.WebAppUtils.WEB_APP_TRUSTSTORE_PASSWORD_KEY;

/**
 * The factory for creating the http connector for the myriad scheduler
 */
public class HttpConnectorProvider implements Provider<Connector> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectorProvider.class);

  private MyriadConfiguration myriadConf;

  @Inject
  public HttpConnectorProvider(MyriadConfiguration myriadConf) {
    this.myriadConf = myriadConf;
  }

  @Override
  public Connector get() {
    final AbstractConnector ret;
    if (!myriadConf.isSSLEnabled()) {
      final SelectChannelConnector listener = new SelectChannelConnector();
      ret = listener;
    } else {
      final Configuration sslConf = new Configuration(false);
      boolean needsClientAuth = YarnConfiguration.YARN_SSL_CLIENT_HTTPS_NEED_AUTH_DEFAULT;
      sslConf.addResource(YarnConfiguration.YARN_SSL_SERVER_RESOURCE_DEFAULT);

      final SslSocketConnectorSecure listener = new SslSocketConnectorSecure();
      listener.setHeaderBufferSize(1024 * 64);
      listener.setNeedClientAuth(needsClientAuth);
      listener.setKeyPassword(getPassword(sslConf, WEB_APP_KEY_PASSWORD_KEY));

      String keyStore = sslConf.get("ssl.server.keystore.location");

      if (keyStore != null) {
        listener.setKeystore(keyStore);
        listener.setKeystoreType(sslConf.get("ssl.server.keystore.type", "jks"));
        listener.setPassword(getPassword(sslConf, WEB_APP_KEYSTORE_PASSWORD_KEY));
      }

      String trustStore = sslConf.get("ssl.server.truststore.location");

      if (trustStore != null) {
        listener.setTruststore(trustStore);
        listener.setTruststoreType(sslConf.get("ssl.server.truststore.type", "jks"));
        listener.setTrustPassword(getPassword(sslConf, WEB_APP_TRUSTSTORE_PASSWORD_KEY));
      }
      ret = listener;
    }

    ret.setName("Myriad");
    ret.setHost("0.0.0.0");
    ret.setPort(myriadConf.getRestApiPort());
    return ret;
  }

  static String getPassword(Configuration conf, String alias) {
    String password = null;
    try {
      char[] passchars = conf.getPassword(alias);
      if (passchars != null) {
        password = new String(passchars);
      }
    } catch (IOException ioe) {
      password = null;
    }
    return password;
  }

}
