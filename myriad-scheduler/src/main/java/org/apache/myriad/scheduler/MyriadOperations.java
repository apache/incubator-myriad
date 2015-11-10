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
package org.apache.myriad.scheduler;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Status;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.configuration.ServiceConfiguration;
import org.apache.myriad.policy.NodeScaleDownPolicy;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.state.MyriadStateStore;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.apache.myriad.webapp.MyriadWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Myriad scheduler operations
 */
public class MyriadOperations {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyriadOperations.class);
  private final SchedulerState schedulerState;

  private MyriadConfiguration cfg;
  private NodeScaleDownPolicy nodeScaleDownPolicy;
  private MyriadDriverManager driverManager;
  private MyriadWebServer myriadWebServer;
  private MyriadStateStore myriadStateStore;

  @Inject
  public MyriadOperations(MyriadConfiguration cfg, SchedulerState schedulerState,
      NodeScaleDownPolicy nodeScaleDownPolicy, MyriadDriverManager driverManager,
      MyriadWebServer myriadWebServer, RMContext rmContext) {
    this.cfg = cfg;
    this.schedulerState = schedulerState;
    this.nodeScaleDownPolicy = nodeScaleDownPolicy;
    this.driverManager = driverManager;
    this.myriadWebServer = myriadWebServer;
    if (rmContext.getStateStore() instanceof MyriadStateStore) {
      myriadStateStore = (MyriadStateStore) rmContext.getStateStore();
    }
  }

  public void flexUpCluster(ServiceResourceProfile serviceResourceProfile, int instances, Constraint constraint) {
    Collection<NodeTask> nodes = new HashSet<>();
    for (int i = 0; i < instances; i++) {
      NodeTask nodeTask = new NodeTask(serviceResourceProfile, constraint);
      nodeTask.setTaskPrefix(NodeManagerConfiguration.NM_TASK_PREFIX);
      nodes.add(nodeTask);
    }

    LOGGER.info("Adding {} NM instances to cluster", nodes.size());
    this.schedulerState.addNodes(nodes);
  }

  public void flexDownCluster(ServiceResourceProfile serviceResourceProfile, Constraint constraint, int numInstancesToScaleDown) {
    // Flex down Pending tasks, if any
    int numPendingTasksScaledDown = flexDownPendingTasks(serviceResourceProfile, constraint, numInstancesToScaleDown);

    // Flex down Staging tasks, if any
    int numStagingTasksScaledDown = flexDownStagingTasks(serviceResourceProfile, constraint,
        numInstancesToScaleDown - numPendingTasksScaledDown);

    // Flex down Active tasks, if any
    int numActiveTasksScaledDown = flexDownActiveTasks(serviceResourceProfile, constraint,
        numInstancesToScaleDown - numPendingTasksScaledDown - numStagingTasksScaledDown);

    if (numActiveTasksScaledDown + numStagingTasksScaledDown + numPendingTasksScaledDown == 0) {
      LOGGER.info("No Node Managers with profile '{}' and constraint '{}' found for scaling down.",
          serviceResourceProfile.getName(), constraint == null ? "null" : constraint.toString());
    } else {
      LOGGER.info("Flexed down {} active, {} staging  and {} pending Node Managers with " + "'{}' profile and constraint '{}'.",
          numActiveTasksScaledDown, numStagingTasksScaledDown, numPendingTasksScaledDown, serviceResourceProfile.getName(),
          constraint == null ? "null" : constraint.toString());
    }
  }

  /**
   * Flexup a service
   *
   * @param instances
   * @param serviceName
   */
  public void flexUpAService(int instances, String serviceName) throws MyriadBadConfigurationException {
    final ServiceConfiguration auxTaskConf = cfg.getServiceConfiguration(serviceName);

    int totalflexInstances = instances + getFlexibleInstances(serviceName);
    Integer maxInstances = auxTaskConf.getMaxInstances().orNull();
    if (maxInstances != null && maxInstances > 0) {
      // check number of instances
      // sum of active, staging, pending should be < maxInstances
      if (totalflexInstances > maxInstances) {
        LOGGER.error("Current number of active, staging, pending and requested instances: {}" +
                     ", while it is greater then max instances allowed: {}", totalflexInstances, maxInstances);
        throw new MyriadBadConfigurationException(
            "Current number of active, staging, pending instances and requested: " + totalflexInstances +
            ", while it is greater then max instances allowed: " + maxInstances);
      }
    }

    final Double cpu = auxTaskConf.getCpus().or(ServiceConfiguration.DEFAULT_CPU);
    final Double mem = auxTaskConf.getJvmMaxMemoryMB().or(ServiceConfiguration.DEFAULT_MEMORY);

    Collection<NodeTask> nodes = new HashSet<>();
    for (int i = 0; i < instances; i++) {
      NodeTask nodeTask = new NodeTask(new ServiceResourceProfile(serviceName, cpu, mem), null);
      nodeTask.setTaskPrefix(serviceName);
      nodes.add(nodeTask);
    }

    LOGGER.info("Adding {} {} instances to cluster", nodes.size(), serviceName);
    this.schedulerState.addNodes(nodes);
  }

  /**
   * Flexing down any service defined in the configuration
   *
   * @param numInstancesToScaleDown
   * @param serviceName             - name of the service
   */
  public void flexDownAService(int numInstancesToScaleDown, String serviceName) {
    LOGGER.info("About to flex down {} instances of {}", numInstancesToScaleDown, serviceName);

    int numScaledDown = 0;

    // Flex down Pending tasks, if any
    if (numScaledDown < numInstancesToScaleDown) {
      Collection<Protos.TaskID> pendingTasks = this.schedulerState.getPendingTaskIds(serviceName);

      for (Protos.TaskID taskId : pendingTasks) {
        this.schedulerState.makeTaskKillable(taskId);
        numScaledDown++;
        if (numScaledDown >= numInstancesToScaleDown) {
          break;
        }
      }
    }
    int numPendingTasksScaledDown = numScaledDown;

    // Flex down Staging tasks, if any
    if (numScaledDown < numInstancesToScaleDown) {
      Collection<Protos.TaskID> stagingTasks = this.schedulerState.getStagingTaskIds(serviceName);

      for (Protos.TaskID taskId : stagingTasks) {
        this.schedulerState.makeTaskKillable(taskId);
        numScaledDown++;
        if (numScaledDown >= numInstancesToScaleDown) {
          break;
        }
      }
    }
    int numStagingTasksScaledDown = numScaledDown - numPendingTasksScaledDown;

    Set<NodeTask> activeTasks = this.schedulerState.getActiveTasksByType(serviceName);
    if (numScaledDown < numInstancesToScaleDown) {
      for (NodeTask nodeTask : activeTasks) {
        this.schedulerState.makeTaskKillable(nodeTask.getTaskStatus().getTaskId());
        numScaledDown++;
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Marked NodeTask {} on host {} for kill.", nodeTask.getTaskStatus().getTaskId(), nodeTask.getHostname());
        }
        if (numScaledDown >= numInstancesToScaleDown) {
          break;
        }
      }
    }

    LOGGER.info("Flexed down {} of {} instances including {} staging instances, and {} pending instances of {}", numScaledDown,
        numInstancesToScaleDown, numStagingTasksScaledDown, numPendingTasksScaledDown, serviceName);
  }

  private int flexDownPendingTasks(ServiceResourceProfile profile, Constraint constraint, int numInstancesToScaleDown) {
    return numInstancesToScaleDown > 0 ? flexDownTasks(schedulerState.getPendingTaskIDsForProfile(profile), profile, constraint,
        numInstancesToScaleDown) : 0;
  }

  private int flexDownStagingTasks(ServiceResourceProfile profile, Constraint constraint, int numInstancesToScaleDown) {
    return numInstancesToScaleDown > 0 ? flexDownTasks(schedulerState.getStagingTaskIDsForProfile(profile), profile, constraint,
        numInstancesToScaleDown) : 0;
  }

  private int flexDownActiveTasks(ServiceResourceProfile profile, Constraint constraint, int numInstancesToScaleDown) {
    if (numInstancesToScaleDown > 0) {
      List<Protos.TaskID> activeTasksForProfile = Lists.newArrayList(schedulerState.getActiveTaskIDsForProfile(profile));
      nodeScaleDownPolicy.apply(activeTasksForProfile);
      return flexDownTasks(activeTasksForProfile, profile, constraint, numInstancesToScaleDown);
    }
    return 0;
  }

  private int flexDownTasks(Collection<Protos.TaskID> taskIDs, ServiceResourceProfile profile, Constraint constraint,
                            int numInstancesToScaleDown) {
    int numInstancesScaledDown = 0;
    for (Protos.TaskID taskID : taskIDs) {
      NodeTask nodeTask = schedulerState.getTask(taskID);
      if (nodeTask.getProfile().getName().equals(profile.getName()) && meetsConstraint(nodeTask, constraint)) {
        this.schedulerState.makeTaskKillable(taskID);
        numInstancesScaledDown++;
        if (numInstancesScaledDown == numInstancesToScaleDown) {
          break;
        }
      }
    }
    return numInstancesScaledDown;
  }

  private boolean meetsConstraint(NodeTask nodeTask, Constraint constraint) {
    if (constraint != null) {
      if (constraint.equals(nodeTask.getConstraint())) {
        return true;
      }
      switch (constraint.getType()) {
        case LIKE:
          LikeConstraint likeConstraint = (LikeConstraint) constraint;
          if (likeConstraint.isConstraintOnHostName()) {
            return likeConstraint.matchesHostName(nodeTask.getHostname());
          } else {
            return likeConstraint.matchesSlaveAttributes(nodeTask.getSlaveAttributes());
          }

        default:
          return false;
      }
    }
    return true;
  }

  public Integer getFlexibleInstances(String taskPrefix) {
    return this.schedulerState.getActiveTaskIds(taskPrefix).size() +
           this.schedulerState.getStagingTaskIds(taskPrefix).size() +
           this.schedulerState.getPendingTaskIds(taskPrefix).size();
  }

  /**
   * Shutdown framework means the Mesos driver is stopped taking down the executors and associated tasks
   */
  public void shutdownFramework() {
    LOGGER.info("Received request to shutdown Myriad Framework..");
    Status driverStatus = driverManager.getDriverStatus();

    if (Status.DRIVER_RUNNING != driverStatus) {
      LOGGER.warn("Driver is not running. Status: " + driverStatus);
    } else {
      // Stop the driver, tasks, and executor.
      driverStatus = driverManager.stopDriver(false);
      LOGGER.info("Myriad driver was shutdown with status " + driverStatus);
    }

    try {
      myriadWebServer.stop();
      LOGGER.info("Myriad webserver was shutdown successfully");
    } catch (Exception e) {
      LOGGER.info("Failed to shutdown Myriad webserver: " + e.getMessage());
    }

    if (myriadStateStore != null) {
      try {
        myriadStateStore.removeMyriadState();
        LOGGER.info("Myriad State store was removed successfully.");
      } catch (Exception e) {
        LOGGER.info("Failed to remove Myriad state store: " + e.getMessage());
      }
    }
  }
}
