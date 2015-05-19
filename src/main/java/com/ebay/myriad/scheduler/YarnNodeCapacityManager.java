package com.ebay.myriad.scheduler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.ResourceOption;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.myriad.executor.ContainerTaskStatusRequest;
import com.ebay.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Manages the capacity exposed by NodeManager. It uses the offers available
 * from Mesos to inflate the node capacity and lets ResourceManager make the
 * scheduling decision. After the sheduling decision is done, there are 2 cases:
 *
 * 1. If ResourceManager did not use the expanded capacity, then the node's
 * capacity is reverted back to original value and the offer is declined.
 * 2. If ResourceManager ended up using the expanded capacity, then the node's
 * capacity is updated accordingly and any unused capacity is returned back to
 * Mesos.
 */
public class YarnNodeCapacityManager extends BaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(
        YarnNodeCapacityManager.class);

    private final AbstractYarnScheduler yarnScheduler;
    private final MyriadDriver myriadDriver;
    private final OfferLifecycleManager offerLifecycleMgr;
    private final NodeStore nodeStore;
    private final TaskFactory taskFactory;

    @Inject
    public YarnNodeCapacityManager(InterceptorRegistry registry,
                                   AbstractYarnScheduler yarnScheduler,
                                   MyriadDriver myriadDriver,
                                   TaskFactory taskFactory,
                                   OfferLifecycleManager offerLifecycleMgr,
                                   NodeStore nodeStore) {
        if (registry != null) {
            registry.register(this);
        }
        this.yarnScheduler = yarnScheduler;
        this.myriadDriver = myriadDriver;
        this.taskFactory = taskFactory;
        this.offerLifecycleMgr = offerLifecycleMgr;
        this.nodeStore = nodeStore;
    }

    @Override
    public void beforeSchedulerEventHandled(SchedulerEvent event) {
      // TODO (Kannan) This will not work with continuous scheduling
      // Need to find out a hook to execute before the actual scheduling attempt
      // is done.
      if (!(event instanceof NodeUpdateSchedulerEvent)) {
        return;
      }

      RMNode rmNode = ((NodeUpdateSchedulerEvent) event).getRMNode();
      applyOffers(rmNode);
    }

    /**
     * Applies the offers stashed till now for this node.
     */
    private void applyOffers(RMNode rmNode) {
      String hostname = rmNode.getNodeID().getHost();
      OfferFeed feed = offerLifecycleMgr.getOfferFeed(hostname);
      if (feed == null) {
        LOGGER.debug("No offer feed for: {}", hostname);
        return;
      }

      Protos.Offer offer;
      Node node = nodeStore.getNode(hostname);
      node.snapshotContainers();
      while ((offer = feed.poll()) != null) {
        Resource resOffered = getResourceFromOffer(Arrays.asList(offer));
        addCapacity(rmNode, resOffered);

        offerLifecycleMgr.markAsConsumed(offer);
      }
    }

    @Override
    public void afterSchedulerEventHandled(SchedulerEvent event) {
        switch (event.getType()) {
            case NODE_ADDED: {
              if (!(event instanceof NodeAddedSchedulerEvent)) {
                LOGGER.error("{} not an instance of {}",
                    event.getClass().getName(),
                    NodeAddedSchedulerEvent.class.getName());
                return;
              }

              NodeAddedSchedulerEvent nodeAddedEvent = (NodeAddedSchedulerEvent) event;
              NodeId nodeId = nodeAddedEvent.getAddedRMNode().getNodeID();
              String host = nodeId.getHost();
              if (nodeStore.isPresent(host)) {
                LOGGER.warn("Ignoring duplicate node registration. Host: {}", host);
                return;
              }

              SchedulerNode node = yarnScheduler.getSchedulerNode(nodeId);
              nodeStore.add(node);
              LOGGER.info("afterSchedulerEventHandled: NM registration from node {}", host);
            }
            break;

            case NODE_UPDATE: {
                if (!(event instanceof NodeUpdateSchedulerEvent)) {
                    LOGGER.error("{} not an instance of {}", event.getClass().getName(),
                        NodeUpdateSchedulerEvent.class.getName());
                    return;
                }

                RMNode rmNode = ((NodeUpdateSchedulerEvent) event).getRMNode();
                handleContainerAllocation(rmNode);
            }
            break;

            default: {
                LOGGER.debug("Unhandled event: {}", event.getClass().getName());
            }
            break;
        }
    }

    /**
     * Checks if any containers were allocated in the current scheduler run and
     * launches the corresponding Mesos tasks. It also udpates the node
     * capacity depending on what portion of the consumed offers were actually
     * used.
     */
    private void handleContainerAllocation(RMNode rmNode) {
      String host = rmNode.getNodeID().getHost();

      ConsumedOffer consumedOffer = offerLifecycleMgr.drainConsumedOffer(host);
      if (consumedOffer == null) {
        LOGGER.debug("No offer consumed for {}", host);
        return;
      }

      Node node = nodeStore.getNode(host);
      Set<RMContainer> containersBeforeSched = node.getContainerSnapshot();
      Set<RMContainer> containersAfterSched = new HashSet<>(
          node.getNode().getRunningContainers());

      Set<RMContainer> containersAllocatedByMesosOffer =
        (containersBeforeSched == null)
        ? containersAfterSched
        : Sets.difference(containersAfterSched, containersBeforeSched);

      if (containersAllocatedByMesosOffer.isEmpty()) {
        LOGGER.debug("No containers allocated using Mesos offers for host: {}", host);
        for (Protos.Offer offer : consumedOffer.getOffers()) {
          offerLifecycleMgr.declineOffer(offer);

          Resource resUnused = getResourceFromOffer(Arrays.asList(offer));
          removeCapacity(rmNode, resUnused);
        }
      } else {
        LOGGER.debug("Containers allocated using Mesos offers for host: {} count: {}",
            host, containersAllocatedByMesosOffer.size());

        // Identify the Mesos tasks that need to be launched
        List<Protos.TaskInfo> tasks = Lists.newArrayList();
        Resource resUsed = Resource.newInstance(0, 0);

        for (RMContainer newContainer : containersAllocatedByMesosOffer) {
          tasks.add(getTaskInfoForContainer(newContainer, consumedOffer, node));
          resUsed = Resources.add(resUsed, newContainer.getAllocatedResource());
        }

        // Reduce node capacity to account for unused offers
        Resource resOffered = getResourceFromOffer(consumedOffer.getOffers());
        Resource resUnused = Resources.subtract(resOffered, resUsed);
        removeCapacity(rmNode, resUnused);

        myriadDriver.getDriver().launchTasks(consumedOffer.getOfferIds(), tasks);
      }

      // No need to hold on to the snapshot anymore
      node.removeContainerSnapshot();
    }

    private void addCapacity(RMNode rmNode, Resource resource) {
      Resource currentResource = rmNode.getResourceOption().getResource();
      Resource newResource = Resources.add(currentResource, resource);

      rmNode.setResourceOption(
          ResourceOption.newInstance(newResource,
            RMNode.OVER_COMMIT_TIMEOUT_MILLIS_DEFAULT));

      LOGGER.debug("Added_Resource: {}, Delta: {}, New NM Capacity: {}",
          rmNode.getNodeID(), resource, newResource);
    }

    private void removeCapacity(RMNode rmNode, Resource resource) {
      Resource currentResource = rmNode.getResourceOption().getResource();
      // Explicitly subtracting each resource since Mesos does not
      // support Disk IO.
      currentResource.setVirtualCores(currentResource.getVirtualCores() - resource.getVirtualCores());
      currentResource.setMemory(currentResource.getMemory() - resource.getMemory());

      LOGGER.debug("Removed_Resource: {}, Delta: {}, New NM Capacity: {}",
          rmNode.getNodeID(), resource, currentResource);
    }

    public void removeCapacity(RMNode rmNode, ContainerId containerId) {
      removeCapacity(rmNode,
          yarnScheduler.getRMContainer(containerId).getAllocatedResource());
    }

    /**
     * Removes the entire capacity of the node.
     */
    public void removeCapacity(RMNode rmNode) {
      Resource currentResource = rmNode.getResourceOption().getResource();
      // Mesos does not support disk IO. So not zeroing out disks.
      currentResource.setVirtualCores(0);
      currentResource.setMemory(0);
    }

    private Resource getResourceFromOffer(Collection<Protos.Offer> offers) {
      double cpus = 0.0;
      double mem = 0.0;

      for (Protos.Offer offer : offers) {
        for (Protos.Resource resource : offer.getResourcesList()) {
          if (resource.getName().equalsIgnoreCase("cpus")) {
            cpus += resource.getScalar().getValue();
          } else if (resource.getName().equalsIgnoreCase("mem")) {
            mem += resource.getScalar().getValue();
          }
        }
      }

      return Resource.newInstance((int) mem, (int) cpus);
    }

    private Protos.TaskInfo getTaskInfoForContainer(RMContainer rmContainer,
        ConsumedOffer consumedOffer, Node node) {

        Protos.Offer offer = consumedOffer.getOffers().get(0);
        Container container = rmContainer.getContainer();
        Protos.TaskID taskId = Protos.TaskID.newBuilder()
            .setValue(ContainerTaskStatusRequest.YARN_CONTAINER_TASK_ID_PREFIX + container.getId().toString()).build();

        Protos.ExecutorInfo executorInfo = node.getExecInfo();
        if (executorInfo == null) {
            executorInfo = Protos.ExecutorInfo.newBuilder(
                taskFactory.getExecutorInfoForSlave(offer.getSlaveId()))
                .setFrameworkId(offer.getFrameworkId()).build();
            node.setExecInfo(executorInfo);
        }

        return Protos.TaskInfo.newBuilder()
            .setName("task_" + taskId.getValue())
            .setTaskId(taskId)
            .setSlaveId(offer.getSlaveId())
            .addResources(Protos.Resource.newBuilder()
                .setName("cpus")
                .setType(Protos.Value.Type.SCALAR)
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(container.getResource().getVirtualCores())))
            .addResources(Protos.Resource.newBuilder()
                .setName("mem")
                .setType(Protos.Value.Type.SCALAR)
                .setScalar(Protos.Value.Scalar.newBuilder().setValue(container.getResource().getMemory())))
            .setExecutor(executorInfo)
            .build();
    }
}
