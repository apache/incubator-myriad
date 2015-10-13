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
package com.ebay.myriad.scheduler;

import com.ebay.myriad.policy.NodeScaleDownPolicy;
import com.ebay.myriad.scheduler.constraints.Constraint;
import com.ebay.myriad.scheduler.constraints.LikeConstraint;
import com.ebay.myriad.state.NodeTask;
import com.ebay.myriad.state.SchedulerState;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Myriad scheduler operations
 */
public class MyriadOperations {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadOperations.class);
    private final SchedulerState schedulerState;
    private NodeScaleDownPolicy nodeScaleDownPolicy;

    @Inject
    public MyriadOperations(SchedulerState schedulerState,
                            NodeScaleDownPolicy nodeScaleDownPolicy) {
      this.schedulerState = schedulerState;
      this.nodeScaleDownPolicy = nodeScaleDownPolicy;
    }

    public void flexUpCluster(NMProfile profile, int instances, Constraint constraint) {
        Collection<NodeTask> nodes = new HashSet<>();
        for (int i = 0; i < instances; i++) {
            nodes.add(new NodeTask(profile, constraint));
        }

        this.schedulerState.addNodes(nodes);
    }

    public void flexDownCluster(NMProfile profile, Constraint constraint, int numInstancesToScaleDown) {
        // Flex down Pending tasks, if any
        int numPendingTasksScaledDown = flexDownPendingTasks(
            profile, constraint, numInstancesToScaleDown);

        // Flex down Staging tasks, if any
        int numStagingTasksScaledDown = flexDownStagingTasks(
            profile, constraint, numInstancesToScaleDown - numPendingTasksScaledDown);

        // Flex down Active tasks, if any
        int numActiveTasksScaledDown = flexDownActiveTasks(
            profile, constraint, numInstancesToScaleDown - numPendingTasksScaledDown - numStagingTasksScaledDown);

        if (numActiveTasksScaledDown + numStagingTasksScaledDown + numPendingTasksScaledDown == 0) {
          LOGGER.info("No Node Managers with profile '{}' and constraint '{}' found for scaling down.",
              profile.getName(), constraint == null ? "null" : constraint.toString());
        } else {
          LOGGER.info("Flexed down {} active, {} staging  and {} pending Node Managers with " +
              "'{}' profile and constraint '{}'.", numActiveTasksScaledDown, numStagingTasksScaledDown,
              numPendingTasksScaledDown, profile.getName(), constraint == null ? "null" : constraint.toString());
        }
    }

    private int flexDownPendingTasks(NMProfile profile, Constraint constraint, int numInstancesToScaleDown) {
      return numInstancesToScaleDown > 0 ? flexDownTasks(schedulerState.getPendingTaskIDsForProfile(profile),
          profile, constraint, numInstancesToScaleDown) : 0;
    }

  private int flexDownStagingTasks(NMProfile profile, Constraint constraint, int numInstancesToScaleDown) {
      return numInstancesToScaleDown > 0 ? flexDownTasks(schedulerState.getStagingTaskIDsForProfile(profile),
          profile, constraint, numInstancesToScaleDown) : 0;
    }

    private int flexDownActiveTasks(NMProfile profile, Constraint constraint, int numInstancesToScaleDown) {
      if (numInstancesToScaleDown > 0) {
        List<Protos.TaskID> activeTasksForProfile = Lists.newArrayList(schedulerState.getActiveTaskIDsForProfile(profile));
        nodeScaleDownPolicy.apply(activeTasksForProfile);
        return flexDownTasks(activeTasksForProfile, profile, constraint, numInstancesToScaleDown);
      }
      return 0;
    }

  private int flexDownTasks(Collection<Protos.TaskID> taskIDs, NMProfile profile,
                              Constraint constraint, int numInstancesToScaleDown) {
      int numInstancesScaledDown = 0;
      for (Protos.TaskID taskID : taskIDs) {
        NodeTask nodeTask = schedulerState.getTask(taskID);
        if (nodeTask.getProfile().getName().equals(profile.getName()) &&
            meetsConstraint(nodeTask, constraint)) {
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

}
