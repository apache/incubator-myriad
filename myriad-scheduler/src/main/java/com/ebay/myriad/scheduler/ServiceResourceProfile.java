package com.ebay.myriad.scheduler;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Resource Profile for any service 
 *
 */
public class ServiceResourceProfile {

  protected final String name;

  /**
   * Number of CPU needed to run a service
   */
  protected final Double cpus;

  /**
   * Memory in MB needed to run a service
   */
  protected final Double memory;

  protected Double executorCpu = 0.0;
  
  protected Double executorMemory = 0.0;
  
  protected String className;
    
  public ServiceResourceProfile(String name, Double cpu, Double mem) {
    this.name = name;
    this.cpus = cpu;
    this.memory = mem;
    this.className = ServiceResourceProfile.class.getName();
  }


  public String getName() {
    return name;
  }

  public Double getCpus() {
    return cpus;
  }

  public Double getMemory() {
    return memory;
  }
  
  public Double getAggregateMemory() {
    return memory;
  }
  
  public Double getAggregateCpu() {
    return cpus;
  }
  
  public Double getExecutorCpu() {
    return executorCpu;
  }

  public void setExecutorCpu(Double executorCpu) {
    this.executorCpu = executorCpu;
  }

  public Double getExecutorMemory() {
    return executorMemory;
  }

  public void setExecutorMemory(Double executorMemory) {
    this.executorMemory = executorMemory;
  }


  @Override
  public String toString() {
      Gson gson = new Gson();
      return gson.toJson(this);
  }
  
  /**
   * Custom serializer to help with deserialization of class hierarchy
   * since at the point of deserialization we don't know class of the data 
   * that is being deserialized
   *
   */
  public static class CustomDeserializer implements JsonDeserializer<ServiceResourceProfile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDeserializer.class);
    
    @Override
    public ServiceResourceProfile deserialize(JsonElement json, Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException {
      String type = json.getAsJsonObject().get("className").getAsString();
      try {
        @SuppressWarnings("rawtypes")
        Class c = Class.forName(type);
        if (ServiceResourceProfile.class.equals(c)) {
          return new Gson().fromJson(json, typeOfT);
        }
        ServiceResourceProfile profile = context.deserialize(json, c);
        return profile;
      } catch (ClassNotFoundException e) {
        LOGGER.error("Classname is not found", e);
      }
      return null;
    }
    
  }
}
