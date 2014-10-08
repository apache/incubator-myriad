/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.mesos.Protos.TaskID;

import java.util.UUID;

public class NodeTask {
    @JsonProperty
    private String taskId;
    @JsonProperty
    private String clusterId;
    @JsonProperty
    private NMProfile profile;
    @JsonProperty
    private String hostname;

    public NodeTask(String clusterId, NMProfile profile) {
        super();
        this.taskId = UUID.randomUUID().toString();
        this.clusterId = clusterId;
        this.profile = profile;
        this.hostname = "";
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public NMProfile getProfile() {
        return profile;
    }

    public void setProfile(NMProfile profile) {
        this.profile = profile;
    }

    @JsonIgnore
    public TaskID getMesosTaskId() {
        return TaskID.newBuilder().setValue(taskId).build();
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
