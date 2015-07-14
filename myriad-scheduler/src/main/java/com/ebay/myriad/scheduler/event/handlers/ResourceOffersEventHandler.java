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
package com.ebay.myriad.scheduler.event.handlers;

import com.ebay.myriad.scheduler.NMPorts;
import com.ebay.myriad.scheduler.NMProfile;
import com.ebay.myriad.scheduler.SchedulerUtils;
import com.ebay.myriad.scheduler.TaskFactory;
import com.ebay.myriad.scheduler.TaskUtils;
import com.ebay.myriad.scheduler.event.ResourceOffersEvent;
import com.ebay.myriad.state.NodeTask;
import com.ebay.myriad.state.SchedulerState;
import com.lmax.disruptor.EventHandler;
import org.apache.commons.collections.CollectionUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * handles and logs resource offers events
 */
public class ResourceOffersEventHandler implements EventHandler<ResourceOffersEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceOffersEventHandler.class);

    private static final Lock driverOperationLock = new ReentrantLock();

    @Inject
    private SchedulerState schedulerState;

    @Inject
    private TaskFactory taskFactory;

    @Inject
    private TaskUtils taskUtils;

    @Override
    public void onEvent(ResourceOffersEvent event, long sequence,
                        boolean endOfBatch) throws Exception {
        SchedulerDriver driver = event.getDriver();
        List<Offer> offers = event.getOffers();

        LOGGER.info("Received offers {}", offers.size());
        LOGGER.debug("Pending tasks: {}", this.schedulerState.getPendingTaskIds());
        driverOperationLock.lock();
        try {
            Set<Protos.TaskID> pendingTasks = schedulerState.getPendingTaskIds();
            if (CollectionUtils.isNotEmpty(pendingTasks)) {
                for (Offer offer : offers) {
                    boolean offerMatch = false;
                    Protos.TaskID launchedTaskId = null;
                    for (Protos.TaskID pendingTaskId : pendingTasks) {
                        NodeTask taskToLaunch = schedulerState
                                .getTask(pendingTaskId);
                        NMProfile profile = taskToLaunch.getProfile();

                        if (matches(offer, profile)
                                && SchedulerUtils.isUniqueHostname(offer,
                                schedulerState.getActiveTasks())) {
                            TaskInfo task = taskFactory.createTask(offer, pendingTaskId,
                                    taskToLaunch);
                            List<OfferID> offerIds = new ArrayList<>();
                            offerIds.add(offer.getId());
                            List<TaskInfo> tasks = new ArrayList<>();
                            tasks.add(task);
                            LOGGER.info("Launching task: {} using offer: {}", task.getTaskId().getValue(), offer.getId());
                            LOGGER.debug("Launching task: {} with profile: {} using offer: {}", task, profile, offer);

                            driver.launchTasks(offerIds, tasks);
                            launchedTaskId = pendingTaskId;

                            taskToLaunch.setHostname(offer.getHostname());
                            taskToLaunch.setSlaveId(offer.getSlaveId());
                            offerMatch = true;
                            break;
                        }
                    }
                    if (null != launchedTaskId) {
                        schedulerState.makeTaskStaging(launchedTaskId);
                        launchedTaskId = null;
                    }
                    if (!offerMatch) {
                        LOGGER.info(
                                "Declining offer {}, as it didn't match any pending task.",
                                offer);
                        driver.declineOffer(offer.getId());
                    }
                }
            } else {
                LOGGER.info("No pending tasks, declining all offers");
                for (Offer offer : offers) {
                    driver.declineOffer(offer.getId());
                }
            }
        } finally {
            driverOperationLock.unlock();
        }
    }

    private boolean matches(Offer offer, NMProfile profile) {
        Map<String, Object> results = new HashMap<String, Object>(5);

        for (Resource resource : offer.getResourcesList()) {
            if (resourceEvaluators.containsKey(resource.getName())) {
                resourceEvaluators.get(resource.getName()).eval(resource, results);
            } else {
                LOGGER.warn("Ignoring unknown resource type: {}",
                        resource.getName());
            }
        }
        double cpus = (Double) results.get("cpus");
        double mem = (Double) results.get("mem");
        int ports = (Integer) results.get("ports");

        checkResource(cpus < 0, "cpus");
        checkResource(mem < 0, "mem");
        checkResource(ports < 0, "port");

        return checkAggregates(offer, profile, ports, cpus, mem);
    }

    private void checkResource(boolean fail, String resource) {
        if (fail) {
            LOGGER.info("No " + resource + " resources present");
        }
    }

    private boolean checkAggregates(Offer offer, NMProfile profile, int ports, double cpus, double mem) {
        Map<String, String> requestAttributes = new HashMap<>();

        if (taskUtils.getAggregateCpus(profile) <= cpus
                && taskUtils.getAggregateMemory(profile) <= mem
                && SchedulerUtils.isMatchSlaveAttributes(offer, requestAttributes)
                && NMPorts.expectedNumPorts() <= ports) {
            return true;
        } else {
            LOGGER.info("Offer not sufficient for task with, cpu: {}, memory: {}, ports: {}",
                    taskUtils.getAggregateCpus(profile), taskUtils.getAggregateMemory(profile), ports);
            return false;
        }
    }

    private static Double scalarToDouble(Resource resource, String id) {
        Double value = new Double(0.0);
        if (resource.getType().equals(Value.Type.SCALAR)) {
            value = new Double(resource.getScalar().getValue());
        } else {
            LOGGER.error(id + " resource was not a scalar: {}", resource
                    .getType().toString());
        }
        return value;
    }

    private interface EvalResources {
        public void eval(Resource resource, Map<String, Object>results);
    }

    private static Map<String, EvalResources> resourceEvaluators;

    static {
        resourceEvaluators = new HashMap<String, EvalResources>(4);
        resourceEvaluators.put("cpus", new EvalResources() {
            public void eval(Resource resource, Map<String, Object> results) {
                results.put("cpus", scalarToDouble(resource, "cpus"));
            }
        });
        resourceEvaluators.put("mem", new EvalResources() {
            public void eval(Resource resource, Map<String, Object> results) {
                results.put("mem", scalarToDouble(resource, "mem"));
            }
        });
        resourceEvaluators.put("disk", new EvalResources() {
            public void eval(Resource resource, Map<String, Object> results) {
            }
        });
        resourceEvaluators.put("ports", new EvalResources() {
            public void eval(Resource resource, Map<String, Object> results) {
                int ports = 0;
                if (resource.getType().equals(Value.Type.RANGES)) {
                    Value.Ranges ranges = resource.getRanges();
                    for (Value.Range range : ranges.getRangeList()) {
                        if (range.getBegin() < range.getEnd()) {
                            ports += range.getEnd() - range.getBegin() + 1;
                        }
                    }

                } else {
                    LOGGER.error("ports resource was not Ranges: {}", resource
                            .getType().toString());

                }
                results.put("ports", Integer.valueOf(ports));
            }
        });
    }
}
