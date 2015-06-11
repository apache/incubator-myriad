package com.ebay.myriad.scheduler.yarn.interceptor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.YarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;

import java.io.IOException;

/**
 * Allows interception of YARN's scheduler events (or methods).
 */
public interface YarnSchedulerInterceptor {

    /**
     * Invoked *before* {@link AbstractYarnScheduler#reinitialize(Configuration, RMContext)}
     *
     * @param conf
     * @param yarnScheduler
     * @throws IOException
     */
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler) throws IOException;

    /**
     * Invoked *after* {@link YarnScheduler#handle(Event)}
     *
     * @param event
     */
    public void onEventHandled(SchedulerEvent event);

}
