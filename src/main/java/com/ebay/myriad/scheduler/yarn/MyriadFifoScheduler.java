package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;

import java.io.IOException;

/**
 * {@link MyriadFifoScheduler} just extends YARN's {@link FifoScheduler} and
 * allows some of the {@link FifoScheduler} methods to be intercepted
 * via the {@link YarnSchedulerPlugin} interface.
 */
public class MyriadFifoScheduler extends FifoScheduler {
    private final YarnSchedulerPlugin yarnSchedulerPlugin;

    public MyriadFifoScheduler() {
        super();
        this.yarnSchedulerPlugin = new MyriadYarnSchedulerPlugin();
    }

    /**
     * ******** Methods overridden from YARN {@link FifoScheduler}  *********************
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

