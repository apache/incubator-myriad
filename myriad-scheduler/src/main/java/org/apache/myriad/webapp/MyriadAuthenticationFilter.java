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

import com.google.common.annotations.VisibleForTesting;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.AuthenticationFilterInitializer;
import org.apache.hadoop.security.authentication.server.AuthenticationFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.util.Properties;

/**
 * Filter to wrap AuthenticationFilter with specific prefix
 */
public class MyriadAuthenticationFilter extends AuthenticationFilter {

  private static final String HTTP_AUTH_PREFIX = "hadoop.http.authentication.";

  private static Configuration conf;

  @Override
  protected Properties getConfiguration(String configPrefix, FilterConfig filterConfig) throws ServletException {
    Configuration localConf = new Configuration();
    if (conf != null) {
      localConf = conf;
    }
    Properties props = new Properties();
    props.putAll(AuthenticationFilterInitializer.getFilterConfigMap(localConf, HTTP_AUTH_PREFIX));
    props.putAll(super.getConfiguration(configPrefix, filterConfig));
    return props;
  }

  @VisibleForTesting
  static void setConfiguration(Configuration conf) {
    MyriadAuthenticationFilter.conf = conf;
  }
}
