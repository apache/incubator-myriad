/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.apache.myriad.scheduler;

import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.policy.NodeScaleDownPolicy;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
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
    private MyriadConfiguration cfg;
    private NMProfileManager profileManager;
    private NodeScaleDownPolicy nodeScaleDownPolicy;

    @Inject
    public MyriadOperations(MyriadConfiguration cfg,
                            SchedulerState schedulerState,
                            NMProfileManager profileManager,
                            NodeScaleDownPolicy nodeScaleDownPolicy) {
        this.cfg = cfg;
        this.schedulerState = schedulerState;
        this.profileManager = profileManager;
        this.nodeScaleDownPolicy = nodeScaleDownPolicy;
    }

    public void flexUpCluster(int instances, String profile) {
        Collection<NodeTask> nodes = new HashSet<>();
        for (int i = 0; i < instances; i++) {
            nodes.add(new NodeTask(profileManager.get(profile)));
        }

        LOGGER.info("Adding {} instances to cluster", nodes.size());
        this.schedulerState.addNodes(nodes);
    }

    public void flexDownCluster(int numInstancesToScaleDown) {
        LOGGER.info("About to flex down {} instances", numInstancesToScaleDown);

        int numScaledDown = 0;
        Set<NodeTask> activeTasks = Sets.newHashSet(this.schedulerState.getActiveTasks());
        List<String> nodesToScaleDown = nodeScaleDownPolicy.getNodesToScaleDown();
        if (activeTasks.size() > nodesToScaleDown.size()) {
            LOGGER.info("Will skip flexing down {} Node Manager instances that were launched but " +
                    "have not yet registered with Resource Manager.", activeTasks.size() - nodesToScaleDown.size());
        }
        
        // If a NM is flexed down it takes time for the RM to realize the NM is no longer up
        // We need to make sure we filter out nodes that have already been flexed down
        // but have not disappeared from the RM's view of the cluster
        for (Iterator<String> iterator = nodesToScaleDown.iterator(); iterator.hasNext();) {
            String nodeToScaleDown = iterator.next();
            boolean nodePresentInMyriad = false;
            for (NodeTask nodeTask : activeTasks) {
                if (nodeTask.getHostname().equals(nodeToScaleDown)) {
                    nodePresentInMyriad = true;    
                    break;
                }
            }
            if (!nodePresentInMyriad) {
                iterator.remove();
            }
        }

        // TODO(Santosh): Make this more efficient by using a Map<HostName, NodeTask> in scheduler state
        for (int i = 0; i < numInstancesToScaleDown; i++) {
            for (NodeTask nodeTask : activeTasks) {
                if (nodesToScaleDown.size() > i && nodesToScaleDown.get(i).equals(nodeTask.getHostname())) {
                    this.schedulerState.makeTaskKillable(nodeTask.getTaskStatus().getTaskId());
                    numScaledDown++;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Marked NodeTask {} on host {} for kill.",
                                nodeTask.getTaskStatus().getTaskId(), nodeTask.getHostname());
                    }
                }
            }
        }
        int numActiveTasksScaledDown = numScaledDown;

        // Flex down Staging tasks, if any
        if (numScaledDown < numInstancesToScaleDown) {
            Set<Protos.TaskID> stagingTasks = Sets.newHashSet(this.schedulerState.getStagingTaskIds());

            for (Protos.TaskID taskId : stagingTasks) {
                this.schedulerState.makeTaskKillable(taskId);
                numScaledDown++;
                if (numScaledDown == numInstancesToScaleDown) {
                    break;
                }
            }
        }
        int numStagingTasksScaledDown = numScaledDown - numActiveTasksScaledDown;

        // Flex down Pending tasks, if any
        if (numScaledDown < numInstancesToScaleDown) {
            Set<Protos.TaskID> pendingTasks = Sets.newHashSet(this.schedulerState.getPendingTaskIds());

            for (Protos.TaskID taskId : pendingTasks) {
                this.schedulerState.makeTaskKillable(taskId);
                numScaledDown++;
                if (numScaledDown == numInstancesToScaleDown) {
                    break;
                }
            }
        }
        int numPendingTasksScaledDown = numScaledDown - numStagingTasksScaledDown;

        LOGGER.info("Flexed down {} of {} instances including {} staging instances, and {} pending instances.",
                numScaledDown, numInstancesToScaleDown, numStagingTasksScaledDown, numPendingTasksScaledDown);
    }
}
