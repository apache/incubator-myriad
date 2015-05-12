package com.ebay.myriad.scheduler;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.configuration.MyriadExecutorConfiguration;
import com.ebay.myriad.configuration.NodeManagerConfiguration;
import com.ebay.myriad.executor.NMTaskConfig;
import com.ebay.myriad.state.NodeTask;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.CommandInfo.URI;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.Offer;
import org.apache.mesos.Protos.Resource;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.Value;
import org.apache.mesos.Protos.Value.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * Creates Tasks based on mesos offers
 */
public interface TaskFactory {
    TaskInfo createTask(Offer offer, TaskID taskId, NodeTask nodeTask);

    /**
     * The Node Manager Task factory implementation
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

        private static String getFileName(String uri) {
            int lastSlash = uri.lastIndexOf('/');
            if (lastSlash == -1) {
                return uri;
            } else {
                String fileName = uri.substring(lastSlash + 1);
                Preconditions.checkArgument(!Strings.isNullOrEmpty(fileName),
                        "URI should not have a slash at the end");
                return fileName;
            }
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

        private CommandInfo getCommandInfo() {
            MyriadExecutorConfiguration myriadExecutorConfiguration = cfg.getMyriadExecutorConfiguration();
            CommandInfo.Builder commandInfo = CommandInfo.newBuilder();
            if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
                /*
                 TODO(darinj): Overall this is messier than I'd like. We can't let mesos untar the distribution, since
                 it will change the permissions.  Instead we simply download the tarball and execute tar -xvpf. We also
                 pull the config from the resource manager and put them in the conf dir.  This is also why we need
                 frameworkSuperUser. This will be refactored after Mesos-1790 is resolved.
                */

                //Both FrameworkUser and FrameworkSuperuser to get all of the directory permissions correct.
                if (!(cfg.getFrameworkUser().isPresent() && cfg.getFrameworkSuperUser().isPresent())) {
                    throw new RuntimeException("Trying to use remote distribution, but frameworkUser" +
                            "and/or frameworkSuperUser not set!");
                }

                LOGGER.info("Using remote distribution");

                String nmURIString = myriadExecutorConfiguration.getNodeManagerUri().get();

                //TODO(DarinJ) support other compression, as this is a temp fix for Mesos 1760 may not get to it.
                //Extract tarball keeping permissions, necessary to keep HADOOP_HOME/bin/container-executor suidbit set.
                String tarCmd = "sudo tar -zxpf " + getFileName(nmURIString);

                //We need the current directory to be writable by frameworkUser for capsuleExecutor to create directories.
                //Best to simply give owenership to the user running the executor but we don't want to use -R as this
                //will silently remove the suid bit on container executor.
                String chownCmd = "sudo chown " + cfg.getFrameworkUser().get() + " .";

                //Place the hadoop config where in the HADOOP_CONF_DIR where it will be read by the NodeManager
                //The url for the resource manager config is: http(s)://hostname:port/conf so fetcher.cpp downloads the
                //config file to conf, It's an xml file with the parameters of yarn-site.xml, core-site.xml and hdfs.xml.
                String configCopyCmd = "cp conf " + cfg.getYarnEnvironment().get("YARN_HOME") +
                        "/etc/hadoop/yarn-site.xml";

                //Command to run the executor
                String executorPathString = myriadExecutorConfiguration.getPath();
                String executorCmd = "export CAPSULE_CACHE_DIR=`pwd`;echo $CAPSULE_CACHE_DIR; " +
                        "sudo -E -u " + cfg.getFrameworkUser().get() + " -H " +
                        "java -Dcapsule.log=verbose -jar " + getFileName(executorPathString);

                //Concatenate all the subcommands
                String cmd = tarCmd + "&&" + chownCmd + "&&" + configCopyCmd + "&&" + executorCmd;

                //get the nodemanagerURI
                //We're going to extract ourselves, so setExtract is false
                LOGGER.info("Getting Hadoop distribution from:" + nmURIString);
                URI nmUri = URI.newBuilder().setValue(nmURIString).setExtract(false)
                        .build();

                //get configs directly from resource manager
                String configUrlString = getConfigurationUrl();
                LOGGER.info("Getting config from:" + configUrlString);
                URI configUri = URI.newBuilder().setValue(configUrlString)
                        .build();

                //get the executor URI
                LOGGER.info("Getting executor from:" + executorPathString);
                URI executorUri = URI.newBuilder().setValue(executorPathString).setExecutable(true)
                        .build();

                LOGGER.info("Slave will execute command:" + cmd);
                commandInfo.addUris(nmUri).addUris(configUri).addUris(executorUri).setValue("echo \"" + cmd + "\";" + cmd);

                commandInfo.setUser(cfg.getFrameworkSuperUser().get());

            } else {
                String cmdPrefix = "export CAPSULE_CACHE_DIR=`pwd` ;" +
                        "echo $CAPSULE_CACHE_DIR; java -Dcapsule.log=verbose -jar ";
                String executorPath = myriadExecutorConfiguration.getPath();
                String cmd = cmdPrefix + getFileName(executorPath);
                URI executorURI = URI.newBuilder().setValue(executorPath)
                        .setExecutable(true).build();
                commandInfo.addUris(executorURI)
                        .setValue("echo \"" + cmd + "\";" + cmd);

                if (cfg.getFrameworkUser().isPresent()) {
                    commandInfo.setUser(cfg.getFrameworkUser().get());
                }
            }
            return commandInfo.build();
        }

        @Override
        public TaskInfo createTask(Offer offer, TaskID taskId, NodeTask nodeTask) {
            Objects.requireNonNull(offer, "Offer should be non-null");
            Objects.requireNonNull(nodeTask, "NodeTask should be non-null");

            NMPorts ports = getPorts(offer);
            LOGGER.debug(ports.toString());

            NMProfile profile = nodeTask.getProfile();
            NMTaskConfig nmTaskConfig = new NMTaskConfig();
            nmTaskConfig.setAdvertisableCpus(profile.getCpus());
            nmTaskConfig.setAdvertisableMem(profile.getMemory());
            NodeManagerConfiguration nodeManagerConfiguration = this.cfg.getNodeManagerConfiguration();
            nmTaskConfig.setJvmOpts(nodeManagerConfiguration.getJvmOpts().orNull());
            nmTaskConfig.setCgroups(nodeManagerConfiguration.getCgroups().or(Boolean.FALSE));
            nmTaskConfig.setRpcPort(ports.getRpcPort());
            nmTaskConfig.setLocalizerPort(ports.getLocalizerPort());
            nmTaskConfig.setWebAppHttpPort(ports.getWebAppHttpPort());
            nmTaskConfig.setShufflePort(ports.getShufflePort());
            nmTaskConfig.setYarnEnvironment(cfg.getYarnEnvironment());

            // if RM's hostname is passed in as a system property, pass it along
            // to Node Managers launched via Myriad
            String rmHostName = System.getProperty(YARN_RESOURCEMANAGER_HOSTNAME);
            if (rmHostName != null && !rmHostName.isEmpty()) {

                String nmOpts = nmTaskConfig.getYarnEnvironment().get(YARN_NODEMANAGER_OPTS_KEY);
                if (nmOpts == null) {
                    nmOpts = "";
                }
                nmOpts += " " + "-D" + YARN_RESOURCEMANAGER_HOSTNAME + "=" + rmHostName;
                nmTaskConfig.getYarnEnvironment().put(YARN_NODEMANAGER_OPTS_KEY, nmOpts);
                LOGGER.info(YARN_RESOURCEMANAGER_HOSTNAME + " is set to " + rmHostName +
                        " via YARN_RESOURCEMANAGER_OPTS. Passing it into YARN_NODEMANAGER_OPTS.");
            }
//            else {
            // TODO(Santosh): Handle this case. Couple of options:
            // 1. Lookup a hostname here and use it as "RM's hostname"
            // 2. Abort here.. RM cannot start unless a hostname is passed in as it requires it to pass to NMs.

            String taskConfigJSON = new Gson().toJson(nmTaskConfig);

            Scalar taskMemory = Scalar.newBuilder()
                    .setValue(taskUtils.getTaskMemory(profile))
                    .build();
            Scalar taskCpus = Scalar.newBuilder()
                    .setValue(taskUtils.getTaskCpus(profile))
                    .build();
            Scalar executorMemory = Scalar.newBuilder()
                    .setValue(taskUtils.getExecutorMemory())
                    .build();
            Scalar executorCpus = Scalar.newBuilder()
                    .setValue(taskUtils.getExecutorCpus())
                    .build();

            CommandInfo commandInfo = getCommandInfo();

            ExecutorID executorId = ExecutorID.newBuilder()
                    .setValue(EXECUTOR_PREFIX + offer.getSlaveId().getValue())
                    .build();
            ExecutorInfo executorInfo = ExecutorInfo
                    .newBuilder()
                    .setCommand(commandInfo)
                    .setName(EXECUTOR_NAME)
                    .addResources(
                            Resource.newBuilder().setName("cpus")
                                    .setType(Value.Type.SCALAR)
                                    .setScalar(executorCpus)
                                    .build())
                    .addResources(
                            Resource.newBuilder().setName("mem")
                                    .setType(Value.Type.SCALAR)
                                    .setScalar(executorMemory)
                                    .build())
                    .setExecutorId(executorId).setCommand(commandInfo).build();

            TaskInfo.Builder taskBuilder = TaskInfo.newBuilder()
                    .setName("task-" + taskId.getValue())
                    .setTaskId(taskId)
                    .setSlaveId(offer.getSlaveId());

            ByteString data = ByteString.copyFrom(taskConfigJSON.getBytes(Charset.defaultCharset()));
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
                    .setExecutor(executorInfo).setData(data).build();
        }
    }
}
