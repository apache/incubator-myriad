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
package org.apache.myriad.webapp;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.apache.myriad.api.ClustersResource;
import org.apache.myriad.api.ConfigurationResource;
import org.apache.myriad.api.ControllerResource;
import org.apache.myriad.api.SchedulerStateResource;
import org.apache.myriad.api.ArtifactsResource;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

/**
 * The guice module for configuring the myriad dashboard
 */
public class MyriadServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(ClustersResource.class);
    bind(ConfigurationResource.class);
    bind(SchedulerStateResource.class);
    bind(ControllerResource.class);
    bind(ArtifactsResource.class);
    bind(GuiceContainer.class);
    bind(JacksonJaxbJsonProvider.class).in(Scopes.SINGLETON);

    serve("/api/*").with(GuiceContainer.class);
  }
}
