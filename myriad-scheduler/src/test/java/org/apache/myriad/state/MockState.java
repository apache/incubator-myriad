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