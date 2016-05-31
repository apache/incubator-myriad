package org.apache.myriad.state;

import org.apache.mesos.Protos.FrameworkID;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for MyriadState
 */
public class MyriadStateTest {
  @Test
  public void testSetAndGetFrameworkITask() throws Exception {
    MockState mState = new MockState();
    mState.store(new MockVariable().setName("frameworkId").setValue(FrameworkID.newBuilder().setValue("mock-framework").build().toByteArray()));

    MyriadState state = new MyriadState(mState);
    state.setFrameworkId(FrameworkID.newBuilder().setValue("mock-framework").build());

    assertEquals("mock-framework", state.getFrameworkID().getValue());
  }
}