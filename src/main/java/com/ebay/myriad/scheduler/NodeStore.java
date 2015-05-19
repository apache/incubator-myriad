package com.ebay.myriad.scheduler;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.yarn.server.resourcemanager.scheduler.SchedulerNode;

/**
 * A store for all Node instances managed by this Myriad instance.
 */
public class NodeStore {
  private ConcurrentHashMap<String, Node> nodeMap;

  public NodeStore() {
    nodeMap = new ConcurrentHashMap<>(200, 0.75f, 50);
  }

  private String getKey(SchedulerNode schedNode) {
    return schedNode.getNodeID().getHost();
  }

  public void add(SchedulerNode schedNode) {
    nodeMap.put(getKey(schedNode), new Node(schedNode));
  }

  public void remove(String hostname) {
    nodeMap.remove(hostname);
  }

  public Node getNode(String hostname) {
    return nodeMap.get(hostname);
  }

  public boolean isPresent(String hostname) {
    return nodeMap.containsKey(hostname);
  }
}
