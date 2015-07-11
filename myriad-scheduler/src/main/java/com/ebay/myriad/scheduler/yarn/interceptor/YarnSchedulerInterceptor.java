package com.ebay.myriad.scheduler.yarn.interceptor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeImpl;
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
     * @param rmContext
     * @throws IOException
     */
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler, RMContext rmContext) throws IOException;

    /**
     * Invoked *before* {@link RMNodeImpl#handle(RMNodeEvent)}.
     *
     * @param event
     * @param context
     */
    public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context);

    /**
     * Invoked *before* {@link YarnScheduler#handle(org.apache.hadoop.yarn.event.Event)}
     * @param event
     */
    public void beforeSchedulerEventHandled(SchedulerEvent event);

    /**
     * Invoked *after* {@link YarnScheduler#handle(org.apache.hadoop.yarn.event.Event)}
     *
     * @param event
     */
    public void afterSchedulerEventHandled(SchedulerEvent event);

}
