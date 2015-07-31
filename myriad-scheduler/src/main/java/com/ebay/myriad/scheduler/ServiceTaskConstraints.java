package com.ebay.myriad.scheduler;

import java.util.Map;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.configuration.ServiceConfiguration;

/**
 * ServiceTaskConstraints is an implementation of TaskConstraints for a service
 * at this point constraints are on ports
 * Later on there may be other types of constraints added
 *
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