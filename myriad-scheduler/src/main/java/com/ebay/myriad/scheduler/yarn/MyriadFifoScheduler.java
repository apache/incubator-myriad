package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.scheduler.yarn.interceptor.CompositeInterceptor;
import com.ebay.myriad.scheduler.yarn.interceptor.YarnSchedulerInterceptor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEvent;
import org.apache.hadoop.yarn.server.resourcemanager.rmnode.RMNodeEventType;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.event.SchedulerEvent;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;

/**
 * {@link MyriadFifoScheduler} just extends YARN's {@link FifoScheduler} and
 * allows some of the {@link FifoScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFifoScheduler extends FifoScheduler {

  private RMContext rmContext;
  private YarnSchedulerInterceptor yarnSchedulerInterceptor;
  private RMNodeEventHandler rmNodeEventHandler;

  public MyriadFifoScheduler() {
    super();
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
    this.rmContext = rmContext;
    this.yarnSchedulerInterceptor = new CompositeInterceptor();
    rmNodeEventHandler = new RMNodeEventHandler(yarnSchedulerInterceptor, rmContext);
    rmContext.getDispatcher().register(RMNodeEventType.class, rmNodeEventHandler);
    super.setRMContext(rmContext);
  }

  /**
   * ******** Methods overridden from YARN {@link FifoScheduler}  *********************
   */

  @Override
  public synchronized void serviceInit(Configuration conf) throws Exception {
    this.yarnSchedulerInterceptor.init(conf, this, rmContext);
    super.serviceInit(conf);
  }

  @Override
  public synchronized void handle(SchedulerEvent event) {
    this.yarnSchedulerInterceptor.beforeSchedulerEventHandled(event);
    super.handle(event);
    this.yarnSchedulerInterceptor.afterSchedulerEventHandled(event);
  }
}

