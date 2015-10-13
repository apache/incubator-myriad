package com.ebay.myriad.policy;

import org.apache.mesos.Protos;

import java.util.List;

/**
 * Policy for scaling down the node managers.
 */
public interface NodeScaleDownPolicy {

    /**
     * Apply a scale down policy to the given list of taskIDs.
     * @param taskIDs
     */
    public void apply(List<Protos.TaskID> taskIDs);

}
