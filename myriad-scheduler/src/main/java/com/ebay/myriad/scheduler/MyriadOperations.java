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
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        int numPendingTasksScaledDown = 0;
          Set<Protos.TaskID> pendingTasks = Sets.newHashSet(this.schedulerState.getPendingTaskIds());

          for (Protos.TaskID taskId : pendingTasks) {
            NodeTask nodeTask = schedulerState.getTask(taskId);
            if (nodeTask != null && nodeTask.getProfile().getName().equals(profile.getName()) &&
                meetsConstraint(nodeTask, constraint)) {
              this.schedulerState.makeTaskKillable(taskId);
              numPendingTasksScaledDown++;
              if (numPendingTasksScaledDown == numInstancesToScaleDown) {
                break;
              }
            }
          }

        // Flex down Staging tasks, if any
        int numStagingTasksScaledDown = 0;
        if (numPendingTasksScaledDown < numInstancesToScaleDown) {
          Set<Protos.TaskID> stagingTasks = Sets.newHashSet(this.schedulerState.getStagingTaskIds());

          for (Protos.TaskID taskId : stagingTasks) {
            NodeTask nodeTask = schedulerState.getTask(taskId);
            if (nodeTask != null && nodeTask.getProfile().getName().equals(profile.getName()) &&
                meetsConstraint(nodeTask, constraint)) {
              this.schedulerState.makeTaskKillable(taskId);
              numStagingTasksScaledDown++;
              if (numStagingTasksScaledDown + numPendingTasksScaledDown == numInstancesToScaleDown) {
                break;
              }
            }
          }
        }

        int numActiveTasksScaledDown = 0;
        if (numPendingTasksScaledDown + numStagingTasksScaledDown < numInstancesToScaleDown) {
          Set<NodeTask> activeTasksForProfile = Sets.newHashSet(this.schedulerState.getActiveTasksForProfile(profile));
          List<String> nodesToScaleDown = nodeScaleDownPolicy.getNodesToScaleDown();
          filterUnregisteredNMs(activeTasksForProfile, nodesToScaleDown);

          for (int i = 0; i < numInstancesToScaleDown - (numPendingTasksScaledDown + numStagingTasksScaledDown); i++) {
            for (NodeTask nodeTask : activeTasksForProfile) {
              if (nodesToScaleDown.size() > i &&
                  nodesToScaleDown.get(i).equals(nodeTask.getHostname()) &&
                  meetsConstraint(nodeTask, constraint)) {
                this.schedulerState.makeTaskKillable(nodeTask.getTaskStatus().getTaskId());
                numActiveTasksScaledDown++;
                if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("Marked NodeTask {} on host {} for kill.",
                      nodeTask.getTaskStatus().getTaskId(), nodeTask.getHostname());
                }
              }
            }
          }
        }

        if (numActiveTasksScaledDown + numStagingTasksScaledDown + numPendingTasksScaledDown == 0) {
          LOGGER.info("No Node Managers with profile '{}' and constraint {} found for scaling down.",
              profile.getName(), constraint.toString());
        } else {
          LOGGER.info("Flexed down {} active, {} staging  and {} pending Node Managers with '{}' profile.",
              numActiveTasksScaledDown, numStagingTasksScaledDown, numPendingTasksScaledDown, profile.getName());
        }
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

  private void filterUnregisteredNMs(Set<NodeTask> activeTasksForProfile, List<String> registeredNMHosts) {
    // If a NM is flexed down it takes time for the RM to realize the NM is no longer up
    // We need to make sure we filter out nodes that have already been flexed down
    // but have not disappeared from the RM's view of the cluster
    for (Iterator<String> iterator = registeredNMHosts.iterator(); iterator.hasNext();) {
        String nodeToScaleDown = iterator.next();
        boolean nodePresentInMyriad = false;
        for (NodeTask nodeTask : activeTasksForProfile) {
            if (nodeTask.getHostname().equals(nodeToScaleDown)) {
                nodePresentInMyriad = true;
                break;
            }
        }
        if (!nodePresentInMyriad) {
            iterator.remove();
        }
    }
  }
}
