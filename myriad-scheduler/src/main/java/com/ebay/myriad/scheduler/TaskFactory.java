/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ebay.myriad.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.inject.Inject;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.configuration.MyriadExecutorConfiguration;
import com.ebay.myriad.state.NodeTask;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.CommandInfo.URI;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Protos.Value.Range;
import org.apache.mesos.Protos.Value.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Creates Tasks based on mesos offers
 */
public interface TaskFactory {
  static final String YARN_RESOURCEMANAGER_HOSTNAME = "yarn.resourcemanager.hostname";
  static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";
  static final String YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS = "yarn.resourcemanager.webapp.https.address";
  static final String YARN_HTTP_POLICY = "yarn.http.policy";
  static final String YARN_HTTP_POLICY_HTTPS_ONLY = "HTTPS_ONLY";

  TaskInfo createTask(Offer offer, FrameworkID frameworkId, 
    TaskID taskId, NodeTask nodeTask);

  // TODO(Santosh): This is needed because the ExecutorInfo constructed
  // to launch NM needs to be specified to launch placeholder tasks for
  // yarn containers (for fine grained scaling).
  // If mesos supports just specifying the 'ExecutorId' without the full
  // ExecutorInfo, we wouldn't need this interface method.
  ExecutorInfo getExecutorInfoForSlave(FrameworkID frameworkId,
    Offer offer, CommandInfo commandInfo);
  
  /**
   * Creates TaskInfo objects to launch NMs as mesos tasks.
   */
  class NMTaskFactoryImpl implements TaskFactory {
    public static final String EXECUTOR_NAME = "myriad_task";
    public static final String EXECUTOR_PREFIX = "myriad_executor";
    public static final String YARN_NODEMANAGER_OPTS_KEY = "YARN_NODEMANAGER_OPTS";

    private static final Logger LOGGER = LoggerFactory.getLogger(NMTaskFactoryImpl.class);
    private static final Random rand = new Random();
    private MyriadConfiguration cfg;
    private TaskUtils taskUtils;
    private ExecutorCommandLineGenerator clGenerator;
    private TaskConstraints constraints;

    @Inject
    public NMTaskFactoryImpl(MyriadConfiguration cfg, TaskUtils taskUtils,
      ExecutorCommandLineGenerator clGenerator) {
      this.cfg = cfg;
      this.taskUtils = taskUtils;
      this.clGenerator = clGenerator;
      this.constraints = new NMTaskConstraints();
    }

    @VisibleForTesting
    protected static HashSet<Long> getNMPorts(Resource resource) {
      HashSet<Long> ports = new HashSet<>();
      if (resource.getName().equals("ports")){
        /*
        ranges.getRangeList() returns a list of ranges, each range specifies a begin and end only.
        so must loop though each range until we get all ports needed.  We exit each loop as soon as all
        ports are found so bounded by NMPorts.expectedNumPorts.
        */
        final List<Range> ranges = resource.getRanges().getRangeList();
        final List<Long> allAvailablePorts = new ArrayList<>();
        for (Range range : ranges) {
          if (range.hasBegin() && range.hasEnd()) {
            for (long i = range.getBegin(); i <= range.getEnd(); i++) {
              allAvailablePorts.add(i);
            }
          }
        }
        final int allAvailablePortsSize = allAvailablePorts.size();
        Preconditions.checkState(allAvailablePorts.size() >= NMPorts.expectedNumPorts(), "Not enough ports in offer");
        
        while (ports.size() < NMPorts.expectedNumPorts()) {
          int portIndex = rand.nextInt(allAvailablePortsSize);
          ports.add(allAvailablePorts.get(portIndex));
        }        
      }
      return ports;
    }
    
    //Utility function to get the first NMPorts.expectedNumPorts number of ports of an offer
    @VisibleForTesting
    protected static NMPorts getPorts(Offer offer) {
      HashSet<Long> ports = new HashSet<>();
      for (Resource resource : offer.getResourcesList()) {
        if (resource.getName().equals("ports")) {
          ports = getNMPorts(resource);
          break;
        }
      }

      Long [] portArray = ports.toArray(new Long [ports.size()]);
      return new NMPorts(portArray);
    }

    @VisibleForTesting
    CommandInfo getCommandInfo(ServiceResourceProfile profile, NMPorts ports) {
      MyriadExecutorConfiguration myriadExecutorConfiguration = cfg.getMyriadExecutorConfiguration();
      CommandInfo.Builder commandInfo = CommandInfo.newBuilder();
      String cmd;

      if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
        //Both FrameworkUser and FrameworkSuperuser to get all of the directory permissions correct.
        if (!(cfg.getFrameworkUser().isPresent() && cfg.getFrameworkSuperUser().isPresent())) {
          throw new RuntimeException("Trying to use remote distribution, but frameworkUser" +
            "and/or frameworkSuperUser not set!");
        }
        String nodeManagerUri = myriadExecutorConfiguration.getNodeManagerUri().get();
        cmd = clGenerator.generateCommandLine(profile, ports);

        //get the nodemanagerURI
        //We're going to extract ourselves, so setExtract is false
        LOGGER.info("Getting Hadoop distribution from:" + nodeManagerUri);
        URI nmUri = URI.newBuilder().setValue(nodeManagerUri).setExtract(false).build();

        //get configs directly from resource manager
        String configUrlString = clGenerator.getConfigurationUrl();
        LOGGER.info("Getting config from:" + configUrlString);
        URI configUri = URI.newBuilder().setValue(configUrlString)
          .build();
        LOGGER.info("Slave will execute command:" + cmd);
        commandInfo.addUris(nmUri).addUris(configUri).setValue("echo \"" + cmd + "\";" + cmd);
        commandInfo.setUser(cfg.getFrameworkSuperUser().get());

      } else {
        cmd = clGenerator.generateCommandLine(profile, ports);
        commandInfo.setValue("echo \"" + cmd + "\";" + cmd);

        if (cfg.getFrameworkUser().isPresent()) {
          commandInfo.setUser(cfg.getFrameworkUser().get());
        }
      }
      return commandInfo.build();
    }

    @Override
    public TaskInfo createTask(Offer offer, FrameworkID frameworkId, TaskID taskId, NodeTask nodeTask) {
      Objects.requireNonNull(offer, "Offer should be non-null");
      Objects.requireNonNull(nodeTask, "NodeTask should be non-null");

      NMPorts ports = getPorts(offer);
      LOGGER.debug(ports.toString());

      ServiceResourceProfile serviceProfile = nodeTask.getProfile();
      Scalar taskMemory = Scalar.newBuilder()
          .setValue(serviceProfile.getAggregateMemory())
          .build();
      Scalar taskCpus = Scalar.newBuilder()
          .setValue(serviceProfile.getAggregateCpu())
          .build();

      CommandInfo commandInfo = getCommandInfo(serviceProfile, ports);
      ExecutorInfo executorInfo = getExecutorInfoForSlave(frameworkId, offer, commandInfo);

      TaskInfo.Builder taskBuilder = TaskInfo.newBuilder()
          .setName("task-" + taskId.getValue())
          .setTaskId(taskId)
          .setSlaveId(offer.getSlaveId());

      return taskBuilder
          .addResources(
              Resource.newBuilder().setName("cpus")
                  .setType(Value.Type.SCALAR)
                  .setScalar(taskCpus)
                  .build())
          .addResources(
              Resource.newBuilder().setName("mem")
                  .setType(Value.Type.SCALAR)
                  .setScalar(taskMemory)
                  .build())
          .addResources(
              Resource.newBuilder().setName("ports")
                  .setType(Value.Type.RANGES)
                  .setRanges(Value.Ranges.newBuilder()
                      .addRange(Value.Range.newBuilder()
                          .setBegin(ports.getRpcPort())
                          .setEnd(ports.getRpcPort())
                          .build())
                      .addRange(Value.Range.newBuilder()
                          .setBegin(ports.getLocalizerPort())
                          .setEnd(ports.getLocalizerPort())
                          .build())
                      .addRange(Value.Range.newBuilder()
                          .setBegin(ports.getWebAppHttpPort())
                          .setEnd(ports.getWebAppHttpPort())
                          .build())
                      .addRange(Value.Range.newBuilder()
                          .setBegin(ports.getShufflePort())
                          .setEnd(ports.getShufflePort())
                          .build())))
          .setExecutor(executorInfo).build();
    }

    @Override
    public ExecutorInfo getExecutorInfoForSlave(FrameworkID frameworkId, Offer offer,
      CommandInfo commandInfo) {
      Scalar executorMemory = Scalar.newBuilder()
          .setValue(taskUtils.getExecutorMemory()).build();
      Scalar executorCpus = Scalar.newBuilder()
          .setValue(taskUtils.getExecutorCpus()).build();

      ExecutorID executorId = ExecutorID.newBuilder()
          .setValue(EXECUTOR_PREFIX + frameworkId.getValue() +
              offer.getId().getValue() + offer.getSlaveId().getValue())
          .build();
      return ExecutorInfo
          .newBuilder()
          .setCommand(commandInfo)
          .setName(EXECUTOR_NAME)
          .addResources(
              Resource.newBuilder().setName("cpus")
                  .setType(Value.Type.SCALAR)
                  .setScalar(executorCpus).build())
          .addResources(
              Resource.newBuilder().setName("mem")
                  .setType(Value.Type.SCALAR)
                  .setScalar(executorMemory).build())
          .setExecutorId(executorId).build();
    }
  }
  
  /**
   * Implement NM Task Constraints
   *
   */
  public static class NMTaskConstraints implements TaskConstraints {

    @Override
    public int portsCount() {
      return NMPorts.expectedNumPorts();
    }
  }
}
