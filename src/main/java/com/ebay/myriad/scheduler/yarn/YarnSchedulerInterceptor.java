package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;

import java.io.IOException;

/**
 * Allows interception of the ResourceManager's calls to Yarn Schedulers.
 */
public interface YarnSchedulerInterceptor {

    /**
     * Called before {@link AbstractYarnScheduler#reinitialize(Configuration, RMContext)}
     *
     * @param conf
     * @param rmContext
     * @throws IOException
     */
    public void beforeReinitialize(Configuration conf, RMContext rmContext) throws IOException;

}
