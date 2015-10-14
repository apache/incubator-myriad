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
package org.apache.myriad.api;

import com.codahale.metrics.annotation.Timed;
import org.apache.myriad.api.model.FlexDownClusterRequest;
import org.apache.myriad.api.model.FlexUpClusterRequest;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.MyriadOperations;
import org.apache.myriad.scheduler.NMProfileManager;
import org.apache.myriad.state.SchedulerState;
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
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response flexUp(FlexUpClusterRequest request) {
        Preconditions.checkNotNull(request,
                "request object cannot be null or empty");

        LOGGER.info("Received Flexup Cluster Request");

        Integer instances = request.getInstances();
        String profile = request.getProfile();

        LOGGER.info("Instances: {}", instances);
        LOGGER.info("Profile: {}", profile);

        // Validate profile request
        Response.ResponseBuilder response = Response.status(Response.Status.ACCEPTED);
        if (!this.profileManager.exists(profile)) {
            response.status(Response.Status.BAD_REQUEST)
                    .entity("Profile does not exist: " + profile);
            LOGGER.error("Provided profile does not exist " + profile);
        } else if (!this.isValidInstanceSize(instances)) {
            response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid instance size: " + instances);
            LOGGER.error("Invalid instance size request " + instances);
        }

        Response returnResponse = response.build();
        if (returnResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
            this.myriadOperations.flexUpCluster(instances, profile);
        }

        return returnResponse;
    }

    @Timed
    @PUT
    @Path("/flexdown")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response flexDown(FlexDownClusterRequest request) {
        Preconditions.checkNotNull(request,
                "request object cannot be null or empty");

        Integer instances = request.getInstances();

        LOGGER.info("Received flexdown request");
        LOGGER.info("Instances: " + instances);

        Response.ResponseBuilder response = Response.status(Response.Status.ACCEPTED);

        if (!this.isValidInstanceSize(instances)) {
            response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid instance size: " + instances);
            LOGGER.error("Invalid instance size request " + instances);
        }

        // warn that number of requested instances isn't available
        // but instances will still be flexed down
        Integer flexibleInstances = this.getFlexibleInstances();
        if (flexibleInstances < instances)  {
            response.entity("Number of requested instances is greater than available.");
            // just doing a simple check here. pass the requested number of instances
            // to myriadOperations and let it sort out how many actually get flexxed down.
            LOGGER.warn("Requested number of instances greater than available: {} < {}", flexibleInstances, instances);
        }

        Response returnResponse = response.build();
        if (returnResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
            this.myriadOperations.flexDownCluster(instances);
        }
        return returnResponse;
    }

    private boolean isValidInstanceSize(Integer instances) {
        return (instances > 0);
    }

    // TODO (mohit): put this in Myriad Operations?
    private Integer getFlexibleInstances() {
        // this follows the logic of myriadOperations.flexDownCluster
        return this.schedulerState.getActiveTaskIds().size()
                + this.schedulerState.getStagingTaskIds().size()
                + this.schedulerState.getPendingTaskIds().size();
    }

}
