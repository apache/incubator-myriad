package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEventType;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fair.FairScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * {@link MyriadFairScheduler} just extends YARN's {@link FairScheduler} and
 * allows some of the {@link FairScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFairScheduler extends FairScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadFairScheduler.class);

    private final YarnSchedulerInterceptor yarnSchedulerInterceptor;
    private RMNodeEventHandler rmNodeEventHandler;

    public MyriadFairScheduler() {
        super();
        this.yarnSchedulerInterceptor = new CompositeInterceptor();
    }

    /**
     * Register an event handler that receives {@link RMNodeEvent} events.
     * This event handler is registered ahead of RM's own event handler for RMNodeEvents.
     * For e.g. myriad can inspect a node's HB (RMNodeStatusEvent) before the HB is handled by
     * RM and the scheduler.
     *
     * @param rmContext
     */
    @Override
    public synchronized void setRMContext(RMContext rmContext) {
        rmNodeEventHandler = new RMNodeEventHandler(yarnSchedulerInterceptor, rmContext);
        rmContext.getDispatcher().register(RMNodeEventType.class, rmNodeEventHandler);
        super.setRMContext(rmContext);
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
        this.yarnSchedulerInterceptor.beforeSchedulerEventHandled(event);
        super.handle(event);
        this.yarnSchedulerInterceptor.afterSchedulerEventHandled(event);
    }
}

