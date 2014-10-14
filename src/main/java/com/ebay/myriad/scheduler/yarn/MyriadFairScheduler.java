package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler;

import java.io.IOException;

/**
 * {@link MyriadFairScheduler} just extends YARN's {@link FairScheduler} and
 * allows some of the {@link FairScheduler} methods to be intercepted
 * via the {@link YarnSchedulerPlugin} interface.
 */
public class MyriadFairScheduler extends FairScheduler {
    private final YarnSchedulerPlugin yarnSchedulerPlugin;

    public MyriadFairScheduler() {
        super();
        this.yarnSchedulerPlugin = new MyriadYarnSchedulerPlugin();
    }

    /**
     * ******** Methods overridden from YARN {@link FairScheduler}  *********************
     */

    @Override
    public void reinitialize(Configuration conf, RMContext rmContext) throws IOException {
        this.yarnSchedulerPlugin.init(conf, this);
        super.reinitialize(conf, rmContext);
    }

    @Override
    public void serviceInit(Configuration conf) throws Exception {
        this.yarnSchedulerPlugin.init(conf, this);
        super.serviceInit(conf);
    }
}

