package com.ebay.myriad.state;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.mesos.Protos;
import org.apache.mesos.state.Variable;
import org.apache.mesos.state.ZooKeeperState;

import java.util.concurrent.ExecutionException;

/**
 * Model that represents the state of Myriad
 */
public class MyriadState {
    private static final String KEY_FRAMEWORK_ID = "frameworkId";

    private ZooKeeperState zkState;

    public MyriadState(ZooKeeperState zkState) {
        this.zkState = zkState;
    }

    public Protos.FrameworkID getFrameworkID() throws InterruptedException, ExecutionException, InvalidProtocolBufferException {
        byte[] frameworkId = zkState.fetch(KEY_FRAMEWORK_ID).get().value();

        if (frameworkId.length > 0) {
            return Protos.FrameworkID.parseFrom(frameworkId);
        } else {
            return null;
        }
    }

    public void setFrameworkId(Protos.FrameworkID newFrameworkId) throws InterruptedException, ExecutionException {
        Variable frameworkId = zkState.fetch(KEY_FRAMEWORK_ID).get();
        frameworkId = frameworkId.mutate(newFrameworkId.toByteArray());
        zkState.store(frameworkId).get();
    }
}
