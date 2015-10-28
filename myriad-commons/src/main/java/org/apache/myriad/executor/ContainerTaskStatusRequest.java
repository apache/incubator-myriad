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
package org.apache.myriad.executor;

/**
 * Sent as a "framework message" to the executor. The executor sends a "status report" for
 * the mesos task id (placeholder task).
 */
public class ContainerTaskStatusRequest {
  public static final String YARN_CONTAINER_TASK_ID_PREFIX = "yarn_";
  private String mesosTaskId; // YARN_CONTAINER_TASK_ID_PREFIX + <container_id>
  private String state; // Protos.TaskState.name()

  public String getMesosTaskId() {
    return mesosTaskId;
  }

  public void setMesosTaskId(String mesosTaskId) {
    this.mesosTaskId = mesosTaskId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
