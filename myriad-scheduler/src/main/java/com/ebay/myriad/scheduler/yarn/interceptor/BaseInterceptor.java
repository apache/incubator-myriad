package com.ebay.myriad.scheduler.yarn.interceptor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;

import java.io.IOException;

/**
 * A no-op interceptor whose sole purpose is to serve as a base class
 * for other interceptors. Child interceptors can selectively override the
 * required methods.
 */
public class BaseInterceptor implements YarnSchedulerInterceptor {
    // restrict the constructor
    protected BaseInterceptor() {
    }

    @Override
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler) throws IOException {
    }

    @Override
    public void onEventHandled(SchedulerEvent event) {

    }
}
