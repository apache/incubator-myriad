package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.YarnScheduler;

import java.io.IOException;

/**
 * Allows interception of the ResourceManager's calls to Yarn Schedulers.
 */
public interface YarnSchedulerPlugin {

    /**
     * Called before {@link AbstractYarnScheduler#reinitialize(Configuration, RMContext)}
     * Initializes the myriad plugin.
     *
     * @param conf
     * @param yarnScheduler
     * @throws IOException
     */
    public void init(Configuration conf, YarnScheduler yarnScheduler) throws IOException;

}
