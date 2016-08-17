/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myriad.state;

import java.util.List;
import java.util.Set;

import org.apache.hadoop.net.Node;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.NodeState;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.server.api.protocolrecords.NodeHeartbeatResponse;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.UpdatedContainerInfo;

/**
 * Mock implementation of RMNode interface for unit test cases
 */
public class MockRMNode implements RMNode {
  String hostName;
  NodeId nodeId;
  String nodeAddress;
  String httpAddress;
  int commandPort;
  int httpPort;
  String healthReport;
  long lastHealthReportTime;
  Set<String> nodeLabels;
  String nodeManagerVersion;
  Resource totalCapability;
  String rackName;
  Node node;
  NodeState nodeState;
  List<ContainerId> containersToCleanUp;
  List<ApplicationId> appsToCleanup;
  List<UpdatedContainerInfo> containerUpdates;
  NodeHeartbeatResponse heartbeatResponse;

  public MockRMNode(NodeId nodeId, NodeState nodeState, Node node) {
    this.nodeId = nodeId;
    this.nodeState = nodeState;
    this.node = node;
  }
  
  public NodeState getNodeState() {
    return nodeState;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public void setNodeAddress(String nodeAddress) {
    this.nodeAddress = nodeAddress;
  }

  public void setHttpAddress(String httpAddress) {
    this.httpAddress = httpAddress;
  }

  public void setCommandPort(int commandPort) {
    this.commandPort = commandPort;
  }
  
  public void setHttpPort(int httpPort) {
    this.httpPort = httpPort;
  }

  public void setHealthReport(String healthReport) {
    this.healthReport = healthReport;
  }

  public void setNodeLabels(Set<String> nodeLabels) {
    this.nodeLabels = nodeLabels;
  }

  public void setNodeManagerVersion(String nodeManagerVersion) {
    this.nodeManagerVersion = nodeManagerVersion;
  }

  public void setTotalCapability(Resource totalCapability) {
    this.totalCapability = totalCapability;
  }

  public void setRackName(String rackName) {
    this.rackName = rackName;
  }

  public void setContainersToCleanUp(List<ContainerId> containersToCleanUp) {
    this.containersToCleanUp = containersToCleanUp;
  }

  public void setAppsToCleanup(List<ApplicationId> appsToCleanup) {
    this.appsToCleanup = appsToCleanup;
  }
  
  public void setLastHealthReportTime(long lastHealthReportTime) {
    this.lastHealthReportTime = lastHealthReportTime;
  }

  public void setContainerUpdates(List<UpdatedContainerInfo> containerUpdates) {
    this.containerUpdates = containerUpdates;
  }

  public void setHeartbeatResponse(NodeHeartbeatResponse heartbeatResponse) {
    this.heartbeatResponse = heartbeatResponse;
  }

  @Override
  public NodeId getNodeID() {
    return nodeId;
  }

  @Override
  public String getHostName() {
    return hostName;
  }

  @Override
  public int getCommandPort() {
    return commandPort;
  }

  @Override
  public int getHttpPort() {
    return httpPort;
  }

  @Override
  public String getNodeAddress() {
    return nodeAddress;
  }

  @Override
  public String getHttpAddress() {
    return httpAddress;
  }

  @Override
  public String getHealthReport() {
    return healthReport;
  }

  @Override
  public long getLastHealthReportTime() {
    return lastHealthReportTime;
  }

  @Override
  public String getNodeManagerVersion() {
    return nodeManagerVersion;
  }

  @Override
  public Resource getTotalCapability() {
    return totalCapability;
  }

  @Override
  public String getRackName() {
    return rackName;
  }

  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public NodeState getState() {
    return nodeState;
  }

  @Override
  public List<ContainerId> getContainersToCleanUp() {
    return containersToCleanUp;
  }

  @Override
  public List<ApplicationId> getAppsToCleanup() {
    return appsToCleanup;
  }

  @Override
  public void updateNodeHeartbeatResponseForCleanup(NodeHeartbeatResponse response) {
    //noop
  }

  @Override
  public NodeHeartbeatResponse getLastNodeHeartBeatResponse() {
    return heartbeatResponse;
  }

  @Override
  public List<UpdatedContainerInfo> pullContainerUpdates() {
    return containerUpdates;
  }  

  @Override
  public Set<String> getNodeLabels() {
    return nodeLabels;
  }
}