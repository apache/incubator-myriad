package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler;

import java.io.IOException;

/**
 * {@link MyriadCapacityScheduler} just extends YARN's {@link CapacityScheduler} and
 * allows some of the {@link CapacityScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadCapacityScheduler extends CapacityScheduler {
    private final YarnSchedulerInterceptor interceptor;

    public MyriadCapacityScheduler() {
        super();
        this.interceptor = new MyriadYarnSchedulerInterceptor();
    }

    /**
     * ******** Methods overridden from YARN {@link CapacityScheduler}  *********************
     */

    @Override
    public void reinitialize(Configuration conf, RMContext rmContext) throws IOException {
        this.interceptor.beforeReinitialize(conf, rmContext);
        super.reinitialize(conf, rmContext);
    }
}

