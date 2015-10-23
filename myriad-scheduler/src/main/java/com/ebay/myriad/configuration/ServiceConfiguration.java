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
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.ebay.myriad.configuration;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

/**
 * Configuration for any service/task to be started from Myriad Scheduler
 *
 */
public class ServiceConfiguration {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfiguration.class);

  public static final Double DEFAULT_CPU = 0.1;
  
  public static final Double DEFAULT_MEMORY = 256.0;
  
  /**
   * Translates to -Xmx for the JVM.
   */
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerDouble.class)
  protected Double jvmMaxMemoryMB;

  /**
   * Amount of CPU share given to  JVM. 
   */
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerDouble.class)
  protected Double cpus;

  /**
   * Translates to jvm opts for the JVM.
   */
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerStr.class)
  protected String jvmOpts;

  @JsonProperty
  @JsonSerialize(using = OptionalSerializerMap.class)
  protected Map<String, Long> ports;
  
  /**
   * If we will have some services
   * that are not easy to express just by properties
   * we can use this one to have a specific implementation
   */
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerStr.class)
  protected String taskFactoryImplName;
  
  @JsonProperty
  protected String envSettings;
  
  @JsonProperty
  @NotEmpty
  protected String taskName;
  
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerInt.class)
  protected Integer maxInstances;
  
  @JsonProperty
  @JsonSerialize(using = OptionalSerializerStr.class)
  protected String command;
  
  @JsonProperty
  protected String serviceOptsName;
  
  
  public Optional<Double> getJvmMaxMemoryMB() {
      return Optional.fromNullable(jvmMaxMemoryMB);
  }

  public Optional<String> getJvmOpts() {
      return Optional.fromNullable(jvmOpts);
  }

  public Optional<Double> getCpus() {
      return Optional.fromNullable(cpus);
  }

  public String getTaskName() {
    return taskName;
  }

  public void setTaskName(String taskName) {
    this.taskName = taskName;
  }

  public Optional<String> getTaskFactoryImplName() {
    return Optional.fromNullable(taskFactoryImplName);
  }

  public String getEnvSettings() {
    return envSettings;
  }
  
  public Optional<Map<String, Long>> getPorts() {
    return Optional.fromNullable(ports);
  }
  
  public Optional<Integer> getMaxInstances() {
    return Optional.fromNullable(maxInstances);
  }

  public Optional<String> getCommand() {
    return Optional.fromNullable(command);
  }

  public String getServiceOpts() {
    return serviceOptsName;
  }

  /**
   * Custom Serializer
   * @param <T>
   */
  public static class OptionalSerializer<T> extends JsonSerializer<Optional<T>> {

    private static final JsonFactory jsonFactory = new ObjectMapper().getJsonFactory();

    protected ObjectMapper objMapper;
    
    public OptionalSerializer() {
      objMapper = new ObjectMapper(jsonFactory);
    }
    
    @Override
    public void serialize(Optional<T> value,
        org.codehaus.jackson.JsonGenerator jgen, SerializerProvider provider)
        throws IOException, org.codehaus.jackson.JsonProcessingException {
      if (value.isPresent()) {
        objMapper.writeValue(jgen, value.get());
      } else {
        objMapper.writeValue(jgen, "value is absent");
      }
    }
  }
  
  /**
   * Custom String serializer
   *
   */
  public static class OptionalSerializerStr extends OptionalSerializer<String> {
    @Override
    public void serialize(Optional<String> value, 
        org.codehaus.jackson.JsonGenerator jgen, SerializerProvider provider) throws IOException,
        org.codehaus.jackson.JsonProcessingException {
      super.serialize(value, jgen, provider);
    }
  }
  
  /**
   * Custom Double serializer
   *
   */
  public static class OptionalSerializerDouble extends OptionalSerializer<Double> {
    @Override
    public void serialize(Optional<Double> value, 
        org.codehaus.jackson.JsonGenerator jgen, SerializerProvider provider) throws IOException,
        org.codehaus.jackson.JsonProcessingException {
      super.serialize(value, jgen, provider);
    }
  }
  
  /**
   * Custom Integer serializer
   *
   */
  public static class OptionalSerializerInt extends OptionalSerializer<Integer> {
    @Override
    public void serialize(Optional<Integer> value, 
        org.codehaus.jackson.JsonGenerator jgen, SerializerProvider provider) throws IOException,
        org.codehaus.jackson.JsonProcessingException {
      super.serialize(value, jgen, provider);
    }
  }

  /**
   * Custom Boolean serializer
   *
   */
  public static class OptionalSerializerBoolean extends OptionalSerializer<Boolean> {
    @Override
    public void serialize(Optional<Boolean> value, 
        org.codehaus.jackson.JsonGenerator jgen, SerializerProvider provider) throws IOException,
        org.codehaus.jackson.JsonProcessingException {
      super.serialize(value, jgen, provider);
    }
  }

  /**
   * Custom Map serializer
   *
   */
  public static class OptionalSerializerMap extends OptionalSerializer<Map<?, ?>> {
    @Override
    public void serialize(Optional<Map<?, ?>> value, 
        org.codehaus.jackson.JsonGenerator jgen, SerializerProvider provider) throws IOException,
        org.codehaus.jackson.JsonProcessingException {
      super.serialize(value, jgen, provider);
    }

  }

}
