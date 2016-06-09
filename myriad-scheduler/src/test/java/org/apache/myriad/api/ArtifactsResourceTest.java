package org.apache.myriad.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import javax.ws.rs.core.Response;

import org.apache.myriad.BaseConfigurableTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for ArtifactsResource
 */
public class ArtifactsResourceTest extends BaseConfigurableTest {
  ArtifactsResource resource;
  File configFile;
  File binaryFile;
   
  @Before
  public void setUp() throws Exception {
    super.setUp();
    configFile = new File("/tmp/myriadEtc");
    binaryFile = new File("/tmp/myriadBinary");
    assertTrue(configFile.createNewFile());
    assertTrue(binaryFile.createNewFile());
    resource = new ArtifactsResource(cfg);   
  }

  @Test
  public void testGetConfig() throws Exception {
    Response res = resource.getConfig();
    assertEquals(configFile, res.getEntity());
    assertEquals(200, res.getStatus());
  }

  @Test
  public void testGetBinary() throws Exception {
    Response res = resource.getBinary();
    assertEquals(binaryFile, res.getEntity());
    assertEquals(200, res.getStatus());
  }

  @After
  public void tearDown() throws Exception {
    assertTrue(new File("/tmp/myriadEtc").delete());
    assertTrue(new File("/tmp/myriadBinary").delete());    
  }
}