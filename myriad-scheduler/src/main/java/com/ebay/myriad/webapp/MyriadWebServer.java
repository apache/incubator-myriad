package com.ebay.myriad.webapp;

import com.google.inject.servlet.GuiceFilter;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.*;

import javax.inject.Inject;

/**
 * The myriad web server configuration for jetty
 */
public class MyriadWebServer {
    private final Server jetty;
    private final Connector connector;
    private final GuiceFilter filter;

    @Inject
    public MyriadWebServer(Server jetty, Connector connector, GuiceFilter filter) {
        this.jetty = jetty;
        this.connector = connector;
        this.filter = filter;
    }

    public void start() throws Exception {
        this.jetty.addConnector(connector);

        ServletHandler servletHandler = new ServletHandler();

        String filterName = "MyriadGuiceFilter";
        FilterHolder holder = new FilterHolder(filter);
        holder.setName(filterName);

        FilterMapping filterMapping = new FilterMapping();
        filterMapping.setPathSpec("/*");
        filterMapping.setDispatches(Handler.ALL);
        filterMapping.setFilterName(filterName);

        servletHandler.addFilter(holder, filterMapping);

        Context context = new Context();
        context.setServletHandler(servletHandler);
        context.addServlet(DefaultServlet.class, "/");

        String staticDir = this.getClass().getClassLoader().getResource("webapp/public").toExternalForm();
        context.setResourceBase(staticDir);

        this.jetty.addHandler(context);
        this.jetty.start();
    }



}
