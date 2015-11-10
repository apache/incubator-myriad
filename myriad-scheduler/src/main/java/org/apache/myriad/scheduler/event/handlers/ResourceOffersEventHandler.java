/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler.event.handlers;

import com.google.common.collect.Sets;
import com.lmax.disruptor.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.OfferID;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.SchedulerDriver;
import org.apache.myriad.scheduler.SchedulerUtils;
import org.apache.myriad.scheduler.ServiceResourceProfile;
import org.apache.myriad.scheduler.TaskConstraints;
import org.apache.myriad.scheduler.TaskConstraintsManager;
import org.apache.myriad.scheduler.TaskFactory;
import org.apache.myriad.scheduler.TaskUtils;
import org.apache.myriad.scheduler.constraints.Constraint;
import org.apache.myriad.scheduler.constraints.LikeConstraint;
import org.apache.myriad.scheduler.event.ResourceOffersEvent;
import org.apache.myriad.scheduler.fgs.OfferLifecycleManager;
import org.apache.myriad.state.NodeTask;
import org.apache.myriad.state.SchedulerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * handles and logs resource offers events
 */
public class ResourceOffersEventHandler implements EventHandler<ResourceOffersEvent> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceOffersEventHandler.class);

  private static final Lock driverOperationLock = new ReentrantLock();

  private static final String RESOURCES_CPU_KEY = "cpus";
  private static final String RESOURCES_MEM_KEY = "mem";
  private static final String RESOURCES_PORTS_KEY = "ports";
  private static final String RESOURCES_DISK_KEY = "disk";


  @Inject
  private SchedulerState schedulerState;

  @Inject
  private TaskUtils taskUtils;

  @Inject
  private Map<String, TaskFactory> taskFactoryMap;

  @Inject
  private OfferLifecycleManager offerLifecycleMgr;

  @Inject
  private TaskConstraintsManager taskConstraintsManager;

  @Override
  public void onEvent(ResourceOffersEvent event, long sequence, boolean endOfBatch) throws Exception {
    SchedulerDriver driver = event.getDriver();
    List<Offer> offers = event.getOffers();

    // Sometimes, we see that mesos sends resource offers before Myriad receives
    // a notification for "framework registration". This is a simple defensive code
    // to not process any offers unless Myriad receives a "framework registered" notification.
    if (schedulerState.getFrameworkID() == null) {
      LOGGER.warn("Received {} offers, but declining them since Framework ID is not yet set", offers.size());
      for (Offer offer : offers) {
        driver.declineOffer(offer.getId());
      }
      return;
    }
    LOGGER.info("Received offers {}", offers.size());
    LOGGER.debug("Pending tasks: {}", this.schedulerState.getPendingTaskIds());
    driverOperationLock.lock();
    try {
      for (Iterator<Offer> iterator = offers.iterator(); iterator.hasNext(); ) {
        Offer offer = iterator.next();
        Set<NodeTask> nodeTasks = schedulerState.getNodeTasks(offer.getSlaveId());
        for (NodeTask nodeTask : nodeTasks) {
          nodeTask.setSlaveAttributes(offer.getAttributesList());
        }
        // keep this in case SchedulerState gets out of sync. This should not happen with
        // synchronizing addNodes method in SchedulerState
        // but to keep it safe
        final Set<Protos.TaskID> missingTasks = Sets.newHashSet();
        Set<Protos.TaskID> pendingTasks = schedulerState.getPendingTaskIds();
        if (CollectionUtils.isNotEmpty(pendingTasks)) {
          for (Protos.TaskID pendingTaskId : pendingTasks) {
            NodeTask taskToLaunch = schedulerState.getTask(pendingTaskId);
            if (taskToLaunch == null) {
              missingTasks.add(pendingTaskId);
              LOGGER.warn("Node task for TaskID: {} does not exist", pendingTaskId);
              continue;
            }
            String taskPrefix = taskToLaunch.getTaskPrefix();
            ServiceResourceProfile profile = taskToLaunch.getProfile();
            Constraint constraint = taskToLaunch.getConstraint();

            Set<NodeTask> launchedTasks = new HashSet<>();
            launchedTasks.addAll(schedulerState.getActiveTasksByType(taskPrefix));
            launchedTasks.addAll(schedulerState.getStagingTasksByType(taskPrefix));

            if (matches(offer, taskToLaunch, constraint) && SchedulerUtils.isUniqueHostname(offer, taskToLaunch, launchedTasks)) {
              try {
                final TaskInfo task = taskFactoryMap.get(taskPrefix).createTask(offer, schedulerState.getFrameworkID(),
                    pendingTaskId, taskToLaunch);
                List<OfferID> offerIds = new ArrayList<>();
                offerIds.add(offer.getId());
                List<TaskInfo> tasks = new ArrayList<>();
                tasks.add(task);
                LOGGER.info("Launching task: {} using offer: {}", task.getTaskId().getValue(), offer.getId());
                LOGGER.debug("Launching task: {} with profile: {} using offer: {}", task, profile, offer);
                driver.launchTasks(offerIds, tasks);
                schedulerState.makeTaskStaging(pendingTaskId);

                // For every NM Task that we launch, we currently
                // need to backup the ExecutorInfo for that NM Task in the State Store.
                // Without this, we will not be able to launch tasks corresponding to yarn
                // containers. This is specially important in case the RM restarts.
                taskToLaunch.setExecutorInfo(task.getExecutor());
                taskToLaunch.setHostname(offer.getHostname());
                taskToLaunch.setSlaveId(offer.getSlaveId());
                schedulerState.addTask(pendingTaskId, taskToLaunch);
                iterator.remove(); // remove the used offer from offers list
                break;
              } catch (Throwable t) {
                LOGGER.error("Exception thrown while trying to create a task for {}", taskPrefix, t);
              }
            }
          }
          for (Protos.TaskID taskId : missingTasks) {
            schedulerState.removeTask(taskId);
          }
        }
      }

      for (Offer offer : offers) {
        if (SchedulerUtils.isEligibleForFineGrainedScaling(offer.getHostname(), schedulerState)) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Picking an offer from slave with hostname {} for fine grained scaling.", offer.getHostname());
          }
          offerLifecycleMgr.addOffers(offer);
        } else {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Declining offer {} from slave {}.", offer, offer.getHostname());
          }
          driver.declineOffer(offer.getId());
        }
      }
    } finally {
      driverOperationLock.unlock();
    }
  }

  private boolean matches(Offer offer, NodeTask taskToLaunch, Constraint constraint) {
    if (!meetsConstraint(offer, constraint)) {
      return false;
    }
    Map<String, Object> results = new HashMap<String, Object>(5);
    //Assign default values to avoid NPE
    results.put(RESOURCES_CPU_KEY, Double.valueOf(0.0));
    results.put(RESOURCES_MEM_KEY, Double.valueOf(0.0));
    results.put(RESOURCES_DISK_KEY, Double.valueOf(0.0));
    results.put(RESOURCES_PORTS_KEY, Integer.valueOf(0));

    for (Resource resource : offer.getResourcesList()) {
      if (resourceEvaluators.containsKey(resource.getName())) {
        resourceEvaluators.get(resource.getName()).eval(resource, results);
      } else {
        LOGGER.warn("Ignoring unknown resource type: {}", resource.getName());
      }
    }
    double cpus = (Double) results.get(RESOURCES_CPU_KEY);
    double mem = (Double) results.get(RESOURCES_MEM_KEY);
    int ports = (Integer) results.get(RESOURCES_PORTS_KEY);

    checkResource(cpus <= 0, RESOURCES_CPU_KEY);
    checkResource(mem <= 0, RESOURCES_MEM_KEY);
    checkResource(ports <= 0, RESOURCES_PORTS_KEY);

    return checkAggregates(offer, taskToLaunch, ports, cpus, mem);
  }

  private boolean checkAggregates(Offer offer, NodeTask taskToLaunch, int ports, double cpus, double mem) {
    final ServiceResourceProfile profile = taskToLaunch.getProfile();
    final String taskPrefix = taskToLaunch.getTaskPrefix();
    final double aggrCpu = profile.getAggregateCpu() + profile.getExecutorCpu();
    final double aggrMem = profile.getAggregateMemory() + profile.getExecutorMemory();
    final TaskConstraints taskConstraints = taskConstraintsManager.getConstraints(taskPrefix);
    if (aggrCpu <= cpus && aggrMem <= mem && taskConstraints.portsCount() <= ports) {
      return true;
    } else {
      LOGGER.info("Offer not sufficient for task with, cpu: {}, memory: {}, ports: {}", aggrCpu, aggrMem, ports);
      return false;
    }
  }

  private boolean meetsConstraint(Offer offer, Constraint constraint) {
    if (constraint != null) {
      switch (constraint.getType()) {
        case LIKE: {
          LikeConstraint likeConstraint = (LikeConstraint) constraint;
          if (likeConstraint.isConstraintOnHostName()) {
            return likeConstraint.matchesHostName(offer.getHostname());
          } else {
            return likeConstraint.matchesSlaveAttributes(offer.getAttributesList());
          }
        }
        default:
          return false;
      }
    }
    return true;
  }

  private void checkResource(boolean fail, String resource) {
    if (fail) {
      LOGGER.info("No " + resource + " resources present");
    }
  }

  private static Double scalarToDouble(Resource resource, String id) {
    Double value = new Double(0.0);
    if (resource.getType().equals(Value.Type.SCALAR)) {
      value = new Double(resource.getScalar().getValue());
    } else {
      LOGGER.error(id + " resource was not a scalar: {}", resource.getType().toString());
    }
    return value;
  }

  private interface EvalResources {
    public void eval(Resource resource, Map<String, Object> results);
  }

  private static Map<String, EvalResources> resourceEvaluators;

  static {
    resourceEvaluators = new HashMap<String, EvalResources>(4);
    resourceEvaluators.put(RESOURCES_CPU_KEY, new EvalResources() {
      public void eval(Resource resource, Map<String, Object> results) {
        results.put(RESOURCES_CPU_KEY, (Double) results.get(RESOURCES_CPU_KEY) + scalarToDouble(resource, RESOURCES_CPU_KEY));
      }
    });
    resourceEvaluators.put(RESOURCES_MEM_KEY, new EvalResources() {
      public void eval(Resource resource, Map<String, Object> results) {
        results.put(RESOURCES_MEM_KEY, (Double) results.get(RESOURCES_MEM_KEY) + scalarToDouble(resource, RESOURCES_MEM_KEY));
      }
    });
    resourceEvaluators.put(RESOURCES_DISK_KEY, new EvalResources() {
      public void eval(Resource resource, Map<String, Object> results) {
      }
    });
    resourceEvaluators.put(RESOURCES_PORTS_KEY, new EvalResources() {
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
          LOGGER.error("ports resource was not Ranges: {}", resource.getType().toString());

        }
        results.put(RESOURCES_PORTS_KEY, (Integer) results.get(RESOURCES_PORTS_KEY) + Integer.valueOf(ports));
      }
    });
  }
}
