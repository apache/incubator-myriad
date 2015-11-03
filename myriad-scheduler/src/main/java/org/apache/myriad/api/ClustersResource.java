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
package org.apache.myriad.api;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.apache.myriad.api.model.FlexDownClusterRequest;
import org.apache.myriad.api.model.FlexDownServiceRequest;
import org.apache.myriad.api.model.FlexUpClusterRequest;
import org.apache.myriad.api.model.FlexUpServiceRequest;
import org.apache.myriad.configuration.MyriadBadConfigurationException;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.scheduler.MyriadOperations;
import org.apache.myriad.scheduler.ServiceProfileManager;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.constraints.ConstraintFactory;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTful API to resource manager
 */
@Path("/cluster")
public class ClustersResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClustersResource.class);
  private static final String LIKE_CONSTRAINT_FORMAT = "'<mesos_slave_attribute|hostname> LIKE <value_regex>'";

  private MyriadConfiguration cfg;
  private SchedulerState schedulerState;
  private ServiceProfileManager profileManager;
  private MyriadOperations myriadOperations;

  @Inject
  public ClustersResource(MyriadConfiguration cfg, SchedulerState state, ServiceProfileManager profileManager,
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
    Preconditions.checkNotNull(request, "request object cannot be null or empty");

    Integer instances = request.getInstances();
    String profile = request.getProfile();
    List<String> constraints = request.getConstraints();
    LOGGER.info("Received flexup request. Profile: {}, Instances: {}, Constraints: {}", profile, instances, constraints);

    Response.ResponseBuilder response = Response.status(Response.Status.ACCEPTED);
    boolean isValidRequest = validateProfile(profile, response);
    isValidRequest = isValidRequest && validateInstances(instances, response);
    isValidRequest = isValidRequest && validateConstraints(constraints, response);

    Response returnResponse = response.build();
    if (returnResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
      String constraint = constraints != null && !constraints.isEmpty() ? constraints.get(0) : null;
      this.myriadOperations.flexUpCluster(this.profileManager.get(profile), instances, ConstraintFactory.createConstraint(
          constraint));
    }

    return returnResponse;
  }

  @Timed
  @PUT
  @Path("/flexupservice")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response flexUpservice(FlexUpServiceRequest request) {
    Preconditions.checkNotNull(request, "request object cannot be null or empty");

    LOGGER.info("Received Flexup a Service Request");

    Integer instances = request.getInstances();
    String serviceName = request.getServiceName();

    LOGGER.info("Instances: {}", instances);
    LOGGER.info("Service: {}", serviceName);

    // Validate profile request
    Response.ResponseBuilder response = Response.status(Response.Status.ACCEPTED);

    if (cfg.getServiceConfiguration(serviceName) == null) {
      response.status(Response.Status.BAD_REQUEST).entity("Service does not exist: " + serviceName);
      LOGGER.error("Provided service does not exist " + serviceName);
      return response.build();
    }

    if (!validateInstances(instances, response)) {
      return response.build();
    }

    try {
      this.myriadOperations.flexUpAService(instances, serviceName);
    } catch (MyriadBadConfigurationException e) {
      return response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }

    return response.build();
  }

  @Timed
  @PUT
  @Path("/flexdown")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response flexDown(FlexDownClusterRequest request) {
    Preconditions.checkNotNull(request, "request object cannot be null or empty");

    Integer instances = request.getInstances();
    String profile = request.getProfile();
    List<String> constraints = request.getConstraints();
    LOGGER.info("Received flex down request. Profile: {}, Instances: {}, Constraints: {}", profile, instances, constraints);

    Response.ResponseBuilder response = Response.status(Response.Status.ACCEPTED);
    boolean isValidRequest = validateProfile(profile, response);
    isValidRequest = isValidRequest && validateInstances(instances, response);
    isValidRequest = isValidRequest && validateConstraints(constraints, response);

    if (isValidRequest) {
      Integer numFlexedUp = this.getNumFlexedupNMs(profile);
      if (numFlexedUp < instances) {
        String message = String.format("Number of requested instances for flexdown is greater than the number of " +
            "Node Managers previously flexed up for profile '%s'. Requested: %d, Previously flexed Up: %d. " +
            "Only %d Node Managers will be flexed down.", profile, instances, numFlexedUp, numFlexedUp);
        response.entity(message);
        LOGGER.warn(message);
      }
    }

    Response returnResponse = response.build();
    if (returnResponse.getStatus() == Response.Status.ACCEPTED.getStatusCode()) {
      String constraint = constraints != null && !constraints.isEmpty() ? constraints.get(0) : null;
      this.myriadOperations.flexDownCluster(profileManager.get(profile), ConstraintFactory.createConstraint(constraint), instances);
    }
    return returnResponse;
  }

  private boolean validateProfile(String profile, ResponseBuilder response) {
    if (profile == null || profile.isEmpty()) {
      response.status(Response.Status.BAD_REQUEST).entity("'profile' is null or empty");
      LOGGER.error("'profile' is null or empty");
      return false;
    }
    if (!this.profileManager.exists(profile)) {
      response.status(Response.Status.BAD_REQUEST).entity("Profile does not exist: '" + profile + "'");
      LOGGER.error("Provided profile does not exist: '" + profile + "'");
      return false;
    }
    return true;
  }

  private boolean validateInstances(Integer instances, ResponseBuilder response) {
    if (instances == null) {
      response.status(Response.Status.BAD_REQUEST).entity("'instances' is null");
      LOGGER.error("'instances' is null");
      return false;
    } else if (!(instances > 0)) {
      response.status(Response.Status.BAD_REQUEST).entity("Invalid instance size: " + instances);
      LOGGER.error("Invalid instance size request " + instances);
      return false;
    }
    return true;
  }

  private boolean validateConstraints(List<String> constraints, ResponseBuilder response) {
    if (constraints != null && !constraints.isEmpty()) {
      boolean valid = validateConstraintsSize(constraints, response);
      valid = valid && validateLIKEConstraint(constraints.get(0), response);
      return valid;
    }
    return true;
  }

  private boolean validateConstraintsSize(List<String> constraints, ResponseBuilder response) {
    if (constraints.size() > 1) {
      String message = String.format("Only 1 constraint is currently supported. Received: %s", constraints.toString());
      response.status(Status.BAD_REQUEST).entity(message);
      LOGGER.error(message);
      return false;
    }
    return true;
  }

  private boolean validateLIKEConstraint(String constraint, ResponseBuilder response) {
    if (constraint.isEmpty()) {
      String message = String.format("The value provided for 'constraints' is empty. Format: %s", LIKE_CONSTRAINT_FORMAT);
      response.status(Status.BAD_REQUEST).entity(message);
      LOGGER.error(message);
      return false;
    }

    String[] splits = constraint.split(" LIKE "); // "<key> LIKE <val_regex>"
    if (splits.length != 2) {
      String message = String.format("Invalid format for LIKE operator in constraint: %s. Format: %s", constraint,
          LIKE_CONSTRAINT_FORMAT);
      response.status(Status.BAD_REQUEST).entity(message);
      LOGGER.error(message);
      return false;
    }
    try {
      Pattern.compile(splits[1]);
    } catch (PatternSyntaxException e) {
      String message = String.format("Invalid regex for LIKE operator in constraint: %s", constraint);
      response.status(Status.BAD_REQUEST).entity(message);
      LOGGER.error(message, e);
      return false;
    }
    return true;
  }


  private Integer getNumFlexedupNMs(String profile) {
    ServiceResourceProfile serviceProfile = profileManager.get(profile);
    return this.schedulerState.getActiveTaskIDsForProfile(serviceProfile).size() + this.schedulerState.getStagingTaskIDsForProfile(
        serviceProfile).size() + this.schedulerState.getPendingTaskIDsForProfile(serviceProfile).size();
  }

  @Timed
  @PUT
  @Path("/flexdownservice")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response flexDownservice(FlexDownServiceRequest request) {
    Preconditions.checkNotNull(request, "request object cannot be null or empty");

    Integer instances = request.getInstances();
    String serviceName = request.getServiceName();

    LOGGER.info("Received flexdown request for service {}", serviceName);
    LOGGER.info("Instances: " + instances);

    Response.ResponseBuilder response = Response.status(Response.Status.ACCEPTED);

    if (!validateInstances(instances, response)) {
      return response.build();
    }

    // warn that number of requested instances isn't available
    // but instances will still be flexed down
    Integer flexibleInstances = this.myriadOperations.getFlexibleInstances(serviceName);
    if (flexibleInstances < instances) {
      response.entity("Number of requested instances is greater than available.");
      // just doing a simple check here. pass the requested number of instances
      // to myriadOperations and let it sort out how many actually get flexxed down.
      LOGGER.warn("Requested number of instances greater than available: {} < {}", flexibleInstances, instances);
    }

    this.myriadOperations.flexDownAService(instances, serviceName);
    return response.build();
  }
}
