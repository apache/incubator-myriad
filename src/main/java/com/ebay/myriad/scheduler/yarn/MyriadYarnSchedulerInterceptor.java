package com.ebay.myriad.scheduler.yarn;

import com.ebay.myriad.Main;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.server.resourcemanager.RMContext;

import java.io.IOException;

/**
 * Bridge between Myriad and YARN, so to speak. Allows Myriad to receive
 * call backs for Yarn schedulers (like Fifo/Fair/Capacity) via {@link YarnSchedulerInterceptor}.
 *
 * Instances of this class are created by {@link MyriadFairScheduler}, {@link MyriadCapacityScheduler}
 * and {@link MyriadFifoScheduler}.
 */
public class MyriadYarnSchedulerInterceptor implements YarnSchedulerInterceptor {

  /**
   * Initialize Myriad plugin before RM's scheduler is initialized.
   * This includes registration with Mesos master and initialization of
   * the myriad web application.
   */
  @Override
  public void beforeReinitialize(Configuration conf, RMContext rmContext) throws IOException {
    try {
      Main.initialize();
    } catch (Exception e) {
      // Abort bringing up RM
      throw new RuntimeException("Failed to initialize myriad", e);
    }
  }
}
