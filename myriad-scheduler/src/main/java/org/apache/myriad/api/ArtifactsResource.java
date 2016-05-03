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
import org.apache.myriad.configuration.MyriadConfiguration;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Defines the REST API to the Myriad configuration.
 */
@Path("/artifacts")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class ArtifactsResource {
  private String myriadEtc;
  private String myriadBinary;

  @Inject
  public ArtifactsResource(MyriadConfiguration cfg) {
    myriadEtc = cfg.getServedConfigPath().or("");
    myriadBinary = cfg.getServedBinaryPath().or("");
  }

  @Timed
  @GET
  @Path("/config.tgz")
  public Response getConfig() throws InterruptedException {
    File file = new File(myriadEtc);
    Response.ResponseBuilder response;
    if (file.exists()) {
      response = Response.ok((Object) file);
      response.header("Content-Disposition", "attachment; filename=" + file.getName());
    } else {
      response = Response.status(Response.Status.BAD_REQUEST)
          .entity("Path does not exist");
    }
    return response.build();
  }

  @Timed
  @GET
  @Path("/binary.tgz")
  public Response getBinary() throws InterruptedException {
    File file = new File(myriadBinary);
    Response.ResponseBuilder response;
    if (file.exists()) {
      response = Response.ok((Object) file);
      response.header("Content-Disposition", "attachment; filename=" + file.getName());
    } else {
      response = Response.status(Response.Status.BAD_REQUEST)
          .entity("Path does not exist");
    }
    return response.build();
  }
}