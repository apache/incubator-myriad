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
package com.ebay.myriad.api;

import com.codahale.metrics.annotation.Timed;
import com.ebay.myriad.api.model.GetSchedulerStateResponse;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.state.SchedulerState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.mesos.Protos;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Defines the REST API for the current state of Myriad
 */
@Path("/state")
@Produces(MediaType.APPLICATION_JSON)
public class SchedulerStateResource {
    private MyriadConfiguration cfg;
    private SchedulerState state;

    @Inject
    public SchedulerStateResource(final MyriadConfiguration cfg,
                                  final SchedulerState state) {
        this.cfg = cfg;
        this.state = state;
    }

    @Timed
    @GET
    public GetSchedulerStateResponse getState() {
        return new GetSchedulerStateResponse(toStringCollection(state.getPendingTaskIds()),
                toStringCollection(state.getStagingTaskIds()), toStringCollection(state.getActiveTaskIds()),
                toStringCollection(state.getKillableTasks()));
    }

    private Collection<String> toStringCollection(Collection<Protos.TaskID> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        Collection<String> returnCollection = new ArrayList<>();
        for (Protos.TaskID task : collection) {
            returnCollection.add(task.getValue());
        }

        return returnCollection;
    }
}
