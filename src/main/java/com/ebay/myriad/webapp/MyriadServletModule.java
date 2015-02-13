package com.ebay.myriad.webapp;

import com.ebay.myriad.api.ClustersResource;
import com.ebay.myriad.api.ConfigurationResource;
import com.ebay.myriad.api.SchedulerStateResource;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
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

        bind(GuiceContainer.class);
        bind(JacksonJaxbJsonProvider.class).in(Scopes.SINGLETON);

        serve("/api/*").with(GuiceContainer.class);
    }
}
