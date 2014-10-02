package com.ebay.myriad.scheduler.yarn;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;

import java.io.IOException;

/**
 * {@link MyriadFifoScheduler} just extends YARN's {@link FifoScheduler} and
 * allows some of the {@link FifoScheduler} methods to be intercepted
 * via the {@link YarnSchedulerInterceptor} interface.
 */
public class MyriadFifoScheduler extends FifoScheduler {
  private final YarnSchedulerInterceptor interceptor;

  public MyriadFifoScheduler() {
    super();
    this.interceptor = new MyriadYarnSchedulerInterceptor();
  }

  /*********** Methods overridden from YARN {@link FifoScheduler}  **********************/

  @Override
  public void reinitialize(Configuration conf, RMContext rmContext) throws IOException {
    this.interceptor.beforeReinitialize(conf, rmContext);
    super.reinitialize(conf, rmContext);
  }
}

