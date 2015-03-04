package com.ebay.myriad.policy;

import com.ebay.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.google.common.collect.Lists;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeRemovedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A scale down policy that maintains returns a list of nodes running least number of AMs.
 */
public class LeastAMNodesFirstPolicy extends BaseInterceptor implements NodeScaleDownPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeastAMNodesFirstPolicy.class);

    private final AbstractYarnScheduler yarnScheduler;

    //TODO(Santosh): Should figure out the right values for the hashmap properties.
    // currently it's tuned for 200 nodes and 50 RM RPC threads (Yarn's default).
    private static final int INITIAL_NODE_SIZE = 200;
    private static final int EXPECTED_CONCURRENT_ACCCESS_COUNT = 50;
    private static final float LOAD_FACTOR_DEFAULT = 0.75f;

    private Map<String, SchedulerNode> schedulerNodes = new ConcurrentHashMap<>(INITIAL_NODE_SIZE, LOAD_FACTOR_DEFAULT, EXPECTED_CONCURRENT_ACCCESS_COUNT);

    @Inject
    public LeastAMNodesFirstPolicy(InterceptorRegistry registry, AbstractYarnScheduler yarnScheduler) {
        registry.register(this);
        this.yarnScheduler = yarnScheduler;
    }

    @Override
    public List<String> getNodesToScaleDown() {
        List<SchedulerNode> nodes = Lists.newArrayList(this.schedulerNodes.values());

        if (LOGGER.isDebugEnabled()) {
            for (SchedulerNode node : nodes) {
                LOGGER.debug("Host {} is running {} containers including {} App Masters",
                        node.getNodeID().getHost(), node.getRunningContainers().size(),
                        getNumAMContainers(node.getRunningContainers()));
            }
        }
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
            hosts.add(node.getNodeID().getHost());
        }

        return hosts;
    }

    @Override
    public void onEventHandled(SchedulerEvent event) {

        try {
            switch (event.getType()) {
                case NODE_UPDATE:
                    onNodeUpdated((NodeUpdateSchedulerEvent) event);
                    break;

                case NODE_REMOVED:
                    onNodeRemoved((NodeRemovedSchedulerEvent) event);
                    break;
                default:
                    LOGGER.warn("event type not supported");
                    break;
            }
        } catch (ClassCastException e) {
            LOGGER.error("incorrect event object", e);
        }
    }

    /**
     * Called whenever a NM HBs to RM. The NM's updates will already be recorded in the
     * SchedulerNode before this method is called.
     *
     * @param event
     */
    private void onNodeUpdated(NodeUpdateSchedulerEvent event) {
        NodeId nodeID = event.getRMNode().getNodeID();
        SchedulerNode schedulerNode = yarnScheduler.getSchedulerNode(nodeID);
        schedulerNodes.put(nodeID.getHost(), schedulerNode); // keep track of only one node per host
    }

    private void onNodeRemoved(NodeRemovedSchedulerEvent event) {
        SchedulerNode schedulerNode = schedulerNodes.get(event.getRemovedRMNode().getNodeID().getHost());
        if (schedulerNode.getNodeID().equals(event.getRemovedRMNode().getNodeID())) {
            schedulerNodes.remove(schedulerNode.getNodeID().getHost());
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
