package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.Main;
import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.*;

import java.io.IOException;
import java.util.List;

/**
 * Bridge between Myriad and YARN, so to speak. Allows Myriad to receive
 * call backs for Yarn schedulers (like Fifo/Fair/Capacity) via {@link YarnSchedulerInterceptor}.
 * <p/>
 * Instances of this class are created by {@link MyriadFairScheduler}, {@link MyriadCapacityScheduler}
 * and {@link MyriadFifoScheduler}.
 */
public class MyriadYarnSchedulerInterceptor implements YarnSchedulerInterceptor {

    private AbstractYarnScheduler yarnScheduler;
    private List<EventListener> listeners = Lists.newArrayList();

    @Override
    public AbstractYarnScheduler registerEventListener(EventListener listener) {
        listeners.add(listener);
        return yarnScheduler;
    }

    /**
     * Initialize Myriad plugin before RM's scheduler is initialized.
     * This includes registration with Mesos master and initialization of
     * the myriad web application.
     */
    @Override
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler) throws IOException {
        try {
            Main.initialize(conf, this);
        } catch (Exception e) {
            // Abort bringing up RM
            throw new RuntimeException("Failed to initialize myriad", e);
        }
        this.yarnScheduler = yarnScheduler;
    }

    @Override
    public void onEventHandled(SchedulerEvent event) {
        switch (event.getType()) {
            case NODE_UPDATE: {
                for (EventListener listener : listeners) {
                    listener.onNodeUpdated((NodeUpdateSchedulerEvent) event);
                }
            }
            break;
            case NODE_REMOVED: {
                for (EventListener listener : listeners) {
                    listener.onNodeRemoved((NodeRemovedSchedulerEvent) event);
                }
            }
        }

    }
}
