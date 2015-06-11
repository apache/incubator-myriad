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
package com.ebay.myriad.api.model;

import java.util.Collection;

/**
 * Response for the current state of Myriad
 */
public class GetSchedulerStateResponse {
    private Collection<String> pendingTasks;
    private Collection<String> stagingTasks;
    private Collection<String> activeTasks;
    private Collection<String> killableTasks;

    public GetSchedulerStateResponse() {

    }

    public GetSchedulerStateResponse(Collection<String> pendingTasks,
                                     Collection<String> stagingTasks, Collection<String> activeTasks,
                                     Collection<String> killableTasks) {
        this.pendingTasks = pendingTasks;
        this.stagingTasks = stagingTasks;
        this.activeTasks = activeTasks;
        this.killableTasks = killableTasks;
    }

    public Collection<String> getPendingTasks() {
        return pendingTasks;
    }

    public Collection<String> getStagingTasks() {
        return stagingTasks;
    }

    public Collection<String> getActiveTasks() {
        return activeTasks;
    }

    public Collection<String> getKillableTasks() {
        return killableTasks;
    }

}
