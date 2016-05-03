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
package org.apache.myriad.scheduler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.mesos.Protos.*;
import org.apache.mesos.Protos.CommandInfo.URI;
import org.apache.mesos.Protos.Value.Range;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.configuration.MyriadExecutorConfiguration;
import org.apache.myriad.state.NodeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;


/**
 * Creates Tasks based on mesos offers
 */
public interface TaskFactory {
  static final String YARN_RESOURCEMANAGER_HOSTNAME = "yarn.resourcemanager.hostname";
  static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";
  static final String YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS = "yarn.resourcemanager.webapp.https.address";
  static final String YARN_HTTP_POLICY = "yarn.http.policy";
  static final String YARN_HTTP_POLICY_HTTPS_ONLY = "HTTPS_ONLY";

  TaskInfo createTask(Offer offer, FrameworkID frameworkId, TaskID taskId, NodeTask nodeTask);

  // TODO(Santosh): This is needed because the ExecutorInfo constructed
  // to launch NM needs to be specified to launch placeholder tasks for
  // yarn containers (for fine grained scaling).
  // If mesos supports just specifying the 'ExecutorId' without the full
  // ExecutorInfo, we wouldn't need this interface method.
  ExecutorInfo getExecutorInfoForSlave(FrameworkID frameworkId, Offer offer, CommandInfo commandInfo);

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
    public NMTaskFactoryImpl(MyriadConfiguration cfg, TaskUtils taskUtils, ExecutorCommandLineGenerator clGenerator) {
      this.cfg = cfg;
      this.taskUtils = taskUtils;
      this.clGenerator = clGenerator;
      this.constraints = new NMTaskConstraints();
    }

    @VisibleForTesting
    protected static HashSet<Long> getNMPorts(Resource resource) {
      HashSet<Long> ports = new HashSet<>();
      if (resource.getName().equals("ports")) {
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

        Preconditions.checkState(allAvailablePorts.size() >= NMPorts.expectedNumPorts(), "Not enough ports in offer");

        while (ports.size() < NMPorts.expectedNumPorts()) {
          int portIndex = rand.nextInt(allAvailablePorts.size());
          ports.add(allAvailablePorts.get(portIndex));
          allAvailablePorts.remove(portIndex);
        }
      }
      return ports;
    }

    //Utility function to get the first NMPorts.expectedNumPorts number of ports of an offer
    @VisibleForTesting
    protected static NMPorts getPorts(Offer offer) {
      HashSet<Long> ports = new HashSet<>();
      for (Resource resource : offer.getResourcesList()) {
        if (resource.getName().equals("ports") && (!resource.hasRole() || resource.getRole().equals("*"))) {
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

      if (myriadExecutorConfiguration.getJvmUri().isPresent()) {
        final String jvmRemoteUri = myriadExecutorConfiguration.getJvmUri().get();
        LOGGER.info("Getting JRE distribution from:" + jvmRemoteUri);
        URI jvmUri = URI.newBuilder().setValue(jvmRemoteUri).setExtract(true).build();
        commandInfo.addUris(jvmUri);
      }

      if (myriadExecutorConfiguration.getConfigUri().isPresent()) {
        String configURI = myriadExecutorConfiguration.getConfigUri().get();
        LOGGER.info("Getting Hadoop distribution from: {}", configURI);
        commandInfo.addUris(URI.newBuilder().setValue(configURI).build());
      }

      if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
        //Both FrameworkUser and FrameworkSuperuser to get all of the directory permissions correct.
        if (!(cfg.getFrameworkUser().isPresent() && cfg.getFrameworkSuperUser().isPresent())) {
          throw new RuntimeException("Trying to use remote distribution, but frameworkUser" + "and/or frameworkSuperUser not set!");
        }
        String nodeManagerUri = myriadExecutorConfiguration.getNodeManagerUri().get();
        cmd = clGenerator.generateCommandLine(profile, ports);

        //get the nodemanagerURI
        //We're going to extract ourselves, so setExtract is false
        LOGGER.info("Getting Hadoop distribution from: {}", nodeManagerUri);
        URI nmUri = URI.newBuilder().setValue(nodeManagerUri).setExtract(false).build();

        //get configs directly from resource manager
        String configUrlString = clGenerator.getConfigurationUrl();
        LOGGER.info("Getting config from:" + configUrlString);
        URI configUri = URI.newBuilder().setValue(configUrlString).build();
        LOGGER.info("Slave will execute command: {}",  cmd);
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
      Double taskMemory = serviceProfile.getAggregateMemory();
      Double taskCpus = serviceProfile.getAggregateCpu();

      CommandInfo commandInfo = getCommandInfo(serviceProfile, ports);
      ExecutorInfo executorInfo = getExecutorInfoForSlave(frameworkId, offer, commandInfo);

      TaskInfo.Builder taskBuilder = TaskInfo.newBuilder().setName(cfg.getFrameworkName() + "-" + taskId.getValue()).setTaskId(taskId).setSlaveId(
          offer.getSlaveId());

      return taskBuilder
          .addAllResources(taskUtils.getScalarResource(offer, "cpus", taskCpus, taskUtils.getExecutorCpus()))
          .addAllResources(taskUtils.getScalarResource(offer, "mem", taskMemory, taskUtils.getExecutorMemory()))
          .addResources(Resource.newBuilder().setName("ports").setType(Value.Type.RANGES).setRanges(Value.Ranges.newBuilder()
              .addRange(Range.newBuilder().setBegin(ports.getRpcPort()).setEnd(ports.getRpcPort()).build())
              .addRange(Range.newBuilder().setBegin(ports.getLocalizerPort()).setEnd(ports.getLocalizerPort()).build())
              .addRange(Range.newBuilder().setBegin(ports.getWebAppHttpPort()).setEnd(ports.getWebAppHttpPort()).build())
              .addRange(Range.newBuilder().setBegin(ports.getShufflePort()).setEnd(ports.getShufflePort()).build())))
          .setExecutor(executorInfo)
          .build();
    }

    @Override
    public ExecutorInfo getExecutorInfoForSlave(FrameworkID frameworkId, Offer offer, CommandInfo commandInfo) {
      ExecutorID executorId = ExecutorID.newBuilder()
          .setValue(EXECUTOR_PREFIX + frameworkId.getValue() + offer.getId().getValue() + offer.getSlaveId().getValue())
          .build();
      ExecutorInfo.Builder executorInfo = ExecutorInfo.newBuilder().setCommand(commandInfo).setName(EXECUTOR_NAME).setExecutorId(executorId)
              .addAllResources(taskUtils.getScalarResource(offer, "cpus", taskUtils.getExecutorCpus(), 0.0))
              .addAllResources(taskUtils.getScalarResource(offer, "mem", taskUtils.getExecutorMemory(), 0.0));
      if (cfg.getContainerInfo().isPresent()) {
        executorInfo.setContainer(taskUtils.getContainerInfo());
      }
      return executorInfo.build();
    }
  }

  /**
   * Implement NM Task Constraints
   */
  public static class NMTaskConstraints implements TaskConstraints {

    @Override
    public int portsCount() {
      return NMPorts.expectedNumPorts();
    }
  }
}
