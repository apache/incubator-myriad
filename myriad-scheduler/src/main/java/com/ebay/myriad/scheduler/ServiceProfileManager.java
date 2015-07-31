package com.ebay.myriad.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Class to keep all the ServiceResourceProfiles together
 *
 */
public class ServiceProfileManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProfileManager.class);

  private Map<String, ServiceResourceProfile> profiles = new ConcurrentHashMap<>();

  public ServiceResourceProfile get(String name) {
      return profiles.get(name);
  }

  public void add(ServiceResourceProfile profile) {
      LOGGER.info("Adding profile {} with CPU: {} and Memory: {}",
              profile.getName(), profile.getCpus(), profile.getMemory());
      profiles.put(profile.getName(), profile);
  }

  public boolean exists(String name) {
      return this.profiles.containsKey(name);
  }

  public String toString() {
      Gson gson = new Gson();
      return gson.toJson(this);
  }

}
