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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.hadoop.yarn.server.resourcemanager.recovery;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.myriad.state.MyriadStateStore;
import org.apache.myriad.state.utils.StoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StateStore that stores Myriad state in addition to RM state to DFS.
 */
public class MyriadFileSystemRMStateStore extends FileSystemRMStateStore implements MyriadStateStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadFileSystemRMStateStore.class);

  protected static final String ROOT_NAME = "FSRMStateRoot";
  private static final String RM_MYRIAD_ROOT = "RMMyriadRoot";
  private static final String MYRIAD_STATE_FILE = "MyriadState";

  private Path myriadPathRoot = null;
  private byte[] myriadStateBytes = null;

  private Method updateFileMethod = null; //This is a cache Method so we do fewer reflection calls

  @Override
  public synchronized void initInternal(Configuration conf) throws Exception {
    super.initInternal(conf);
    Path rootPath = new Path(fsWorkingPath, ROOT_NAME);
    myriadPathRoot = new Path(rootPath, RM_MYRIAD_ROOT);
    updateFileMethod = getUpdateFileMethod();
    if (updateFileMethod == null) {
      //something is broken
      throw new RuntimeException("Could not find valid updateFile Method");
    }
  }

  private Method getUpdateFileMethod() {
    Method[] methods = super.getClass().getSuperclass().getDeclaredMethods();
    for (Method m : methods) {
      if (m.getName().equals("updateFile")) {
        return m;
      }
    }
    return null;
  }

  @Override
  protected synchronized void startInternal() throws Exception {
    super.startInternal();
    fs.mkdirs(myriadPathRoot);
  }

  @Override
  public synchronized RMState loadState() throws Exception {
    RMState rmState = super.loadState();
    Path myriadStatePath = new Path(myriadPathRoot, MYRIAD_STATE_FILE);
    LOGGER.info("Loading state information for Myriad from: " + myriadStatePath);

    try {
      // Throws IOException if file is not present.
      FileStatus fileStatus = fs.listStatus(myriadStatePath)[0];
      FSDataInputStream in = fs.open(myriadStatePath);
      myriadStateBytes = new byte[(int) fileStatus.getLen()];
      in.readFully(myriadStateBytes);
      in.close();
    } catch (IOException e) {
      LOGGER.error("State information for Myriad could not be loaded from: " + myriadStatePath);
    }
    return rmState;
  }

  @Override
  public synchronized StoreContext loadMyriadState() throws Exception {
    StoreContext sc = null;
    if (myriadStateBytes != null && myriadStateBytes.length > 0) {
      sc = StoreContext.fromSerializedBytes(myriadStateBytes);
      myriadStateBytes = null;
    }
    return sc;
  }

  @Override
  public synchronized void storeMyriadState(StoreContext sc) throws Exception {
    Path myriadStatePath = new Path(myriadPathRoot, MYRIAD_STATE_FILE);

    LOGGER.debug("Storing state information for Myriad at: " + myriadStatePath);
    try {
      reflectedUpdateFile(myriadStatePath, sc.toSerializedContext().toByteArray());
    } catch (Exception e) {
      LOGGER.error("State information for Myriad could not be stored at: " + myriadStatePath, e);
    }
  }



  protected void reflectedUpdateFile(Path outputPath, byte[] data) throws InvocationTargetException, IllegalAccessException {
    Class [] parameters = updateFileMethod.getParameterTypes();
    if (parameters.length == 2 && parameters[0].equals(Path.class) && parameters[1].isArray()) {
      updateFileMethod.invoke(this, outputPath, data);
    } else if (parameters.length == 3 && parameters[0].equals(Path.class) && parameters[1].isArray() && parameters[2].isPrimitive()) {
      updateFileMethod.invoke(this, outputPath, data, true);
    } else {
      //something is broken
      throw new RuntimeException("updateFile Method has unexpected parameters");
    }
  }

  @Override
  public synchronized void removeMyriadState() throws Exception {
    if (fs.exists(myriadPathRoot)) {
      if (!fs.delete(myriadPathRoot, true)) {
        LOGGER.error("Failed to delete MyriadStateStore path " + myriadPathRoot);
        return;
      }
      LOGGER.info("Deleted Myriad state store path " + myriadPathRoot);
    }
  }
}
