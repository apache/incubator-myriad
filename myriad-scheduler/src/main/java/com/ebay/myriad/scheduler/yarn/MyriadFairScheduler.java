package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler;

import java.io.IOException;

/**
 * {@link MyriadFairScheduler} just extends YARN's {@link FairScheduler} and
 * allows some of the {@link FairScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFairScheduler extends FairScheduler {
    private final YarnSchedulerInterceptor yarnSchedulerInterceptor;

    public MyriadFairScheduler() {
        super();
        this.yarnSchedulerInterceptor = new CompositeInterceptor();
    }

    /**
     * ******** Methods overridden from YARN {@link FairScheduler}  *********************
     */

    @Override
    public void reinitialize(Configuration conf, RMContext rmContext) throws IOException {
        this.yarnSchedulerInterceptor.init(conf, this);
        super.reinitialize(conf, rmContext);
    }

    @Override
    public void serviceInit(Configuration conf) throws Exception {
        this.yarnSchedulerInterceptor.init(conf, this);
        super.serviceInit(conf);
    }

    @Override
    public void handle(SchedulerEvent event) {
        super.handle(event);
        this.yarnSchedulerInterceptor.onEventHandled(event);
    }
}

