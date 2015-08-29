package com.ebay.myriad.scheduler;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.configuration.MyriadExecutorConfiguration;
import com.ebay.myriad.state.NodeTask;
import com.google.common.base.Preconditions;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
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
import org.apache.mesos.Protos.Value.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * Creates Tasks based on mesos offers
 */
public interface TaskFactory {
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
    private static final String YARN_RESOURCEMANAGER_HOSTNAME = "yarn.resourcemanager.hostname";
    private static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";
    private static final String YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS = "yarn.resourcemanager.webapp.https.address";
    private static final String YARN_HTTP_POLICY = "yarn.http.policy";
    private static final String YARN_HTTP_POLICY_HTTPS_ONLY = "HTTPS_ONLY";

    private static final Logger LOGGER = LoggerFactory.getLogger(NMTaskFactoryImpl.class);
    private MyriadConfiguration cfg;
    private TaskUtils taskUtils;

    @Inject
    public NMTaskFactoryImpl(MyriadConfiguration cfg, TaskUtils taskUtils) {
      this.cfg = cfg;
      this.taskUtils = taskUtils;
    }

    //Utility function to get the first NMPorts.expectedNumPorts number of ports of an offer
    private static NMPorts getPorts(Offer offer) {
      HashSet<Long> ports = new HashSet<>();
      for (Resource resource : offer.getResourcesList()){
        if (resource.getName().equals("ports")){
          /*
          ranges.getRangeList() returns a list of ranges, each range specifies a begin and end only.
          so must loop though each range until we get all ports needed.  We exit each loop as soon as all
          ports are found so bounded by NMPorts.expectedNumPorts.
          */
          Iterator<Value.Range> itr = resource.getRanges().getRangeList().iterator();
          while (itr.hasNext() && ports.size() < NMPorts.expectedNumPorts()) {
            Value.Range range = itr.next();
            if (range.getBegin() <= range.getEnd()) {
              long i = range.getBegin();
              while (i <= range.getEnd() && ports.size() < NMPorts.expectedNumPorts()) {
                ports.add(i);
                i++;
              }
            }
          }
        }
      }

      Preconditions.checkState(ports.size() == NMPorts.expectedNumPorts(), "Not enough ports in offer");
      Long [] portArray = ports.toArray(new Long [ports.size()]);
      return new NMPorts(portArray);
    }

    private String getConfigurationUrl() {
      YarnConfiguration conf = new YarnConfiguration();
      String httpPolicy = conf.get(YARN_HTTP_POLICY);
      if (httpPolicy != null && httpPolicy.equals(YARN_HTTP_POLICY_HTTPS_ONLY)) {
        String address = conf.get(YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS);
        if (address == null || address.isEmpty()) {
          address = conf.get(YARN_RESOURCEMANAGER_HOSTNAME) + ":8090";
        }
        return "https://" + address + "/conf";
      } else {
        String address = conf.get(YARN_RESOURCEMANAGER_WEBAPP_ADDRESS);
        if (address == null || address.isEmpty()) {
          address = conf.get(YARN_RESOURCEMANAGER_HOSTNAME) + ":8088";
        }
        return "http://" + address + "/conf";
      }
    }

    private CommandInfo getCommandInfo(NMProfile profile, NMPorts ports) {
      MyriadExecutorConfiguration myriadExecutorConfiguration = cfg.getMyriadExecutorConfiguration();
      CommandInfo.Builder commandInfo = CommandInfo.newBuilder();
      ExecutorCommandLineGenerator clGenerator;
      String cmd;

      if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
        //Both FrameworkUser and FrameworkSuperuser to get all of the directory permissions correct.
        if (!(cfg.getFrameworkUser().isPresent() && cfg.getFrameworkSuperUser().isPresent())) {
          throw new RuntimeException("Trying to use remote distribution, but frameworkUser" +
            "and/or frameworkSuperUser not set!");
        }
        String nodeManagerUri = myriadExecutorConfiguration.getNodeManagerUri().get();
        clGenerator = new DownloadNMExecutorCLGenImpl(cfg, profile, ports, nodeManagerUri);
        cmd = clGenerator.generateCommandLine();

        //get the nodemanagerURI
        //We're going to extract ourselves, so setExtract is false
        LOGGER.info("Getting Hadoop distribution from:" + nodeManagerUri);
        URI nmUri = URI.newBuilder().setValue(nodeManagerUri).setExtract(false).build();

        //get configs directly from resource manager
        String configUrlString = getConfigurationUrl();
        LOGGER.info("Getting config from:" + configUrlString);
        URI configUri = URI.newBuilder().setValue(configUrlString)
          .build();
        LOGGER.info("Slave will execute command:" + cmd);
        commandInfo.addUris(nmUri).addUris(configUri).setValue("echo \"" + cmd + "\";" + cmd);
        commandInfo.setUser(cfg.getFrameworkSuperUser().get());

      } else {
        clGenerator = new NMExecutorCLGenImpl(cfg, profile, ports);
        cmd = clGenerator.generateCommandLine();
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

      NMProfile profile = nodeTask.getProfile();
      Scalar taskMemory = Scalar.newBuilder()
          .setValue(taskUtils.getTaskMemory(profile))
          .build();
      Scalar taskCpus = Scalar.newBuilder()
          .setValue(taskUtils.getTaskCpus(profile))
          .build();

      CommandInfo commandInfo = getCommandInfo(profile, ports);
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
}
