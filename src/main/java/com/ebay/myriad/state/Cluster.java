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

import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

/**
 * Model which represents the configuration of a cluster
 */
public class Cluster {
    private String clusterId;
    private String clusterName;
    private Collection<NodeTask> nodes;
    private String resourceManagerHost;
    private String resourceManagerPort;
    private double minQuota;

    public Cluster() {
        this.clusterId = UUID.randomUUID().toString();
        this.nodes = new HashSet<>();
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Collection<NodeTask> getNodes() {
        return nodes;
    }

    public void addNode(NodeTask node) {
        this.nodes.add(node);
    }

    public void addNodes(Collection<NodeTask> nodes) {
        this.nodes.addAll(nodes);
    }

    public void removeNode(NodeTask task) {
        this.nodes.remove(task);
    }

    public String getResourceManagerHost() {
        return resourceManagerHost;
    }

    public void setResourceManagerHost(String resourceManagerHost) {
        this.resourceManagerHost = resourceManagerHost;
    }

    public String getResourceManagerPort() {
        return resourceManagerPort;
    }

    public void setResourceManagerPort(String resourceManagerPort) {
        this.resourceManagerPort = resourceManagerPort;
    }

    public double getMinQuota() {
        return minQuota;
    }

    public void setMinQuota(double minQuota) {
        this.minQuota = minQuota;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
