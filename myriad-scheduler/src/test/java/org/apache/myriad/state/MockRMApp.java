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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.NodeId;
import org.apache.hadoop.yarn.api.records.Priority;
import org.apache.hadoop.yarn.api.records.ReservationId;
import org.apache.hadoop.yarn.api.records.ResourceRequest;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMApp;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppMetrics;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.RMAppState;
import org.apache.hadoop.yarn.server.resourcemanager.rmapp.attempt.RMAppAttempt;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNode;

/**
 * Mock RMApp for unit tests
 */
public class MockRMApp implements RMApp {
  static final int DT = 1000000;
  String user = "yarn";
  String name = "mock-app";
  String queue = "mock-queue";
  long start = System.currentTimeMillis();
  long submit = start - (5 * DT);
  long finish = start + (15 * DT);
  RMAppState state = RMAppState.NEW;
  String applicationType = "mock";
  ApplicationId id;
  ApplicationSubmissionContext context;
  String tUrl = "localhost:8080";
  String oUrl = "localhost:8081";
  int maxAppAttempts = 5;
  
  Map<ApplicationAttemptId, RMAppAttempt> attempts = new HashMap<ApplicationAttemptId, RMAppAttempt>();

  public MockRMApp(int newId, long time, RMAppState state) {
    finish = time;
    id = ApplicationId.newInstance(System.currentTimeMillis(), newId);
    context = ApplicationSubmissionContext.newInstance(id, name, queue, Priority.newInstance(0), null, false, false, newId, null, applicationType);
    this.state = state;
  }
  
  @Override
  public void handle(RMAppEvent event) {

  }

  @Override
  public ApplicationId getApplicationId() {
    return id;
  }

  @Override
  public ApplicationSubmissionContext getApplicationSubmissionContext() {
    return context;
  }

  @Override
  public RMAppState getState() {
    return state;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public float getProgress() {
    return 0;
  }

  @Override
  public RMAppAttempt getRMAppAttempt(ApplicationAttemptId appAttemptId) {
    return null;
  }

  @Override
  public String getQueue() {
    return queue;
  }

  @Override
  public void setQueue(String queue) {
    this.queue = queue;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public RMAppAttempt getCurrentAppAttempt() {
    return null;
  }

  @Override
  public Map<ApplicationAttemptId, RMAppAttempt> getAppAttempts() {
    return attempts;
  }

  @Override
  public ApplicationReport createAndGetApplicationReport(String clientUserName, boolean allowAccess) {
    return null;
  }

  @Override
  public int pullRMNodeUpdates(Collection<RMNode> updatedNodes) {
    return 0;
  }

  @Override
  public long getFinishTime() {
    return finish;
  }

  @Override
  public long getStartTime() {
    return start;
  }

  @Override
  public long getSubmitTime() {
    return submit;
  }

  @Override
  public String getTrackingUrl() {
    return this.tUrl;
  }

  @Override
  public String getOriginalTrackingUrl() {
    return this.oUrl;
  }

  @Override
  public StringBuilder getDiagnostics() {
    return null;
  }

  @Override
  public FinalApplicationStatus getFinalApplicationStatus() {
    return null;
  }

  @Override
  public int getMaxAppAttempts() {
    return this.maxAppAttempts;
  }

  @Override
  public String getApplicationType() {
    return this.applicationType;
  }

  @Override
  public Set<String> getApplicationTags() {
    return null;
  }

  @Override
  public boolean isAppFinalStateStored() {
    return false;
  }

  @Override
  public Set<NodeId> getRanNodes() {
    return null;
  }

  @Override
  public YarnApplicationState createApplicationState() {
    return null;
  }

  @Override
  public RMAppMetrics getRMAppMetrics() {
    return null;
  }

  @Override
  public ReservationId getReservationId() {
    return null;
  }

  @Override
  public ResourceRequest getAMResourceRequest() {
    return null;
  }
}