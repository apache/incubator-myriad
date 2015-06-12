package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;
import org.apache.hadoop.yarn.event.EventHandler;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;

/**
 * Passes the {@link RMNodeEvent} events into the {@link YarnSchedulerInterceptor}.
 */
public class RMNodeEventHandler implements EventHandler<RMNodeEvent> {
    private final YarnSchedulerInterceptor interceptor;
    private final RMContext rmContext;

    public RMNodeEventHandler(YarnSchedulerInterceptor interceptor, RMContext rmContext) {
        this.interceptor = interceptor;
        this.rmContext = rmContext;
    }

    @Override
    public void handle(RMNodeEvent event) {
        interceptor.beforeRMNodeEventHandled(event, rmContext);

    }
}
