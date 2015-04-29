package com.ebay.myriad.scheduler;

import com.ebay.myriad.executor.ContainerTaskStatusRequest;
import com.ebay.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeAddedSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.NodeUpdateSchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Receives offers from mesos and projects them as "Node Capacities" to YARN's scheduler.
 */
public class YarnNodeCapacityManager extends BaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(YarnNodeCapacityManager.class);
    private final AbstractYarnScheduler yarnScheduler;
    private final MyriadDriver myriadDriver;
    private final TaskFactory taskFactory;

    // TODO(Santosh): Define a single class that encapsulates (Offer, SchedulerNode, containersBeforeSchedMap)
    private Map<String, OfferWrapper> offersMap = new ConcurrentHashMap<>(200, 0.75f, 50);
    private Map<String, SchedulerNode> schedulerNodes = new ConcurrentHashMap<>(200, 0.75f, 50);
    private Map<NodeId, Set<RMContainer>> containersBeforeSchedMap = new ConcurrentHashMap<>(200, 0.75f, 50);
    private Map<String, Protos.SlaveID> slaves = new ConcurrentHashMap<>(200, 0.75f, 50);
    // cache the ExecutorInfos that were used to launch MyriadExecutor
    private Map<Protos.SlaveID, Protos.ExecutorInfo> executorInfos = new ConcurrentHashMap<>(200, 0.75f, 50);

    // Constants used to determine whether to apply or unapply offer
    private static final int APPLY_OFFER = 1;
    private static final int UNAPPLY_OFFER = -1;

    @Inject
    public YarnNodeCapacityManager(InterceptorRegistry registry,
                                   AbstractYarnScheduler yarnScheduler,
                                   MyriadDriver myriadDriver,
                                   TaskFactory taskFactory) {
        if (registry != null) {
            registry.register(this);
        }
        this.yarnScheduler = yarnScheduler;
        this.myriadDriver = myriadDriver;
        this.taskFactory = taskFactory;
    }

    private static class OfferWrapper {
      private Protos.Offer offer;
      private boolean applied = false;

      public OfferWrapper(Protos.Offer offer) {
        this.offer = offer;
      }
    }

    public void addResourceOffers(List<Protos.Offer> offers) {
        for (Protos.Offer offer : offers) {
            if (schedulerNodes.containsKey(offer.getHostname())) {
                offersMap.put(offer.getHostname(), new OfferWrapper(offer));
                slaves.put(offer.getHostname(), offer.getSlaveId());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("addResourceOffers: caching offer for host {}, offer id {}",
                        offer.getHostname(), offer.getId().getValue());
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("addResourceOffers: declining offer for host {} as NM didn't register yet",
                        offer.getHostname());
                }
                myriadDriver.getDriver().declineOffer(offer.getId());
            }
        }
    }

    @Override
    public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {
        switch (event.getType()) {
            case STARTED: {
                // TODO(Santosh): We can't zero out resources here in all cases. For e.g.
                // this event might be fired when an existing node is rejoining the
                // cluster (NM restart) as well. Sometimes this event can have a list of
                // container statuses, which, capacity/fifo schedulers seem to handle
                // (perhaps due to work preserving NM restart).
                RMNode addedRMNode = context.getRMNodes().get(event.getNodeId());
                ResourceOption resourceOption = addedRMNode.getResourceOption();
                resourceOption.getResource().setVirtualCores(0);
                resourceOption.getResource().setMemory(0);
                addedRMNode.setResourceOption(
                    ResourceOption.newInstance(resourceOption.getResource(),
                        RMNode.OVER_COMMIT_TIMEOUT_MILLIS_DEFAULT));
            }
            break;

            case EXPIRE: {
                schedulerNodes.remove(event.getNodeId().getHost());
            }
            break;

            case STATUS_UPDATE: {
                if (!(event instanceof RMNodeStatusEvent)) {
                    LOGGER.error("{} not an instance of {}", event.getClass().getName(),
                        RMNodeStatusEvent.class.getName());
                    return;
                }
                RMNodeStatusEvent statusEvent = (RMNodeStatusEvent) event;
                RMNode rmNode = context.getRMNodes().get(event.getNodeId());
                SchedulerNode schedulerNode = yarnScheduler.getSchedulerNode(rmNode.getNodeID());
                String hostName = rmNode.getNodeID().getHost();

                int rmSchNodeNumContainers = schedulerNode.getNumContainers();

                List<ContainerStatus> nmContainers = statusEvent.getContainers();

                if (nmContainers.size() != rmSchNodeNumContainers) {
                    LOGGER.warn("Node: {}, Num Containers known by RM scheduler: {}, Num containers NM is reporting: {}",
                        hostName, rmSchNodeNumContainers, nmContainers.size());
                }

                for (ContainerStatus status : nmContainers) {
                    ContainerId containerId = status.getContainerId();
                    if (status.getState() == ContainerState.COMPLETE) {
                        requestExecutorToSendTaskStatusUpdate(slaves.get(hostName),
                            containerId, Protos.TaskState.TASK_FINISHED);
                    } else { // state == NEW | RUNNING
                        requestExecutorToSendTaskStatusUpdate(slaves.get(hostName),
                            containerId, Protos.TaskState.TASK_RUNNING);
                    }
                }
            }
            break;

            default: {
                LOGGER.warn("Unexpected fall through. event: {}", event.getClass().getName());
            }
            break;
        }
    }

    @Override
    public void beforeSchedulerEventHandled(SchedulerEvent event) {
      // Apply the offers stashed till now.
      for (Map.Entry<String, OfferWrapper> entry : offersMap.entrySet()) {
        applyOffer(schedulerNodes.get(entry.getKey()), entry.getValue());
      }
    }

    private void applyOffer(SchedulerNode schedulerNode, OfferWrapper offerWrapper) {
      RMNode rmNode = schedulerNode.getRMNode();
      updateRMNodeCapacity(rmNode, offerWrapper, APPLY_OFFER);

      containersBeforeSchedMap.put(rmNode.getNodeID(), new HashSet<>(
            schedulerNode.getRunningContainers()));
    }

    @Override
    public void afterSchedulerEventHandled(SchedulerEvent event) {
        switch (event.getType()) {
            case NODE_ADDED: {
                if (!(event instanceof NodeAddedSchedulerEvent)) {
                    LOGGER.error("{} not an instance of {}", event.getClass().getName(),
                        NodeAddedSchedulerEvent.class.getName());
                    return;
                }
                NodeAddedSchedulerEvent nodeAddedEvent = (NodeAddedSchedulerEvent) event;
                String host = nodeAddedEvent.getAddedRMNode().getNodeID().getHost();
                if (schedulerNodes.containsKey(host)) {
                    LOGGER.warn("Duplicate node being added. Host: {}", host);
                }
                schedulerNodes.put(host, yarnScheduler.getSchedulerNode(nodeAddedEvent.getAddedRMNode().getNodeID()));
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
                String host = rmNode.getNodeID().getHost();

                Set<RMContainer> containersBeforeSched = this.containersBeforeSchedMap.get(rmNode.getNodeID());
                if (containersBeforeSched == null) {
                  containersBeforeSched = Collections.emptySet();
                }

                Set<RMContainer> containersAfterSched = new HashSet<>(yarnScheduler.getSchedulerNode(
                    rmNode.getNodeID()).getRunningContainers());
                Set<RMContainer> containersAllocatedDueToMesosOffer =
                    Sets.difference(containersAfterSched, containersBeforeSched).immutableCopy();

                OfferWrapper offerWrapper = offersMap.get(host);
                if (offerWrapper == null) {
                  LOGGER.debug("No offer available for {}", host);
                  return;
                }
                Protos.Offer offer = offerWrapper.offer;
                if (containersAllocatedDueToMesosOffer.isEmpty()) {
                  myriadDriver.getDriver().declineOffer(offer.getId());
                  offersMap.remove(host);
                  updateRMNodeCapacity(rmNode, offerWrapper, UNAPPLY_OFFER);
                  LOGGER.debug("Declining unused offer for {}", offer.getHostname());
                } else {
                    List<Protos.TaskInfo> tasks = Lists.newArrayList();
                    for (RMContainer newContainer : containersAllocatedDueToMesosOffer) {
                        tasks.add(getTaskInfoForContainer(newContainer, offer));
                    }
                    myriadDriver.getDriver().launchTasks(offer.getId(), tasks);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Launched containers: {} for offer: {}",
                            containersAllocatedDueToMesosOffer.toString(),
                            offer.getId().getValue());
                    }
                    offersMap.remove(host);
                }
            }
            break;

            default: {
                LOGGER.warn("Unexpected fall through. event: {}", event.getClass().getName());
            }
            break;
        }
    }

    /**
     * Updates the current capacity of the node by either increasing or
     * decreasing by the amount specified by offer.
     *
     * @param rmNode node to update
     * @param offerWrapper offer holder
     * @param factor either 1 or -1 indicating whether to increase or decrease
     * the capacity
     */
    private void updateRMNodeCapacity(RMNode rmNode, OfferWrapper offerWrapper,
        int factor) {

      if (factor == APPLY_OFFER) {
        if (offerWrapper.applied) {
          LOGGER.warn("Offer is already applied {}", offerWrapper.offer);
          return;
        }
      } else {
        if (!offerWrapper.applied) {
          LOGGER.debug("Offer is already unapplied {}", offerWrapper.offer);
          return;
        }
      }

      Resource mesosOffer = getResourceFromOffer(offerWrapper.offer);
      ResourceOption currentResourceOption = rmNode.getResourceOption();
      Resource currentResource = currentResourceOption.getResource();

      // Increase/decrease capacity by offer
      currentResource.setVirtualCores(currentResource.getVirtualCores()
          + factor * mesosOffer.getVirtualCores());
      currentResource.setMemory(currentResource.getMemory()
          + factor * mesosOffer.getMemory());

      rmNode.setResourceOption(
          ResourceOption.newInstance(currentResourceOption.getResource(),
            RMNode.OVER_COMMIT_TIMEOUT_MILLIS_DEFAULT));

      if (LOGGER.isDebugEnabled()) {
        String operation = (factor == APPLY_OFFER) ? "applied" : "removed";
        LOGGER.debug("Offer: {} Node: {}, Offer: {}, New NM Capacity: {}",
            rmNode.getNodeID(), mesosOffer, currentResource);
      }
    }

    /**
     * sends a request to executor on the given slave to send back a status update
     * for the mesos task launched for this container.
     *
     * @param slaveId
     * @param containerId
     * @param taskState
     */
    private void requestExecutorToSendTaskStatusUpdate(Protos.SlaveID slaveId,
                                                       ContainerId containerId,
                                                       Protos.TaskState taskState) {
        ContainerTaskStatusRequest containerTaskStatusRequest = new ContainerTaskStatusRequest();
        containerTaskStatusRequest.setMesosTaskId(
            ContainerTaskStatusRequest.YARN_CONTAINER_TASK_ID_PREFIX + containerId.toString());
        containerTaskStatusRequest.setState(taskState.name());
        myriadDriver.getDriver().sendFrameworkMessage(
            getExecutorId(slaveId),
            slaveId,
            new Gson().toJson(containerTaskStatusRequest).getBytes(Charset.defaultCharset()));
    }

    private Protos.ExecutorID getExecutorId(Protos.SlaveID slaveId) {
        return Protos.ExecutorID.newBuilder().setValue(
            TaskFactory.NMTaskFactoryImpl.EXECUTOR_PREFIX + slaveId.getValue()).build();
    }

    private Resource getResourceFromOffer(Protos.Offer offer) {
        double cpus = 0.0;
        double mem = 0.0;
        if (offer != null) {
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

    private Protos.TaskInfo getTaskInfoForContainer(RMContainer rmContainer, Protos.Offer offer) {
        Container container = rmContainer.getContainer();
        Protos.TaskID taskId = Protos.TaskID.newBuilder()
            .setValue(ContainerTaskStatusRequest.YARN_CONTAINER_TASK_ID_PREFIX + container.getId().toString()).build();

        if (LOGGER.isDebugEnabled()) {
            double offerCpu = 0.0, offerMemory = 0.0;
            for (Protos.Resource resource : offer.getResourcesList()) {
                if (resource.getName().equals("cpus")) {
                    offerCpu = resource.getScalar().getValue();
                } else if (resource.getName().equals("mem")) {
                    offerMemory = resource.getScalar().getValue();
                }
            }
            LOGGER.debug("Launching Container {} with cpu: {}, mem: {} on slave {} for offer with cpu: {}, mem: {}",
                container.getId(),
                container.getResource().getVirtualCores(),
                container.getResource().getMemory(),
                offer.getHostname(),
                offerCpu,
                offerMemory);
        }

        Protos.ExecutorInfo executorInfo = executorInfos.get(offer.getSlaveId());
        if (executorInfo == null) {
            executorInfo = Protos.ExecutorInfo.newBuilder(
                taskFactory.getExecutorInfoForSlave(offer.getSlaveId()))
                .setFrameworkId(offer.getFrameworkId()).build();
            executorInfos.put(offer.getSlaveId(), executorInfo);
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
