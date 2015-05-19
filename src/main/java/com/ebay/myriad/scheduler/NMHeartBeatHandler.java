package com.ebay.myriad.scheduler;

import java.nio.charset.Charset;
import java.util.List;

import javax.inject.Inject;

import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.ContainerState;
import org.apache.hadoop.yarn.api.records.ContainerStatus;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeStatusEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebay.myriad.executor.ContainerTaskStatusRequest;
import com.ebay.myriad.scheduler.yarn.interceptor.BaseInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.InterceptorRegistry;
import com.google.gson.Gson;

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

  @Inject
  public NMHeartBeatHandler(
      InterceptorRegistry registry,
      AbstractYarnScheduler yarnScheduler,
      MyriadDriver myriadDriver,
      YarnNodeCapacityManager yarnNodeCapacityMgr,
      OfferLifecycleManager offerLifecycleMgr,
      NodeStore nodeStore) {

    if (registry != null) {
      registry.register(this);
    }

    this.yarnScheduler = yarnScheduler;
    this.myriadDriver = myriadDriver;
    this.yarnNodeCapacityMgr = yarnNodeCapacityMgr;
    this.offerLifecycleMgr = offerLifecycleMgr;
    this.nodeStore = nodeStore;
  }

  @Override
  public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context) {
    switch (event.getType()) {
      case STARTED: {
        // TODO (Santosh) We can't zero out resources here in all cases. For e.g.
        // this event might be fired when an existing node is rejoining the
        // cluster (NM restart) as well. Sometimes this event can have a list of
        // container statuses, which, capacity/fifo schedulers seem to handle
        // (perhaps due to work preserving NM restart).
        RMNode rmNode = context.getRMNodes().get(event.getNodeId());
        yarnNodeCapacityMgr.removeCapacity(rmNode);
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

      default: {
        LOGGER.debug("Unhandled event: {}", event.getClass().getName());
      }
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
    SchedulerNode schedulerNode = yarnScheduler.getSchedulerNode(rmNode.getNodeID());
    String hostName = rmNode.getNodeID().getHost();

    int rmSchNodeNumContainers = schedulerNode.getNumContainers();
    List<ContainerStatus> nmContainers = statusEvent.getContainers();

    if (nmContainers.size() != rmSchNodeNumContainers) {
      LOGGER.warn("Node: {}, Num Containers known by RM scheduler: {}, "
          + "Num containers NM is reporting: {}",
          hostName, rmSchNodeNumContainers, nmContainers.size());
    }

    // Send task update to Mesos
    for (ContainerStatus status : nmContainers) {
      ContainerId containerId = status.getContainerId();
      Protos.SlaveID slaveId = nodeStore.getNode(hostName).getSlaveId();
      if (status.getState() == ContainerState.COMPLETE) {
        LOGGER.debug("Task complete: {}", containerId);
        yarnNodeCapacityMgr.removeCapacity(rmNode, containerId);
        requestExecutorToSendTaskStatusUpdate(
            slaveId,
            containerId, Protos.TaskState.TASK_FINISHED);
      } else { // state == NEW | RUNNING
        requestExecutorToSendTaskStatusUpdate(
            slaveId,
            containerId, Protos.TaskState.TASK_RUNNING);
      }
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
}
