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
import org.apache.myriad.scheduler.MyriadOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RESTful API to resource manager
 */
@Path("/framework")
public class ControllerResource {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private MyriadOperations myriadOperations;

  @Inject
  public ControllerResource(MyriadOperations myriadOperations) {
    this.myriadOperations = myriadOperations;
  }

  /**
   * Shutdown framework means the Mesos driver is stopped, all executors and tasks as well.
   *
   * @return a successful response.
   */
  @Timed
  @POST
  @Path("/shutdown/framework")
  @Produces(MediaType.APPLICATION_JSON)
  public Response shutdownFramework() {
    logger.info("shutdown....terminating framework... ");

    myriadOperations.shutdownFramework();

    return Response.ok().build();
  }
}
