package com.ebay.myriad.state;

import com.google.protobuf.InvalidProtocolBufferException;

import org.apache.mesos.Protos;
import org.apache.mesos.state.State;
import org.apache.mesos.state.Variable;

import java.util.concurrent.ExecutionException;

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
