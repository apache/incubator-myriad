/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.scheduler;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.state.SchedulerState;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * {@link Rebalancer} is responsible for scaling registered YARN clusters as per
 * configured rules and policies.
 */
public class Rebalancer implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rebalancer.class);

    private MyriadConfiguration cfg;
    private SchedulerState schedulerState;
    private MyriadDriverManager driverManager;
    private MyriadOperations myriadOperations;

    @Inject
    public Rebalancer(MyriadConfiguration cfg, SchedulerState schedulerState,
                      MyriadDriverManager driverManager, MyriadOperations myriadOperations) {
        Preconditions.checkArgument(cfg != null);
        Preconditions.checkArgument(schedulerState != null);
        Preconditions.checkArgument(driverManager != null);
        Preconditions.checkArgument(myriadOperations != null);

        this.cfg = cfg;
        this.schedulerState = schedulerState;
        this.driverManager = driverManager;
        this.myriadOperations = myriadOperations;
    }

    @Override
    public void run() {
        LOGGER.info("Active {}, Pending {}", schedulerState.getActiveTaskIds().size(), schedulerState.getPendingTaskIds().size());
        if (schedulerState.getActiveTaskIds().size() < 1 && schedulerState.getPendingTaskIds().size() < 1) {
            myriadOperations.flexUpCluster(1, "small");
        }
//            RestAdapter restAdapter = new RestAdapter.Builder()
//                    .setEndpoint("http://" + host + ":" + port)
//                    .setLogLevel(LogLevel.FULL).build();
//            YARNResourceManagerService service = restAdapter
//                    .create(YARNResourceManagerService.class);
//
//            ClusterMetrics metrics = service.metrics().getClusterMetrics();
//            AppsResponse appsResponse = service.apps("ACCEPTED");
//
//            int acceptedApps = 0;
//
//            if (appsResponse == null || appsResponse.getApps() == null
//                    || appsResponse.getApps().getApps() == null) {
//                acceptedApps = 0;
//            } else {
//                acceptedApps = appsResponse.getApps().getApps().size();
//            }
//            LOGGER.info("Metrics: {}", metrics);
//            LOGGER.info("Apps: {}", appsResponse);
//
//            long availableMB = metrics.getAvailableMB();
//            long allocatedMB = metrics.getAllocatedMB();
//            long reservedMB = metrics.getReservedMB();
//            int activeNodes = metrics.getActiveNodes();
//            int unhealthyNodes = metrics.getUnhealthyNodes();
//            int appsPending = metrics.getAppsPending();
//            int appsRunning = metrics.getAppsRunning();

//            if (activeNodes == 0 && appsPending > 0) {
//                LOGGER.info(
//                        "Flexing up for condition: activeNodes ({}) == 0 && appsPending ({}) > 0",
//                        activeNodes, appsPending);
//                this.myriadOperations.flexUpCluster(clusterId, 1, "small");
//            } else if (appsPending == 0 && appsRunning == 0 && activeNodes > 0) {
//                LOGGER.info(
//                        "Flexing down for condition: appsPending ({}) == 0 && appsRunning ({}) == 0 && activeNodes ({}) > 0",
//                        appsPending, appsRunning, activeNodes);
//                this.myriadOperations.flexDownCluster(cluster, 1);
//            } else if (acceptedApps > 0) {
//                LOGGER.info("Flexing up for condition: acceptedApps ({}) > 0",
//                        acceptedApps);
//                this.myriadOperations.flexUpCluster(clusterId, 1, "small");
//            } else {
//                LOGGER.info("Nothing to rebalance");
//                this.schedulerState.releaseLock(clusterId);
//            }
    }
}
