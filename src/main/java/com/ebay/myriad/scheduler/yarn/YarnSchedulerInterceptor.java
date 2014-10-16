package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.event.Event;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.AbstractYarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.YarnScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.*;

import java.io.IOException;

/**
 * Intercepts the YARN's scheduler events (or methods) and exposes the events via {@link EventListener} interface.
 */
public interface YarnSchedulerInterceptor {

    /**
     * Listener interface that exposes YARN's scheduler events.
     */
    public interface EventListener {

        /**
         * Invoked *after* Yarn's scheduler handles a HB from a Node Manager.
         * @param event
         *
         */
        public void onNodeUpdated(NodeUpdateSchedulerEvent event);
    }

    /**
     * Allows event listeners to be registered. In addition, returns a reference to the YarnScheduler
     * to the event listener so that listeners can query more information from the YarnScheduler.
     * This method needs to be called only after {@link #init(Configuration, AbstractYarnScheduler)}
     * is called on this interface.
     * @param listener
     */
    public AbstractYarnScheduler registerEventListener(EventListener listener);


    /************** Methods invoked before/after intercepting YARN's scheduler events/method calls **************/

    /**
     * Invoked *before* {@link AbstractYarnScheduler#reinitialize(Configuration, RMContext)}
     * Initializes the myriad plugin. This should be the first method to be called on this interface.
     *
     * @param conf
     * @param yarnScheduler
     * @throws IOException
     */
    public void init(Configuration conf, AbstractYarnScheduler yarnScheduler) throws IOException;

    /**
     * Invoked *after* {@link YarnScheduler#handle(Event)}
     * @param event
     */
    public void onEventHandled(SchedulerEvent event);

}
