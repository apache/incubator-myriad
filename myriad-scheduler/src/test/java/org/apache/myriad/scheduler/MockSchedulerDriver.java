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
package org.apache.myriad.scheduler;

import java.util.Collection;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.Filters;
import org.apache.mesos.Protos.Offer.Operation;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Request;
import org.apache.mesos.Protos.SlaveID;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.SchedulerDriver;

/**
 * Mock SchedulerDriver implementation for JUnit tests
 */
public class MockSchedulerDriver implements SchedulerDriver {

  @Override
  public Status start() {
    return Protos.Status.DRIVER_RUNNING;
  }

  @Override
  public Status stop(boolean failover) {
    return Protos.Status.DRIVER_STOPPED;
  }

  @Override
  public Status stop() {
    return Protos.Status.DRIVER_STOPPED;
  }

  @Override
  public Status abort() {
    return Protos.Status.DRIVER_ABORTED;
  }

  @Override
  public Status join() {
    return Protos.Status.DRIVER_RUNNING;
  }

  @Override
  public Status run() {
    return Protos.Status.DRIVER_RUNNING;
  }

  @Override
  public Status requestResources(Collection<Request> requests) {
    return null;
  }

  @Override
  public Status launchTasks(Collection<OfferID> offerIds, Collection<TaskInfo> tasks, Filters filters) {
    return null;
  }

  @Override 
  public Status launchTasks(Collection<OfferID> offerIds, Collection<TaskInfo> tasks) {
    return null;
  }

  @Override
  @SuppressWarnings("deprecation")
  public Status launchTasks(OfferID offerId, Collection<TaskInfo> tasks, Filters filters) {
    return null;
  }

  @Override
  @SuppressWarnings("deprecation")
  public Status launchTasks(OfferID offerId, Collection<TaskInfo> tasks) {
    return null;
  }

  @Override
  public Status killTask(TaskID taskId) {
    return null;
  }

  @Override
  public Status acceptOffers(Collection<OfferID> offerIds, Collection<Operation> operations, Filters filters) {
    return null;
  }

  @Override
  public Status declineOffer(OfferID offerId, Filters filters) {
    return null;
  }

  @Override
  public Status declineOffer(OfferID offerId) {
    return null;
  }

  @Override
  public Status reviveOffers() {
    return null;
  }

  @Override
  public Status suppressOffers() {
    return null;
  }

  @Override
  public Status acknowledgeStatusUpdate(TaskStatus status) {
    return null;
  }

  @Override
  public Status sendFrameworkMessage(ExecutorID executorId, SlaveID slaveId, byte[] data) {
    return null;
  }

  @Override
  public Status reconcileTasks(Collection<TaskStatus> statuses) {
    return null;
  }
}