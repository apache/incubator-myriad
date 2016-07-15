package org.apache.myriad;

import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.Before;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Base class for all JUnit tests that require a MyriadConfiguration object. This class encapsulates the
 * logic instantiate and configure a MyriadConfiguration object using all yml config files.
 * 
 */
public class BaseConfigurableTest {
  protected MyriadConfiguration cfg;
  protected MyriadConfiguration cfgWithRole;
  protected MyriadConfiguration cfgWithDocker;

  @Before
  public void setUp() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default.yml"),
    MyriadConfiguration.class);
    cfgWithRole = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default-with-framework-role.yml"),
            MyriadConfiguration.class);
    cfgWithDocker = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("myriad-config-test-default-with-docker-info.yml"),
                MyriadConfiguration.class);
  } 
}