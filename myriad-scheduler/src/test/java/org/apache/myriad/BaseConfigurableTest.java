package org.apache.myriad;

import java.net.URL;

import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.Before;
import org.junit.Test;

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

  /**
   * This is normally overridden in derived classes. Be sure to invoke this implementation, otherwise
   * cfg, cfgWithRole, and cfgWithDocker will all be null.
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    cfg = mapper.readValue(getConfURL("myriad-config-test-default.yml"),
    MyriadConfiguration.class);
    cfgWithRole = mapper.readValue(getConfURL("myriad-config-test-default-with-framework-role.yml"),
            MyriadConfiguration.class);
    cfgWithDocker = mapper.readValue(getConfURL("myriad-config-test-default-with-docker-info.yml"),
                MyriadConfiguration.class);
  } 

  private URL getConfURL(String file) {
    return Thread.currentThread().getContextClassLoader().getResource(file);
  }
  
  @Test
  public void testMyriadConfiguration() throws Exception {
    cfg.getFrameworkName();
  }
}