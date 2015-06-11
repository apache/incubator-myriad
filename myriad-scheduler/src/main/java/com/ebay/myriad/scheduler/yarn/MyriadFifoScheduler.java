package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;

import java.io.IOException;

/**
 * {@link MyriadFifoScheduler} just extends YARN's {@link FifoScheduler} and
 * allows some of the {@link FifoScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFifoScheduler extends FifoScheduler {
    private final YarnSchedulerInterceptor yarnSchedulerInterceptor;

    public MyriadFifoScheduler() {
        super();
        this.yarnSchedulerInterceptor = new CompositeInterceptor();
    }

    /**
     * ******** Methods overridden from YARN {@link FifoScheduler}  *********************
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

