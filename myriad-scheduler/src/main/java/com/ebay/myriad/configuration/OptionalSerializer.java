package com.ebay.myriad.configuration;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;

import com.google.common.base.Optional;

/**
 * Custom Serializer that allows to serialize Optional 
 * today Optional does not serialize value, but just state: "present: true/false"
 * This class will serialize <T> value instead of state
 * This is needed for REST APIs and Myriad UI
 * @param <T>
 */
public class OptionalSerializer<T> extends JsonSerializer<Optional<T>> {

  private static final JsonFactory jsonFactory = new ObjectMapper().getJsonFactory();

  protected ObjectMapper objMapper;
  
  public OptionalSerializer() {
    objMapper = new ObjectMapper(jsonFactory);
  }
  
  @Override
  public void serialize(Optional<T> value,
      JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    if (value.isPresent()) {
      objMapper.writeValue(jgen, value.get());
    } else {
      objMapper.writeValue(jgen, "value is absent");
    }
  }
  
  /**
   * Custom String serializer
   *
   */
  public static class OptionalSerializerString extends OptionalSerializer<String> {
    @Override
    public void serialize(Optional<String> value, 
        JsonGenerator jgen, SerializerProvider provider) throws IOException,
        JsonProcessingException {
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
        JsonGenerator jgen, SerializerProvider provider) throws IOException,
        JsonProcessingException {
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
        JsonGenerator jgen, SerializerProvider provider) throws IOException,
        JsonProcessingException {
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
        JsonGenerator jgen, SerializerProvider provider) throws IOException,
        JsonProcessingException {
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
        JsonGenerator jgen, SerializerProvider provider) throws IOException,
        JsonProcessingException {
      super.serialize(value, jgen, provider);
    }

  }
}