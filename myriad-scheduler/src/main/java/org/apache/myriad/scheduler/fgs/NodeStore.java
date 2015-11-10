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
package org.apache.myriad.scheduler.fgs;

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
