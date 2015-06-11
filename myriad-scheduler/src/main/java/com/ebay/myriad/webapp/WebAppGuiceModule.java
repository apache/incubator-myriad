package com.ebay.myriad.webapp;

import com.google.inject.AbstractModule;
import org.mortbay.jetty.Connector;

/**
 * The guice web application configuration
 */
public class WebAppGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Connector.class).toProvider(HttpConnectorProvider.class);
        install(new MyriadServletModule());
    }
}
