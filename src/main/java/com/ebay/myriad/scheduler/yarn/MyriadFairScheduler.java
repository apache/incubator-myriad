package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler;

import java.io.IOException;

/**
 * {@link MyriadFairScheduler} just extends YARN's {@link FairScheduler} and
 * allows some of the {@link FairScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFairScheduler extends FairScheduler {
    private final YarnSchedulerInterceptor interceptor;

    public MyriadFairScheduler() {
        super();
        this.interceptor = new MyriadYarnSchedulerInterceptor();
    }

    /**
     * ******** Methods overridden from YARN {@link FairScheduler}  *********************
     */

    @Override
    public void reinitialize(Configuration conf, RMContext rmContext) throws IOException {
        this.interceptor.beforeReinitialize(conf, rmContext);
        super.reinitialize(conf, rmContext);
    }

    @Override
    public void serviceInit(Configuration conf) throws Exception {
        this.reinitialize(conf, null);
        super.serviceInit(conf);
    }
}

