/**
 * Copyright 2015 PayPal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.state;

import com.ebay.myriad.scheduler.NMProfile;
import com.ebay.myriad.scheduler.constraints.Constraint;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Attribute;

/**
 * Represents a task to be launched by the executor
 */
public class NodeTask {
    @JsonProperty
    private NMProfile profile;
    @JsonProperty
    private String hostname;
    @JsonProperty
    private Protos.SlaveID slaveId;
    @JsonProperty
    private Protos.TaskStatus taskStatus;

    /**
     * Mesos executor for this node.
     */
    private Protos.ExecutorInfo executorInfo;

    private Constraint constraint;
    private List<Attribute> slaveAttributes;

    public NodeTask(NMProfile profile, Constraint constraint) {
        this.profile = profile;
        this.hostname = "";
        this.constraint = constraint;
    }

    public Protos.SlaveID getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(Protos.SlaveID slaveId) {
        this.slaveId = slaveId;
    }

    public NMProfile getProfile() {
        return profile;
    }

    public void setProfile(NMProfile profile) {
        this.profile = profile;
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
}
