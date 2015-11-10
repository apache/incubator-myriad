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
package org.apache.myriad.state;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.concurrent.ExecutionException;
import org.apache.mesos.Protos;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

/**
 * Model that represents the state of Myriad
 */
public class MyriadState {
  public static final String KEY_FRAMEWORK_ID = "frameworkId";

  private State stateStore;

  public MyriadState(State stateStore) {
    this.stateStore = stateStore;
  }

  public Protos.FrameworkID getFrameworkID() throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
    byte[] frameworkId = stateStore.fetch(KEY_FRAMEWORK_ID).get().value();

    if (frameworkId.length > 0) {
      return Protos.FrameworkID.parseFrom(frameworkId);
    } else {
      return null;
    }
  }

  public void setFrameworkId(Protos.FrameworkID newFrameworkId) throws InterruptedException, ExecutionException {
    Variable frameworkId = stateStore.fetch(KEY_FRAMEWORK_ID).get();
    frameworkId = frameworkId.mutate(newFrameworkId.toByteArray());
    stateStore.store(frameworkId).get();
  }
}
