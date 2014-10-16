package com.ebay.myriad.policy;

import com.ebay.myriad.scheduler.yarn.YarnSchedulerInterceptor;
import com.google.common.collect.Lists;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A scale down policy that maintains returns a list of nodes running least number of AMs.
 */
public class LeastAMNodesFirstPolicy implements NodeScaleDownPolicy, YarnSchedulerInterceptor.EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeastAMNodesFirstPolicy.class);

    private final YarnSchedulerInterceptor interceptor;
    private final AbstractYarnScheduler yarnScheduler;

    //TODO(Santosh): Should figure out the right values for the hashmap properties.
    // currently it's tuned for 200 nodes and 50 RM RPC threads (Yarn's default).
    private Map<SchedulerNode, NodeId> schedulerNodes = new ConcurrentHashMap<>(200, 0.75f, 50);

    @Inject
    public LeastAMNodesFirstPolicy(YarnSchedulerInterceptor interceptor) {
        this.interceptor = interceptor;
        this.yarnScheduler = this.interceptor.registerEventListener(this);
    }

    @Override
    public List<String> getNodesToScaleDown() {
        List<SchedulerNode> nodes = Lists.newArrayList(this.schedulerNodes.keySet());
        // We need to lock the YARN scheduler here. If we don't do that, then the YARN scheduler can
        // process HBs from NodeManagers and the state of SchedulerNode objects might change while we
        // are in the middle of sorting them based on the least number of AM containers.
        synchronized (yarnScheduler) {
            Collections.sort(nodes, new Comparator<SchedulerNode>() {
                @Override
                public int compare(SchedulerNode o1, SchedulerNode o2) {
                    List<RMContainer> runningContainers1 = o1.getRunningContainers();
                    List<RMContainer> runningContainers2 = o2.getRunningContainers();

                    Integer numRunningAMs1 = getNumAMContainers(runningContainers1);
                    Integer numRunningAMs2 = getNumAMContainers(runningContainers2);

                    Integer numRunningContainers1 = runningContainers1.size();
                    Integer numRunningContainers2 = runningContainers2.size();

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Host {} is running {} containers including {} App Masters",
                            o1.getNodeID().getHost(), numRunningContainers1, numRunningAMs1);
                        LOGGER.debug("Host {} is running {} containers including {} App Masters",
                            o2.getNodeID().getHost(), numRunningContainers2, numRunningAMs2);
                    }

                    // If two NMs are running equal number of AMs, sort them based on total num of running containers
                    if (numRunningAMs1.compareTo(numRunningAMs2) == 0) {
                        return numRunningContainers1.compareTo(numRunningContainers2);
                    }
                    return numRunningAMs1.compareTo(numRunningAMs2);
                }
            });
        }

        List<String> hosts = new ArrayList<>(nodes.size());
        for (SchedulerNode node : nodes) {
            hosts.add(schedulerNodes.get(node).getHost());
        }

        return hosts;
    }

    /**
     * Called whenever a NM HBs to RM. The NM's updates will already be recorded in the
     * SchedulerNode before this method is called.
     *
     * @param event
     */
    @Override
    public void onNodeUpdated(NodeUpdateSchedulerEvent event) {
        NodeId nodeID = event.getRMNode().getNodeID();
        SchedulerNode schedulerNode = yarnScheduler.getSchedulerNode(nodeID);
        if (!schedulerNodes.containsKey(schedulerNode)) {
            schedulerNodes.put(schedulerNode, nodeID);
        }
    }


    private Integer getNumAMContainers(List<RMContainer> containers) {
        int count = 0;
        for (RMContainer container : containers) {
            if (container.isAMContainer()) {
                count++;
            }
        }
        return count;
    }
}
