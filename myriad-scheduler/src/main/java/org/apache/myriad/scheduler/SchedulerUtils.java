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

import com.google.common.base.Preconditions;
import java.util.Collection;
import org.apache.commons.collections.CollectionUtils;
import org.apache.mesos.Protos;
import org.apache.myriad.configuration.NodeManagerConfiguration;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utilities for scheduling with the mesos offers
 */
public class SchedulerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerUtils.class);

  public static boolean isUniqueHostname(Protos.OfferOrBuilder offer, NodeTask taskToLaunch, Collection<NodeTask> tasks) {
    Preconditions.checkArgument(offer != null);
    String offerHostname = offer.getHostname();

    if (!CollectionUtils.isEmpty(tasks)) {
      for (NodeTask task : tasks) {
        if (offerHostname.equalsIgnoreCase(task.getHostname())) {
          LOGGER.debug("Offer's hostname {} is not unique", offerHostname);
          return false;
        }
      }
    }
    LOGGER.debug("Offer's hostname {} is unique", offerHostname);
    return true;
  }

  /**
   * Determines if a given host has a nodemanager running with zero profile. Node Managers
   * launched with zero profile (zero cpu & memory) are eligible for fine grained scaling.
   * Node Managers launched with a non-zero profile size are not eligible for fine grained scaling.
   *
   * @param hostName
   * @return
   */
  public static boolean isEligibleForFineGrainedScaling(String hostName, SchedulerState state) {
    for (NodeTask activeNMTask : state.getActiveTasksByType(NodeManagerConfiguration.NM_TASK_PREFIX)) {
      if (activeNMTask.getProfile().getCpus() == 0 &&
          activeNMTask.getProfile().getMemory() == 0 &&
          activeNMTask.getHostname().equals(hostName)) {
        return true;
      }
    }
    return false;
  }
}
