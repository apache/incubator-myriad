package com.ebay.myriad.webapp;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.google.inject.Provider;
import org.apache.hadoop.conf.Configuration;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.nio.SelectChannelConnector;

import javax.inject.Inject;

public class HttpConnectorProvider implements Provider<Connector> {

    private MyriadConfiguration myriadConf;

    @Inject
    public HttpConnectorProvider(MyriadConfiguration myriadConf) {
        this.myriadConf = myriadConf;
    }

    @Override
    public Connector get() {
        SelectChannelConnector ret = new SelectChannelConnector();
        ret.setHost("0.0.0.0");
        ret.setPort( myriadConf.getRestApiPort() );

        return ret;
    }
}
