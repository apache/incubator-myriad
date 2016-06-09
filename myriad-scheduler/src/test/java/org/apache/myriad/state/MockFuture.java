package org.apache.myriad.state;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.mesos.state.Variable;

/**
 * Stubbed-out implementation for unit tests
 */
public class MockFuture implements Future<Variable> {
  private Variable value;

  public MockFuture(Variable value) {
    this.value = value;
  }
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return false;
  }

  @Override
  public Variable get() throws InterruptedException, ExecutionException {
    return value;
  }

  @Override
  public Variable get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
    return value;
  }
}