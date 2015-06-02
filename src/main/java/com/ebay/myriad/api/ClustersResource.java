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
import com.ebay.myriad.api.model.FlexDownClusterRequest;
import com.ebay.myriad.api.model.FlexUpClusterRequest;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.MyriadOperations;
import com.ebay.myriad.scheduler.NMProfileManager;
import com.ebay.myriad.state.SchedulerState;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RESTful API to resource manager
 */
@Path("/cluster")
public class ClustersResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClustersResource.class);

    private MyriadConfiguration cfg;
    private SchedulerState schedulerState;
    private NMProfileManager profileManager;
    private MyriadOperations myriadOperations;

    @Inject
    public ClustersResource(MyriadConfiguration cfg,
                            SchedulerState state,
                            NMProfileManager profileManager,
                            MyriadOperations myriadOperations) {
        this.cfg = cfg;
        this.schedulerState = state;
        this.profileManager = profileManager;
        this.myriadOperations = myriadOperations;
    }

    @Timed
    @PUT
    @Path("/flexup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response flexUp(FlexUpClusterRequest request) {
        Preconditions.checkNotNull(request,
                "request object cannot be null or empty");

        // TODO(mohit): Validation
        LOGGER.info("Received Flexup Cluster Request");


        Integer instances = request.getInstances();
        String profile = request.getProfile();

        LOGGER.info("Instances: ", instances);
        LOGGER.info("Profile: ", profile);

        this.myriadOperations.flexUpCluster(instances, profile);
        return Response.ok().build();
    }

    @Timed
    @PUT
    @Path("/flexdown")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response flexDown(FlexDownClusterRequest request) {
        Preconditions.checkNotNull(request,
                "request object cannot be null or empty");

        // TODO(mohit): Make safer.
        this.myriadOperations.flexDownCluster(request.getInstances());
        return Response.ok().build();
    }

}
