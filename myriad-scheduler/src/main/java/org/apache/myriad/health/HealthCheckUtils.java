/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * Health Check Utilities
 */
public class HealthCheckUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckUtils.class);

    public static boolean checkHostPort(String connectionString) {
        String[] split = connectionString.split(":");
        String serverAddress = split[0];
        Integer serverPort = Integer.valueOf(split[1]);
        try (Socket s = new Socket(serverAddress, serverPort)) {
            return true;
        } catch (IOException ex) {
            LOGGER.error("parsing host port", ex);
        }

        return false;
    }
}
