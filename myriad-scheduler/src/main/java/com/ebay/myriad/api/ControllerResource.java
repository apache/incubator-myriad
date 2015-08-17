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
import com.ebay.myriad.scheduler.MyriadOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RESTful API to resource manager
 */
@Path("/framework")
public class ControllerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerResource.class);

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
    @GET
    @Path("/shutdown/framework")
    @Produces(MediaType.APPLICATION_JSON)
    public Response shutdownFramework() {
        LOGGER.info("shutdown....terminating framework... ");
         
        myriadOperations.shutdownFramework();
        
        return Response.ok().build();
    }  
    
   
}
