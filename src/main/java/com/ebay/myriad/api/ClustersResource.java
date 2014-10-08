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
import com.ebay.myriad.api.model.*;
import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.scheduler.MyriadOperations;
import com.ebay.myriad.scheduler.NMProfileManager;
import com.ebay.myriad.state.Cluster;
import com.ebay.myriad.state.SchedulerState;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/api/clusters")
@Produces(MediaType.APPLICATION_JSON)
public class ClustersResource {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ClustersResource.class);

    private MyriadConfiguration cfg;
    private SchedulerState schedulerState;
    private NMProfileManager profileManager;
    private MyriadOperations myriadOperations;

    @Inject
    public ClustersResource(MyriadConfiguration cfg, SchedulerState state,
                            NMProfileManager profileManager, MyriadOperations myriadOperations) {
        this.cfg = cfg;
        this.schedulerState = state;
        this.profileManager = profileManager;
        this.myriadOperations = myriadOperations;
    }

    @Timed
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GetClustersResponse getClusters() {
        GetClustersResponse getClustersResponse = new GetClustersResponse(
                this.schedulerState.getClusters());

        return getClustersResponse;
    }

    @Timed
    @Path("/{clusterId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public GetClusterResponse getCluster(
            @PathParam("clusterId") String clusterId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterId),
                "clusterId cannot be null or empty.");
        return new GetClusterResponse(this.schedulerState.getCluster(clusterId));
    }

    @Timed
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerCluster(RegisterClusterRequest request) {
        Preconditions.checkNotNull(request,
                "RegisterClusterRequest object cannot be null");
        Cluster cluster = new Cluster();
        cluster.setClusterName(request.getClusterName());
        cluster.setResourceManagerHost(request.getResourceManagerHost());
        cluster.setResourceManagerPort(request.getResourceManagerPort());
        this.schedulerState.addCluster(cluster);
        return Response.ok()
                .entity(new RegisterClusterResponse(cluster.getClusterId()))
                .build();
    }

    @Timed
    @Path("/{clusterId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response unregisterCluster(@PathParam("clusterId") String clusterId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterId),
                "clusterId cannot be null or empty.");

        this.schedulerState.deleteCluster(clusterId);
        return Response.ok().build();
    }

    @Timed
    @PUT
    @Path("/{clusterId}/flexup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response flexUp(@PathParam("clusterId") String clusterId,
                           FlexUpClusterRequest request) {
        Preconditions.checkNotNull(request,
                "request object cannot be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterId),
                "clusterId cannot be null or empty");

        Cluster cluster = this.schedulerState.getCluster(clusterId);
        if (cluster == null) {
            LOGGER.error("No cluster found with clusterId: " + clusterId);
            return Response.status(Status.NOT_FOUND).build();
        }

        // TODO(mohit): Validation
        Integer instances = request.getInstances();
        String profile = request.getProfile();
        this.myriadOperations.flexUpCluster(clusterId, instances, profile);
        return Response.ok().build();
    }

    @Timed
    @PUT
    @Path("/{clusterId}/flexdown")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response flexDown(@PathParam("clusterId") String clusterId,
                             FlexDownClusterRequest request) {
        Preconditions.checkNotNull(request,
                "request object cannot be null or empty");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterId),
                "clusterId cannot be null or empty");

        Cluster cluster = this.schedulerState.getCluster(clusterId);
        if (cluster == null) {
            LOGGER.error("No cluster found with clusterId: " + clusterId);
            return Response.status(Status.NOT_FOUND).build();
        }

        // TODO(mohit): Make safer.
        this.myriadOperations.flexDownCluster(cluster, request.getInstances());
        return Response.ok().build();
    }

}
