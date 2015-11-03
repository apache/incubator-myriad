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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import java.util.List;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Attribute;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.TaskUtils;
import org.apache.myriad.scheduler.constraints.Constraint;

/**
 * Represents a task to be launched by the executor
 */
public class NodeTask {
  @JsonProperty
  private String hostname;
  @JsonProperty
  private Protos.SlaveID slaveId;
  @JsonProperty
  private Protos.TaskStatus taskStatus;
  @JsonProperty
  private String taskPrefix;
  @JsonProperty
  private ServiceResourceProfile serviceresourceProfile;

  @Inject
  TaskUtils taskUtils;
  /**
   * Mesos executor for this node.
   */
  private Protos.ExecutorInfo executorInfo;

  private Constraint constraint;
  private List<Attribute> slaveAttributes;

  public NodeTask(ServiceResourceProfile profile, Constraint constraint) {
    this.serviceresourceProfile = profile;
    this.hostname = "";
    this.constraint = constraint;
  }

  public Protos.SlaveID getSlaveId() {
    return slaveId;
  }

  public void setSlaveId(Protos.SlaveID slaveId) {
    this.slaveId = slaveId;
  }

  public Constraint getConstraint() {
    return constraint;
  }

  public String getHostname() {
    return this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public Protos.TaskStatus getTaskStatus() {
    return taskStatus;
  }

  public void setTaskStatus(Protos.TaskStatus taskStatus) {
    this.taskStatus = taskStatus;
  }

  public Protos.ExecutorInfo getExecutorInfo() {
    return executorInfo;
  }

  public void setExecutorInfo(Protos.ExecutorInfo executorInfo) {
    this.executorInfo = executorInfo;
  }

  public void setSlaveAttributes(List<Attribute> slaveAttributes) {
    this.slaveAttributes = slaveAttributes;
  }

  public List<Attribute> getSlaveAttributes() {
    return slaveAttributes;
  }

  public String getTaskPrefix() {
    return taskPrefix;
  }

  public void setTaskPrefix(String taskPrefix) {
    this.taskPrefix = taskPrefix;
  }

  public ServiceResourceProfile getProfile() {
    return serviceresourceProfile;
  }

  public void setProfile(ServiceResourceProfile serviceresourceProfile) {
    this.serviceresourceProfile = serviceresourceProfile;
  }
}
