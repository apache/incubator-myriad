package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler;

import java.io.IOException;

/**
 * {@link MyriadCapacityScheduler} just extends YARN's {@link CapacityScheduler} and
 * allows some of the {@link CapacityScheduler} methods to be intercepted
 * via the {@link YarnSchedulerPlugin} interface.
 */
public class MyriadCapacityScheduler extends CapacityScheduler {
    private final YarnSchedulerPlugin yarnSchedulerPlugin;

    public MyriadCapacityScheduler() {
        super();
        this.yarnSchedulerPlugin = new MyriadYarnSchedulerPlugin();
    }

    /**
     * ******** Methods overridden from YARN {@link CapacityScheduler}  *********************
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

