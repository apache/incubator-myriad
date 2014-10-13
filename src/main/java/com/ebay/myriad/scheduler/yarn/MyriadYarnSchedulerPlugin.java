package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.Main;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.YarnScheduler;

import java.io.IOException;

/**
 * Bridge between Myriad and YARN, so to speak. Allows Myriad to receive
 * call backs for Yarn schedulers (like Fifo/Fair/Capacity) via {@link YarnSchedulerPlugin}.
 * <p/>
 * Instances of this class are created by {@link MyriadFairScheduler}, {@link MyriadCapacityScheduler}
 * and {@link MyriadFifoScheduler}.
 */
public class MyriadYarnSchedulerPlugin implements YarnSchedulerPlugin {

    private YarnScheduler yarnScheduler;

    /**
     * Initialize Myriad plugin before RM's scheduler is initialized.
     * This includes registration with Mesos master and initialization of
     * the myriad web application.
     */
    @Override
    public void init(Configuration conf, YarnScheduler yarnScheduler) throws IOException {
        try {
            Main.initialize(conf);
        } catch (Exception e) {
            // Abort bringing up RM
            throw new RuntimeException("Failed to initialize myriad", e);
        }
        this.yarnScheduler = yarnScheduler;
    }
}
