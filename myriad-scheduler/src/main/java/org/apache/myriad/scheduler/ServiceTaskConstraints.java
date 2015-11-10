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

import java.util.Map;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;

/**
 * ServiceTaskConstraints is an implementation of TaskConstraints for a service
 * at this point constraints are on ports
 * Later on there may be other types of constraints added
 */
public class ServiceTaskConstraints implements TaskConstraints {

  private int portsCount;

  public ServiceTaskConstraints(MyriadConfiguration cfg, String taskPrefix) {
    this.portsCount = 0;
    Map<String, ServiceConfiguration> auxConfigs = cfg.getServiceConfigurations();
    if (auxConfigs == null) {
      return;
    }
    ServiceConfiguration serviceConfig = auxConfigs.get(taskPrefix);
    if (serviceConfig != null) {
      if (serviceConfig.getPorts().isPresent()) {
        this.portsCount = serviceConfig.getPorts().get().size();
      }
    }
  }

  @Override
  public int portsCount() {
    return portsCount;
  }
}