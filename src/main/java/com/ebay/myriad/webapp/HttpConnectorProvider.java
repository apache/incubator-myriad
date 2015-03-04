package com.ebay.myriad.webapp;

import com.google.inject.Provider;
import org.apache.hadoop.conf.Configuration;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.nio.SelectChannelConnector;

import javax.inject.Inject;

/**
 * The factory for creating the http connector for the myriad scheduler
 */
public class HttpConnectorProvider implements Provider<Connector> {

    private final Configuration hadoopConf;

    @Inject
    public HttpConnectorProvider(Configuration hadoopConf) {
        this.hadoopConf = hadoopConf;
    }

    @Override
    public Connector get() {
        SelectChannelConnector ret = new SelectChannelConnector();
        ret.setHost("0.0.0.0");
        //TODO (Santosh): get the port from yarn-site.xml via hadoopConf
        ret.setPort(8192);

        return ret;
    }
}
