package com.ebay.myriad.scheduler.yarn.interceptor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.NodeId;
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
     * Filters the method callbacks.
     */
    interface CallBackFilter {
      /**
       * Method to determine if any other methods in {@link YarnSchedulerInterceptor}
       * pertaining to a given node manager should be invoked or not.
       *
       * @param nodeManager NodeId of the Node Manager registered with RM.
       * @return true to allow invoking further interceptor methods. false otherwise.
       */
      public boolean allowCallBacksForNode(NodeId nodeManager);
    }

    /**
     * Return an instance of {@link CallBackFilter}. {@link CallBackFilter#allowCallBacksForNode(NodeId)}
     * method is invoked to *determine* if any of the other methods pertaining to a specific node
     * needs to be invoked or not.
     *
     * @return
     */
    public CallBackFilter getCallBackFilter();

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
     * Invoked *before* {@link RMNodeImpl#handle(RMNodeEvent)} only if
     * {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
     *
     * @param event
     * @param context
     */
    public void beforeRMNodeEventHandled(RMNodeEvent event, RMContext context);

    /**
     * Invoked *before* {@link YarnScheduler#handle(org.apache.hadoop.yarn.event.Event)} only if
     * {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
     *
     * @param event
     */
    public void beforeSchedulerEventHandled(SchedulerEvent event);

    /**
     * Invoked *after* {@link YarnScheduler#handle(org.apache.hadoop.yarn.event.Event)} only if
     * {@link CallBackFilter#allowCallBacksForNode(NodeId)} returns true.
     *
     * @param event
     */
    public void afterSchedulerEventHandled(SchedulerEvent event);

}
