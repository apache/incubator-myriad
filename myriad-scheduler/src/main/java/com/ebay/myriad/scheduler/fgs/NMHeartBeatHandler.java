package com.ebay.myriad.scheduler.fgs;

import com.ebay.myriad.executor.ContainerTaskStatusRequest;
import com.ebay.myriad.scheduler.MyriadDriver;
import com.ebay.myriad.scheduler.SchedulerUtils;
import com.ebay.myriad.scheduler.TaskFactory;
import com.ebay.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.ebay.myriad.state.SchedulerState;
import com.google.gson.Gson;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.util.resource.Resources;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles node manager heartbeat.
 */
public class NMHeartBeatHandler extends BaseInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      NMHeartBeatHandler.class);

  private final AbstractYarnScheduler yarnScheduler;
  private final MyriadDriver myriadDriver;
  private final YarnNodeCapacityManager yarnNodeCapacityMgr;
  private final OfferLifecycleManager offerLifecycleMgr;
  private final NodeStore nodeStore;
  private final SchedulerState state;

  @Inject
  public NMHeartBeatHandler(
      InterceptorRegistry registry,
      AbstractYarnScheduler yarnScheduler,
      MyriadDriver myriadDriver,
      YarnNodeCapacityManager yarnNodeCapacityMgr,
      OfferLifecycleManager offerLifecycleMgr,
      NodeStore nodeStore,
      SchedulerState state) {

    if (registry != null) {
      registry.register(this);
    }

    this.yarnScheduler = yarnScheduler;
    this.myriadDriver = myriadDriver;
    this.yarnNodeCapacityMgr = yarnNodeCapacityMgr;
    this.offerLifecycleMgr = offerLifecycleMgr;
    this.nodeStore = nodeStore;
    this.state = state;
  }

  @Override
  public CallBackFilter getCallBackFilter() {
    return new CallBackFilter() {
      @Override
      public boolean allowCallBacksForNode(NodeId nodeManager) {
        return SchedulerUtils.isEligibleForFineGrainedScaling(nodeManager.getHost(), state);
      }
    };
  }

  @Override
  public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {
    switch (event.getType()) {
      case STARTED: {
        RMNode rmNode = context.getRMNodes().get(event.getNodeId());
        Resource totalCapability = rmNode.getTotalCapability();
        if (totalCapability.getMemory() != 0 ||
            totalCapability.getVirtualCores() != 0) {
          LOGGER.warn("FineGrainedScaling feature got invoked for a " +
                  "NM with non-zero capacity. Host: {}, Mem: {}, CPU: {}. Setting the NM's capacity to (0G,0CPU)",
              rmNode.getHostName(),
              totalCapability.getMemory(), totalCapability.getVirtualCores());
          totalCapability.setMemory(0);
          totalCapability.setVirtualCores(0);
        }
      }
      break;

      case EXPIRE: {
        nodeStore.remove(event.getNodeId().getHost());
      }
      break;

      case STATUS_UPDATE: {
        handleStatusUpdate(event, context);
      }
      break;

      default:
        break;
    }
  }

  private void handleStatusUpdate(RMNodeEvent event, RMContext context) {
    if (!(event instanceof RMNodeStatusEvent)) {
      LOGGER.error("{} not an instance of {}", event.getClass().getName(),
          RMNodeStatusEvent.class.getName());
      return;
    }

    RMNodeStatusEvent statusEvent = (RMNodeStatusEvent) event;
    RMNode rmNode = context.getRMNodes().get(event.getNodeId());
    String hostName = rmNode.getNodeID().getHost();

    nodeStore.getNode(hostName).snapshotRunningContainers();
    sendStatusUpdatesToMesosForCompletedContainers(statusEvent);

    // New capacity of the node =
    // resources under use on the node (due to previous offers) +
    // new resources offered by mesos for the node
    yarnNodeCapacityMgr.setNodeCapacity(rmNode,
            Resources.add(getResourcesUnderUse(statusEvent),
                getNewResourcesOfferedByMesos(hostName)));
  }

  private Resource getNewResourcesOfferedByMesos(String hostname) {
    OfferFeed feed = offerLifecycleMgr.getOfferFeed(hostname);
    if (feed == null) {
      LOGGER.debug("No offer feed for: {}", hostname);
      return Resource.newInstance(0, 0);
    }
    List<Offer> offers = new ArrayList<>();
    Protos.Offer offer;
    while ((offer = feed.poll()) != null) {
      offers.add(offer);
      offerLifecycleMgr.markAsConsumed(offer);
    }
    Resource fromMesosOffers = OfferUtils.getYarnResourcesFromMesosOffers(offers);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("NM on host {} got {} CPUs and {} memory from mesos",
          hostname, fromMesosOffers.getVirtualCores(), fromMesosOffers.getMemory());
    }

    return fromMesosOffers;
  }

  private Resource getResourcesUnderUse(RMNodeStatusEvent statusEvent) {
    Resource usedResources = Resource.newInstance(0, 0);
    for (ContainerStatus status : statusEvent.getContainers()) {
      if (status.getState() == ContainerState.NEW || status.getState() == ContainerState.RUNNING) {
        Resources.addTo(usedResources, yarnScheduler.getRMContainer(status.getContainerId()).getAllocatedResource());
      }
    }
    return usedResources;
  }

  private void sendStatusUpdatesToMesosForCompletedContainers(RMNodeStatusEvent statusEvent) {
    // Send task update to Mesos
    Protos.SlaveID slaveId = nodeStore.getNode(statusEvent.getNodeId().getHost()).getSlaveId();
    for (ContainerStatus status : statusEvent.getContainers()) {
      ContainerId containerId = status.getContainerId();
      if (status.getState() == ContainerState.COMPLETE) {
        requestExecutorToSendTaskStatusUpdate(slaveId, containerId, Protos.TaskState.TASK_FINISHED);
      } else { // state == NEW | RUNNING
        requestExecutorToSendTaskStatusUpdate(slaveId, containerId, Protos.TaskState.TASK_RUNNING);
      }
    }
  }


  /**
   * sends a request to executor on the given slave to send back a status update
   * for the mesos task launched for this container.
   *
   * TODO(Santosh):
   *  Framework messages are unreliable. Try a NM auxiliary service that can help
   *  send out the status messages from NM itself. NM and MyriadExecutor would need
   *  to be merged into a single process.
   *
   * @param slaveId
   * @param containerId
   * @param taskState
   */
  private void requestExecutorToSendTaskStatusUpdate(Protos.SlaveID slaveId,
      ContainerId containerId,
      Protos.TaskState taskState) {
    final String mesosTaskId = ContainerTaskStatusRequest.YARN_CONTAINER_TASK_ID_PREFIX + containerId.toString();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Sending out framework message requesting the executor to send {} status for task: {}",
          taskState.name(), mesosTaskId);
    }
    ContainerTaskStatusRequest containerTaskStatusRequest = new ContainerTaskStatusRequest();
    containerTaskStatusRequest.setMesosTaskId(mesosTaskId);
    containerTaskStatusRequest.setState(taskState.name());
    myriadDriver.getDriver().sendFrameworkMessage(getExecutorId(slaveId), slaveId,
        new Gson().toJson(containerTaskStatusRequest).getBytes(Charset.defaultCharset()));
  }

  private Protos.ExecutorID getExecutorId(Protos.SlaveID slaveId) {
    return Protos.ExecutorID.newBuilder().setValue(
        TaskFactory.NMTaskFactoryImpl.EXECUTOR_PREFIX + slaveId.getValue()).build();
  }
}
