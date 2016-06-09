package org.apache.myriad;

import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Base class for all JUnit tests that require a MyriadConfiguration object. This class encapsulates the
 * logic instantiate and configure a MyriadConfiguration object using the myriad-config-test-default.yml file.
 * 
 */
public class BaseConfigurableTest {
  protected MyriadConfiguration cfg;

  @Before
  public void setUp() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
    MyriadConfiguration.class);
  } 

  @Test
  public void testMyriadConfiguration() throws Exception {
    cfg.getFrameworkName();
  }
}