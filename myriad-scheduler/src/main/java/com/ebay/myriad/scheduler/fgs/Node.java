package com.ebay.myriad.scheduler.fgs;

import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.yarn.server.resourcemanager.rmcontainer.RMContainer;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;
import org.apache.mesos.Protos;

/**
 * Abstraction that encapsulates YARN and Mesos view of a node.
 */
public class Node {
  /**
   * Mesos slave id associated with this node.
   */
  private Protos.SlaveID slaveId;

  /**
   * Mesos executor on this node.
   */
  private Protos.ExecutorInfo execInfo;

  /**
   * YARN scheduler's representation of this node.
   */
  private SchedulerNode node;

  /**
   * Snapshot of containers allocated by YARN scheduler.
   * This need not reflect the current state. It is meant to be used by the
   * Myriad scheduler.
   */
  private Set<RMContainer> containerSnapshot;

  public Node(SchedulerNode node) {
    this.node = node;
  }

  public SchedulerNode getNode() {
    return node;
  }

  public Protos.SlaveID getSlaveId() {
    return slaveId;
  }

  public void setSlaveId(Protos.SlaveID slaveId) {
    this.slaveId = slaveId;
  }

  public Protos.ExecutorInfo getExecInfo() {
    return execInfo;
  }

  public void setExecInfo(Protos.ExecutorInfo execInfo) {
    this.execInfo = execInfo;
  }

  public void snapshotRunningContainers() {
    this.containerSnapshot = new HashSet<>(node.getRunningContainers());
  }

  public void removeContainerSnapshot() {
    this.containerSnapshot = null;
  }

  public Set<RMContainer> getContainerSnapshot() {
    return this.containerSnapshot;
  }
}
