package com.ebay.myriad.policy;

import java.util.List;

/**
 * Policy for scaling down the node managers.
 */
public interface NodeScaleDownPolicy {

    /**
     * Get a list of host names of the nodes that needs to be scaled down.
     * The implementation of the policy should populate this list in a way that
     * the most preferred nodes to be scaled down should occur first in the list.
     * @return
     */
    public List<String> getNodesToScaleDown();

}
