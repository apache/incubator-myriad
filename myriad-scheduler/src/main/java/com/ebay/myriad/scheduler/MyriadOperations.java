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
import com.ebay.myriad.state.NodeTask;
import com.ebay.myriad.state.SchedulerState;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public void flexUpCluster(NMProfile profile, int instances) {
        Collection<NodeTask> nodes = new HashSet<>();
        for (int i = 0; i < instances; i++) {
            nodes.add(new NodeTask(profile));
        }

        this.schedulerState.addNodes(nodes);
    }

    public void flexDownCluster(NMProfile profile, int numInstancesToScaleDown) {
        Set<NodeTask> activeTasksForProfile = Sets.newHashSet(this.schedulerState.getActiveTasksForProfile(profile));
        List<String> nodesToScaleDown = nodeScaleDownPolicy.getNodesToScaleDown();
        filterUnregisteredNMs(activeTasksForProfile, nodesToScaleDown);

        // TODO(Santosh): Make this more efficient by using a Map<HostName, NodeTask> in scheduler state
        int numActiveTasksScaledDown = 0;
        for (int i = 0; i < numInstancesToScaleDown; i++) {
            for (NodeTask nodeTask : activeTasksForProfile) {
                if (nodesToScaleDown.size() > i && nodesToScaleDown.get(i).equals(nodeTask.getHostname())) {
                    this.schedulerState.makeTaskKillable(nodeTask.getTaskStatus().getTaskId());
                    numActiveTasksScaledDown++;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Marked NodeTask {} on host {} for kill.",
                                nodeTask.getTaskStatus().getTaskId(), nodeTask.getHostname());
                    }
                }
            }
        }

        // Flex down Staging tasks, if any
        int numStagingTasksScaledDown = 0;
        if (numActiveTasksScaledDown < numInstancesToScaleDown) {
            Set<Protos.TaskID> stagingTasks = Sets.newHashSet(this.schedulerState.getStagingTaskIds());

            for (Protos.TaskID taskId : stagingTasks) {
                if (schedulerState.getTask(taskId).getProfile().getName().equals(profile.getName())) {
                  this.schedulerState.makeTaskKillable(taskId);
                  numStagingTasksScaledDown++;
                  if (numStagingTasksScaledDown + numActiveTasksScaledDown == numInstancesToScaleDown) {
                    break;
                  }
                }
            }
        }

        // Flex down Pending tasks, if any
        int numPendingTasksScaledDown = 0;
        if (numStagingTasksScaledDown + numActiveTasksScaledDown < numInstancesToScaleDown) {
          Set<Protos.TaskID> pendingTasks = Sets.newHashSet(this.schedulerState.getPendingTaskIds());

          for (Protos.TaskID taskId : pendingTasks) {
              if (schedulerState.getTask(taskId).getProfile().getName().equals(profile.getName())) {
                this.schedulerState.makeTaskKillable(taskId);
                numPendingTasksScaledDown++;
                if (numActiveTasksScaledDown + numStagingTasksScaledDown + numPendingTasksScaledDown
                    == numInstancesToScaleDown) {
                  break;
                }
              }
            }
        }

        if (numActiveTasksScaledDown + numStagingTasksScaledDown + numPendingTasksScaledDown == 0) {
          LOGGER.info("No Node Managers with profile '{}' found for scaling down.", profile.getName());
        } else {
          LOGGER.info("Flexed down {} active, {} staging  and {} pending Node Managers with '{}' profile.",
              numActiveTasksScaledDown, numStagingTasksScaledDown, numPendingTasksScaledDown, profile.getName());
        }
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
