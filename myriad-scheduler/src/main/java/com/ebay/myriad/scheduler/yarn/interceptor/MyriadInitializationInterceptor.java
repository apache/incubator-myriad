package com.ebay.myriad.scheduler.yarn.interceptor;

import com.ebay.myriad.Main;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Responsible for intializing myriad.
 */
public class MyriadInitializationInterceptor extends BaseInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadInitializationInterceptor.class);

    private final InterceptorRegistry registry;

    public MyriadInitializationInterceptor(InterceptorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Initialize Myriad plugin before RM's scheduler is initialized.
     * This includes registration with Mesos master, initialization of
     * the myriad web application, initializing guice modules etc.
     */
    @Override
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler) throws IOException {
        try {
            Main.initialize(conf, yarnScheduler, registry);
        } catch (Exception e) {
            // Abort bringing up RM
            throw new RuntimeException("Failed to initialize myriad", e);
        }
        LOGGER.info("Initialized myriad.");
    }
}
