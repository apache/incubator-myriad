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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myriad;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.junit.After;
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
  protected String baseStateStoreDirectory = StringUtils.EMPTY;

  /**
   * This is normally overridden in derived classes. Be sure to invoke this implementation; 
   * otherwise, cfg, cfgWithRole, and cfgWithDocker will all be null.
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

  /**
   * Deletes the directories and files that back the MyriadFileSystemRMStateStore to ensure there
   * is no stale state within the MyriadFileSystemRMStateStore that could result in race conditions
   * depending upon how the unit tests are executed.
   * 
   * @throws Exception
   */
  protected void resetStoreState() throws Exception {
    checkConfiguration();
    File rootFile = new File(baseStateStoreDirectory + "/FSRMStateRoot/RMMyriadRoot");
    //Delete directory if present and recursively create directory path
    FileUtils.deleteDirectory(rootFile);
    FileUtils.forceMkdir(rootFile);

    File storeFile = new File(rootFile.getAbsolutePath() + "/MyriadState");

    if (!storeFile.createNewFile()) {
      throw new IllegalStateException(rootFile.getAbsolutePath() + "/MyriadState could not be created");
    }
  }

  /**
   * Confirms the configuration of object graph is correct
   * 
   * @throws IllegalStateException
   */
  protected void checkConfiguration() throws IllegalStateException {
    if (StringUtils.isEmpty(baseStateStoreDirectory)) {
      throw new IllegalStateException("The baseStateStoreDirectory must be set, preferably in overridden setUp method");
    }
  }

  private URL getConfURL(String file) {
    return Thread.currentThread().getContextClassLoader().getResource(file);
  }
  
  @After
  public void cleanUp() throws Exception {
    FileUtils.deleteDirectory(new File(baseStateStoreDirectory));
  }
}