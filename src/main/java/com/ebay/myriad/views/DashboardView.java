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
package com.ebay.myriad.views;


import com.ebay.myriad.state.Cluster;
import com.ebay.myriad.state.SchedulerState;

import java.util.Collection;
import java.util.Collections;

public class DashboardView {
    // todo:  (kgs) appears to be used
//    public static final String TEMPLATE_NAME = "master.mustache";
    private SchedulerState schedulerState;

    public DashboardView(SchedulerState schedulerState) {
        this.schedulerState = schedulerState;
    }

    public SchedulerState getSchedulerState() {
        return schedulerState;
    }

    public Collection<String> getPendingTasks() {
        return Collections.EMPTY_LIST;
    }

    public Collection<String> getStagingTasks() {
        return Collections.EMPTY_LIST;
    }

    public Collection<String> getKillableTasks() {
        return Collections.EMPTY_LIST;
    }

    public Collection<Cluster> getClusters() {
        return Collections.EMPTY_LIST;
    }

    public Collection<String> getActiveTasks() {
        return Collections.EMPTY_LIST;
    }
}
