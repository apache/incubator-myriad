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
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.*;
import org.apache.mesos.Protos.CommandInfo.URI;
import org.apache.mesos.Protos.Value.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.nio.charset.Charset;
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
        private static final String YARN_DEFAULT_GROUP = "hadoop";
        private static final String YARN_RESOURCEMANAGER_HOSTNAME = "yarn.resourcemanager.hostname";
        private static final String YARN_RESOURCEMANAGER_WEBAPP_ADDRESS = "yarn.resourcemanager.webapp.address";
        private static final String YARN_RESOURCEMANAGER_WEBAPP_HTTPS_ADDRESS = "yarn.resourcemanager.webapp.https.address";
        private static final String YARN_HTTP_POLICY = "yarn.http.policy";
        private static final String YARN_HTTP_POLICY_HTTP_ONLY = "HTTP_ONLY";
        private static final String YARN_HTTP_POLICY_HTTPS_ONLY = "HTTPS_ONLY";

        private static final Logger LOGGER = LoggerFactory.getLogger(NMTaskFactoryImpl.class);
        private MyriadConfiguration cfg;
        private TaskUtils taskUtils;

        @Inject
        public NMTaskFactoryImpl(MyriadConfiguration cfg, TaskUtils taskUtils) {
            this.cfg = cfg;
            this.taskUtils = taskUtils;
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
                return "http://" + address + "/conf";
            } else {
                String address = conf.get(YARN_RESOURCEMANAGER_WEBAPP_ADDRESS);
                if (address == null || address.isEmpty()) {
                    address = conf.get(YARN_RESOURCEMANAGER_HOSTNAME) + ":8088";
                }
                return "http://" + address + "/conf";
            }
        }
        private Protos.CommandInfo getCommandInfo() {
            MyriadExecutorConfiguration myriadExecutorConfiguration = cfg.getMyriadExecutorConfiguration();
            CommandInfo.Builder commandInfo = CommandInfo.newBuilder();
            if (myriadExecutorConfiguration.getNodeManagerUri().isPresent()) {
            /*
            Overall this is messier than I'd like due to Mesos 1760. We can't let mesos untar the distribution,
            since it will change the permissions.  Instead we simply download the tarball and execute tar -xvpf.
            We also pull the config from the resource manager and put them in the conf dir.  This is also why we need
            frameWorkSuperUser
            */

                LOGGER.info("Using remote distribution");

                String executorCmdPrefix = "export CAPSULE_CACHE_DIR=`pwd`;echo $CAPSULE_CACHE_DIR; ";
                if (cfg.getFrameworkUser().isPresent()) {
                    executorCmdPrefix += "sudo -E -u " + cfg.getFrameworkUser().get() + " -H ";
                }
                executorCmdPrefix += "java -Dcapsule.log=verbose -jar ";

                String nmURI = myriadExecutorConfiguration.getNodeManagerUri().get();

                //todo(DarinJ) support other compression, as this is a temp fix for Mesos 1760 may not get to it.
                String tarCmd = "sudo tar -zxpf " + getFileName(nmURI);

                String chownCmd = "";
                if (cfg.getFrameworkUser().isPresent()) {
                    chownCmd += "sudo chown " + cfg.getFrameworkUser().get() + " .";
                }

                String configCopyCmd = "cp conf " + cfg.getYarnEnvironment().get("YARN_HOME") + "/etc/hadoop/yarn-site.xml";

                String cmd = tarCmd + "&&" + configCopyCmd + "&&" + chownCmd + "&&" + executorCmdPrefix + myriadExecutorConfiguration.getPath();

                //get configs directly from resource manager
                URI configURI = URI.newBuilder().setValue(getConfigurationUrl()).build();
                //We're going to extract ourselves, so setExtract is false
                URI executorURI = URI.newBuilder().setValue(nmURI).setExecutable(true).build();
                commandInfo.addUris(configURI).addUris(executorURI).setUser(cfg.getFrameworkSuperUser().or(""))
                        .setValue("echo \"" + cmd + "\";" + cmd);
            } else {
                String cmdPrefix = " export CAPSULE_CACHE_DIR=`pwd` ;" +
                        "echo $CAPSULE_CACHE_DIR; java -Dcapsule.log=verbose -jar ";
                String executorPath = myriadExecutorConfiguration.getPath();
                String cmd = cmdPrefix + getFileName(executorPath);
                URI executorURI = URI.newBuilder().setValue(executorPath)
                        .setExecutable(true).build();
                commandInfo.addUris(executorURI).setUser(cfg.getFrameworkUser().or(""))
                        .setValue("echo \"cmd\";" + cmd);
            }
            return commandInfo.build();
        }

        @Override
        public TaskInfo createTask(Offer offer, TaskID taskId, NodeTask nodeTask) {
            Objects.requireNonNull(offer, "Offer should be non-null");
            Objects.requireNonNull(nodeTask, "NodeTask should be non-null");

            NMProfile profile = nodeTask.getProfile();
            NMTaskConfig nmTaskConfig = new NMTaskConfig();
            nmTaskConfig.setAdvertisableCpus(profile.getCpus());
            nmTaskConfig.setAdvertisableMem(profile.getMemory());
            NodeManagerConfiguration nodeManagerConfiguration = this.cfg.getNodeManagerConfiguration();
            nmTaskConfig.setJvmOpts(nodeManagerConfiguration.getJvmOpts().orNull());
            nmTaskConfig.setCgroups(nodeManagerConfiguration.getCgroups().or(Boolean.FALSE));
            nmTaskConfig.setYarnEnvironment(cfg.getYarnEnvironment());
            nmTaskConfig.setRemoteDistribution(cfg.getMyriadExecutorConfiguration().getNodeManagerUri().isPresent());

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

            Scalar taskMemory = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getTaskMemory(profile))
                    .build();
            Scalar taskCpus = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getTaskCpus(profile))
                    .build();
            Scalar executorMemory = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getExecutorMemory())
                    .build();
            Scalar executorCpus = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getExecutorCpus())
                    .build();


            CommandInfo commandInfo = getCommandInfo();

            ExecutorID executorId = Protos.ExecutorID.newBuilder()
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

            // TODO (mohit): Configure ports for multi-tenancy
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
                    .setExecutor(executorInfo).setData(data).build();
        }
    }
}
