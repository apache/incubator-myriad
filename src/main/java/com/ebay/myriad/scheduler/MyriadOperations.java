/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
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

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.state.Cluster;
import com.ebay.myriad.state.NodeTask;
import com.ebay.myriad.state.SchedulerState;
import com.google.inject.Inject;
import org.apache.mesos.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MyriadOperations {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MyriadOperations.class);

    private MyriadConfiguration cfg;
    private SchedulerState schedulerState;
    private NMProfileManager profileManager;

    @Inject
    public MyriadOperations(MyriadConfiguration cfg,
                            SchedulerState schedulerState, NMProfileManager profileManager) {
        super();
        this.cfg = cfg;
        this.schedulerState = schedulerState;
        this.profileManager = profileManager;
    }

    public void flexUpCluster(int instances, String profile) {
        Collection<NodeTask> nodes = new HashSet<>();
        for (int i = 0; i < instances; i++) {
            nodes.add(new NodeTask(profileManager.get(profile)));
        }

        LOGGER.info("Adding {} instances to cluster", nodes.size());
        this.schedulerState.addNodes(nodes);
    }

    public void flexDownCluster(int n) {
        AtomicInteger instances = new AtomicInteger(n);
        Set<Protos.TaskID> activeTaskIds = this.schedulerState.getActiveTaskIds();
        Iterator<Protos.TaskID> iterator = activeTaskIds.iterator();
        while (instances.get() > 0) {
            this.schedulerState.makeTaskKillable(iterator.next());
            instances.decrementAndGet();
        }
        LOGGER.info("Removed {} instances from cluster", instances);
    }
}
