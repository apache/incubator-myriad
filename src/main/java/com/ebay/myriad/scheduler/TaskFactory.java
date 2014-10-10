package com.ebay.myriad.scheduler;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.executor.NMTaskConfig;
import com.ebay.myriad.state.NodeTask;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.*;
import org.apache.mesos.Protos.CommandInfo.URI;
import org.apache.mesos.Protos.Value.Scalar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;

public interface TaskFactory {
    TaskInfo createTask(Offer offer, TaskID taskId, NodeTask nodeTask);

    class NMTaskFactoryImpl implements TaskFactory {
        public static final String EXECUTOR_NAME = "myriad_task";
        public static final String EXECUTOR_PREFIX = "myriad_executor";
        private static final Logger LOGGER = LoggerFactory
                .getLogger(NMTaskFactoryImpl.class);
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

        @Override
        public TaskInfo createTask(Offer offer, TaskID taskId, NodeTask nodeTask) {
            Objects.requireNonNull(offer, "Offer should be non-null");
            Objects.requireNonNull(nodeTask, "NodeTask should be non-null");

            NMProfile profile = nodeTask.getProfile();
            NMTaskConfig nmTaskConfig = new NMTaskConfig();
            nmTaskConfig.setAdvertisableCpus(taskUtils.getTaskCpus(profile));
            nmTaskConfig.setAdvertisableMem(taskUtils.getTaskMemory(profile));
            nmTaskConfig.setUser(this.cfg.getNodeManagerConfiguration()
                    .getUser().orNull());
            nmTaskConfig.setJvmOpts(this.cfg.getNodeManagerConfiguration()
                    .getJvmOpts().orNull());

            String taskConfigJSON = new Gson().toJson(nmTaskConfig);

            Scalar taskMemory = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getTaskMemory(profile)).build();
            Scalar taskCpus = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getTaskCpus(profile)).build();
            Scalar executorMemory = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getExecutorMemory()).build();
            Scalar executorCpus = Value.Scalar.newBuilder()
                    .setValue(taskUtils.getExecutorCpus()).build();

            String executorPath = cfg.getMyriadExecutorConfiguration()
                    .getPath();
            URI executorURI = URI.newBuilder().setValue(executorPath)
                    .setExecutable(true).build();
            CommandInfo commandInfo = CommandInfo.newBuilder()
                    .addUris(executorURI).setUser(nmTaskConfig.getUser())
                    .setValue("java -jar " + getFileName(executorPath)).build();

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
                                    .setScalar(executorCpus).build())
                    .addResources(
                            Resource.newBuilder().setName("mem")
                                    .setType(Value.Type.SCALAR)
                                    .setScalar(executorMemory).build())
                    .setExecutorId(executorId).setCommand(commandInfo).build();

            TaskInfo.Builder taskBuilder = TaskInfo.newBuilder()
                    .setName("task-" + taskId.getValue()).setTaskId(taskId)
                    .setSlaveId(offer.getSlaveId());

            // TODO (mohit): Configure ports for multi-tenancy
            ByteString data = ByteString.copyFrom(taskConfigJSON.getBytes());
            return taskBuilder
                    .addResources(
                            Resource.newBuilder().setName("cpus")
                                    .setType(Value.Type.SCALAR)
                                    .setScalar(taskCpus).build())
                    .addResources(
                            Resource.newBuilder().setName("mem")
                                    .setType(Value.Type.SCALAR)
                                    .setScalar(taskMemory).build())
                    .setExecutor(executorInfo).setData(data).build();
        }
    }
}
