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
package org.apache.myriad.health;

import java.io.IOException;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Health Check Utilities
 */
public class HealthCheckUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckUtils.class);

  public static boolean checkHostPort(String connectionString) {
    String[] hostPort = generateHostPortArray(connectionString);
    
    try {
      createSocket(hostPort);
      return true;
    } catch (IOException e) {
      LOGGER.error("error in connecting to " + hostPort[0] + ":" + hostPort[1], e);
    } 
    
    return false;
  }

  private static void createSocket(String[] hostPort) throws IOException {
    String address = hostPort[0];
    Integer port = Integer.valueOf(hostPort[1]); 

    Socket s = new Socket(address, port);
    s.close();
  }

  private static String[] generateHostPortArray(String connectionString) {
    String[] split = connectionString.split(":");   
    if (split.length != 2) {
      throw new IllegalArgumentException("The Connection String " + connectionString + " is invalid. It must be in <host>:<port> format");
    } else if (!StringUtils.isNumeric(split[1])) {
      throw new IllegalArgumentException("The Connection String " + connectionString + " is invalid. The port must be an integer");
    } else {
      return split;
    }
  }
}