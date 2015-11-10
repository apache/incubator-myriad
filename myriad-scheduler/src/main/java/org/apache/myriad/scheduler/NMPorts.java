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
package org.apache.myriad.scheduler;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for dynamically assigning ports to nodemanager
 */
public class NMPorts implements Ports {
  private static final String NM_RPC_PORT_KEY = "nm.rpc.port";
  private static final String NM_LOCALIZER_PORT_KEY = "nm.localizer.port";
  private static final String NM_WEBAPP_HTTP_PORT_KEY = "nm.webapp.http.port";
  private static final String NM_HTTP_SHUFFLE_PORT_KEY = "nm.http.shuffle.port";

  private static final String[] NM_PORT_KEYS =
      {NM_RPC_PORT_KEY, NM_LOCALIZER_PORT_KEY, NM_WEBAPP_HTTP_PORT_KEY, NM_HTTP_SHUFFLE_PORT_KEY};

  private Map<String, Long> portsMap = new HashMap<>(NM_PORT_KEYS.length);

  public NMPorts(Long[] ports) {
    Preconditions.checkState(ports.length == NM_PORT_KEYS.length, "NMPorts: array \"ports\" is of unexpected length");
    for (int i = 0; i < NM_PORT_KEYS.length; i++) {
      portsMap.put(NM_PORT_KEYS[i], ports[i]);
    }
  }

  public long getRpcPort() {
    return portsMap.get(NM_RPC_PORT_KEY);
  }

  public long getLocalizerPort() {
    return portsMap.get(NM_LOCALIZER_PORT_KEY);
  }

  public long getWebAppHttpPort() {
    return portsMap.get(NM_WEBAPP_HTTP_PORT_KEY);
  }

  public long getShufflePort() {
    return portsMap.get(NM_HTTP_SHUFFLE_PORT_KEY);
  }

  public static int expectedNumPorts() {
    return NM_PORT_KEYS.length;
  }

  /**
   * @return a string representation of NMPorts
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder().append("{");
    for (String key : NM_PORT_KEYS) {
      sb.append(key).append(": ").append(portsMap.get(key).toString()).append(", ");
    }
    sb.replace(sb.length() - 2, sb.length(), "}");
    return sb.toString();
  }
}
