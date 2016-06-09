package org.apache.myriad.state;

import org.apache.mesos.state.Variable;

/**
 * Simplified implementation for unit tests
 */
public class MockVariable extends Variable {
  private byte[] value;
  private String name;
  public MockVariable(){}
 
  public MockVariable setValue(byte[] value) {
    this.value = value.clone();
    return this;
  }

  public MockVariable setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public byte[] value() {
    return this.value.clone();
  }

  public String name() {
    return this.name;
  }
  @Override
  public Variable mutate(byte[] value) {
    return new MockVariable();
  }
}