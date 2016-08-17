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
package org.apache.myriad.state;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

/**
 * Stubbed-out implementation for unit tests
 */
public class MockState implements State {
  private Map<String, Future<Variable>> values = new HashMap<String, Future<Variable>>();

  @Override
  public Future<Variable> fetch(String name) {
    return values.get(name);
  }

  @Override
  public Future<Variable> store(Variable variable) {
    MockFuture future = new MockFuture(variable);
    
    if (!(variable instanceof MockVariable)) {
      throw new IllegalArgumentException("The Variable must be a MockVariable");
    }
    
    MockVariable mVar = (MockVariable) variable;
    values.put(mVar.name(), future);

    return future;
  }

  @Override
  public Future<Boolean> expunge(Variable variable) {
    return null;
  }

  @Override
  public Future<Iterator<String>> names() {
    return null;
  }
}