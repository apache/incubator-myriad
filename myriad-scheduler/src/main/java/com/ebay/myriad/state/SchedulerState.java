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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.mesos.Protos;
import org.apache.mesos.Protos.SlaveID;

import com.ebay.myriad.state.utils.StoreContext;

/**
 * Represents the state of the Myriad scheduler
 */
public class SchedulerState {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerState.class);

    private Map<Protos.TaskID, NodeTask> tasks;
    private Set<Protos.TaskID> pendingTasks;
    private Set<Protos.TaskID> stagingTasks;
    private Set<Protos.TaskID> activeTasks;
    private Set<Protos.TaskID> lostTasks;
    private Set<Protos.TaskID> killableTasks;
    private Protos.FrameworkID frameworkId;
    private MyriadStateStore stateStore;

    public SchedulerState(MyriadStateStore stateStore) {
        this.tasks = new ConcurrentHashMap<>();
        this.pendingTasks = new HashSet<>();
        this.stagingTasks = new HashSet<>();
        this.activeTasks = new HashSet<>();
        this.lostTasks = new HashSet<>();
        this.killableTasks = new HashSet<>();
        this.stateStore = stateStore;
        loadStateStore();
    }

    public void addNodes(Collection<NodeTask> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            LOGGER.info("No nodes to add");
            return;
        }
        for (NodeTask node : nodes) {
            Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue(String.format("nm.%s.%s", node.getProfile().getName(), UUID.randomUUID()))
                    .build();
            addTask(taskId, node);
            LOGGER.info("Marked taskId {} pending, size of pending queue {}", taskId.getValue(), this.pendingTasks.size());
            makeTaskPending(taskId);
        }

    }

    // TODO (sdaingade) Clone NodeTask
    public synchronized void addTask(Protos.TaskID taskId, NodeTask node) {
        this.tasks.put(taskId, node);
        updateStateStore();
    }

    public synchronized void updateTask(Protos.TaskStatus taskStatus) {
        Objects.requireNonNull(taskStatus, "TaskStatus object shouldn't be null");
        Protos.TaskID taskId = taskStatus.getTaskId();
        if (this.tasks.containsKey(taskId)) {
            this.tasks.get(taskId).setTaskStatus(taskStatus);
        }
        updateStateStore();
    }

    public synchronized void makeTaskPending(Protos.TaskID taskId) {
        Objects.requireNonNull(taskId,
                "taskId cannot be empty or null");
        pendingTasks.add(taskId);
        stagingTasks.remove(taskId);
        activeTasks.remove(taskId);
        lostTasks.remove(taskId);
        killableTasks.remove(taskId);
        updateStateStore();
    }

    public synchronized void makeTaskStaging(Protos.TaskID taskId) {
        Objects.requireNonNull(taskId,
                "taskId cannot be empty or null");

        pendingTasks.remove(taskId);
        stagingTasks.add(taskId);
        activeTasks.remove(taskId);
        lostTasks.remove(taskId);
        killableTasks.remove(taskId);
        updateStateStore();
    }

    public synchronized void makeTaskActive(Protos.TaskID taskId) {
        Objects.requireNonNull(taskId,
                "taskId cannot be empty or null");

        pendingTasks.remove(taskId);
        stagingTasks.remove(taskId);
        activeTasks.add(taskId);
        lostTasks.remove(taskId);
        killableTasks.remove(taskId);
        updateStateStore();
    }

    public synchronized void makeTaskLost(Protos.TaskID taskId) {
        Objects.requireNonNull(taskId,
                "taskId cannot be empty or null");

        pendingTasks.remove(taskId);
        stagingTasks.remove(taskId);
        activeTasks.remove(taskId);
        lostTasks.add(taskId);
        killableTasks.remove(taskId);
        updateStateStore();
    }

    public synchronized void makeTaskKillable(Protos.TaskID taskId) {
        Objects.requireNonNull(taskId,
                "taskId cannot be empty or null");

        pendingTasks.remove(taskId);
        stagingTasks.remove(taskId);
        activeTasks.remove(taskId);
        lostTasks.remove(taskId);
        killableTasks.add(taskId);
        updateStateStore();
    }

    public synchronized Set<Protos.TaskID> getKillableTasks() {
        return Collections.unmodifiableSet(this.killableTasks);
    }

    // TODO (sdaingade) Clone NodeTask
    public synchronized NodeTask getTask(Protos.TaskID taskId) {
        return this.tasks.get(taskId);
    }

    public synchronized void removeTask(Protos.TaskID taskId) {
        this.pendingTasks.remove(taskId);
        this.stagingTasks.remove(taskId);
        this.activeTasks.remove(taskId);
        this.lostTasks.remove(taskId);
        this.killableTasks.remove(taskId);
        this.tasks.remove(taskId);
        updateStateStore();
    }

    public synchronized Set<Protos.TaskID> getPendingTaskIds() {
        return Collections.unmodifiableSet(this.pendingTasks);
    }

    public synchronized Set<Protos.TaskID> getActiveTaskIds() {
        return Collections.unmodifiableSet(this.activeTasks);
    }

    public synchronized Collection<NodeTask> getActiveTasks() {
        List<NodeTask> activeNodeTasks = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(activeTasks)
                && CollectionUtils.isNotEmpty(tasks.values())) {
            for (Map.Entry<Protos.TaskID, NodeTask> entry : tasks.entrySet()) {
                if (activeTasks.contains(entry.getKey())) {
                    activeNodeTasks.add(entry.getValue());
                }
            }
        }
        return Collections.unmodifiableCollection(activeNodeTasks);
    }

    public synchronized Collection<NodeTask> getActiveTasksForProfile(NMProfile profile) {
      List<NodeTask> activeNodeTasks = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(activeTasks)
          && CollectionUtils.isNotEmpty(tasks.values())) {
        for (Map.Entry<Protos.TaskID, NodeTask> entry : tasks.entrySet()) {
          NodeTask nodeTask = entry.getValue();
          if (activeTasks.contains(entry.getKey()) && nodeTask.getProfile().getName().equals(profile.getName())) {
            activeNodeTasks.add(nodeTask);
          }
        }
      }
      return Collections.unmodifiableCollection(activeNodeTasks);
    }

  // TODO (sdaingade) Clone NodeTask
    public synchronized NodeTask getNodeTask(SlaveID slaveId) {
        for (Map.Entry<Protos.TaskID, NodeTask> entry : tasks.entrySet()) {
            if (entry.getValue().getSlaveId() != null &&
                entry.getValue().getSlaveId().equals(slaveId)) {
                return entry.getValue(); 
            }
        }
        return null;
    }

    public synchronized Set<Protos.TaskID> getStagingTaskIds() {
        return Collections.unmodifiableSet(this.stagingTasks);
    }

    public synchronized Set<Protos.TaskID> getLostTaskIds() {
        return Collections.unmodifiableSet(this.lostTasks);
    }

    // TODO (sdaingade) Currently cannot return unmodifiableCollection
    // as this will break ReconcileService code
    public synchronized Collection<Protos.TaskStatus> getTaskStatuses() {
        Collection<Protos.TaskStatus> taskStatuses = new ArrayList<>(this.tasks.size());
        Collection<NodeTask> tasks = this.tasks.values();
        for (NodeTask task : tasks) {
            Protos.TaskStatus taskStatus = task.getTaskStatus();
            if (taskStatus != null) {
                taskStatuses.add(taskStatus);
            }
        }

        return taskStatuses;
    }

    public synchronized boolean hasTask(Protos.TaskID taskID) {
        return this.tasks.containsKey(taskID);
    }

    public synchronized Protos.FrameworkID getFrameworkID() {
         return this.frameworkId;
    }

    public synchronized void setFrameworkId(Protos.FrameworkID newFrameworkId) {
        this.frameworkId = newFrameworkId;
        updateStateStore();
    }

    private synchronized void updateStateStore() {
        if (this.stateStore == null) {
            LOGGER.debug("Could not update state to state store as HA is disabled");
            return;
        }

        try {
            StoreContext sc = new StoreContext(frameworkId, tasks, pendingTasks,
                stagingTasks, activeTasks, lostTasks, killableTasks);
            stateStore.storeMyriadState(sc);
        } catch (Exception e) {
            LOGGER.error("Failed to update scheduler state to state store", e);
        }
    }

    private synchronized void loadStateStore() {
        if (this.stateStore == null) {
            LOGGER.debug("Could not load state from state store as HA is disabled");
            return;
        }

        try {
            StoreContext sc = stateStore.loadMyriadState();
            if (sc != null) {
                this.frameworkId = sc.getFrameworkId();
                this.tasks.putAll(sc.getTasks());
                this.pendingTasks.addAll(sc.getPendingTasks());
                this.stagingTasks.addAll(sc.getStagingTasks());
                this.activeTasks.addAll(sc.getActiveTasks());
                this.lostTasks.addAll(sc.getLostTasks());
                this.killableTasks.addAll(sc.getKillableTasks());

                LOGGER.info("Loaded Myriad state from state store successfully.");
                LOGGER.debug("State Store state includes " +
                  "frameworkId: {}, pending tasks count: {}, staging tasks count: {} " +
                  "active tasks count: {}, lost tasks count: {}, " +
                  "and killable tasks count: {}", frameworkId.getValue(),
                  this.pendingTasks.size(), this.stagingTasks.size(),
                  this.activeTasks.size(), this.lostTasks.size(),
                  this.killableTasks.size());
            }
        }  catch (Exception e) {
            LOGGER.error("Failed to read scheduler state from state store", e);
        }
   }
}
