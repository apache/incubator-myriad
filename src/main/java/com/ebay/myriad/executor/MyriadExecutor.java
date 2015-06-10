package com.ebay.myriad.executor;

import com.google.gson.Gson;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.SlaveInfo;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.apache.mesos.Protos.TaskInfo;
import org.apache.mesos.Protos.TaskState;
import org.apache.mesos.Protos.TaskStatus;
import org.apache.mesos.Protos.TaskStatus.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Myriad's Executor
 */
public class MyriadExecutor implements Executor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadExecutor.class);
    public static final String ENV_YARN_NODEMANAGER_OPTS = "YARN_NODEMANAGER_OPTS";

    /**
     * YARN container executor class.
     */
    public static final String KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS = "yarn.nodemanager.container-executor.class";

    // TODO (mohit): Should it be configurable ?
    public static final String VAL_YARN_NM_CONTAINER_EXECUTOR_CLASS = "org.apache.hadoop.yarn.server.nodemanager.LinuxContainerExecutor";

    public static final String DEFAULT_YARN_NM_CONTAINER_EXECUTOR_CLASS = "org.apache.hadoop.yarn.server.nodemanager.DefaultContainerExecutor";

    /**
     * YARN class to help handle LCE resources
     */
    public static final String KEY_YARN_NM_LCE_RH_CLASS = "yarn.nodemanager.linux-container-executor.resources-handler.class";

    // TODO (mohit): Should it be configurable ?
    public static final String VAL_YARN_NM_LCE_RH_CLASS = "org.apache.hadoop.yarn.server.nodemanager.util.CgroupsLCEResourcesHandler";

    public static final String KEY_YARN_NM_LCE_CGROUPS_HIERARCHY = "yarn.nodemanager.linux-container-executor.cgroups.hierarchy";

    public static final String KEY_YARN_NM_LCE_CGROUPS_MOUNT = "yarn.nodemanager.linux-container-executor.cgroups.mount";

    public static final String KEY_YARN_NM_LCE_CGROUPS_MOUNT_PATH = "yarn.nodemanager.linux-container-executor.cgroups.mount-path";

    public static final String KEY_YARN_NM_LCE_GROUP = "yarn.nodemanager.linux-container-executor.group";

    public static final String KEY_YARN_NM_LCE_PATH = "yarn.nodemanager.linux-container-executor.path";

    public static final String KEY_YARN_HOME = "yarn.home";

    public static final String KEY_NM_RESOURCE_CPU_VCORES = "nodemanager.resource.cpu-vcores";

    public static final String KEY_NM_RESOURCE_MEM_MB = "nodemanager.resource.memory-mb";

    /**
     * Allot 10% more memory to account for JVM overhead.
     */
    public static final double JVM_OVERHEAD = 0.1;

    /**
     * Default -Xmx for executor JVM.
     */

    public static final double DEFAULT_JVM_MAX_MEMORY_MB = 256;
    /**
     * Default cpus for executor JVM.
     */
    public static final double DEFAULT_CPUS = 0.2;

    public static final Gson GSON = new Gson();

    private static final String PROPERTY_FORMAT = "-D%s=%s ";

    private SlaveInfo slaveInfo;

    private Process process;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Starting MyriadExecutor...");
        MesosExecutorDriver driver = new MesosExecutorDriver(new MyriadExecutor());
        System.exit(driver.run() == Status.DRIVER_STOPPED ? 0 : 1);
    }

    @Override
    public void registered(ExecutorDriver driver,
                           ExecutorInfo executorInfo,
                           FrameworkInfo frameworkInfo,
                           SlaveInfo slaveInfo) {
        LOGGER.debug("Registered ", executorInfo, " for framework ", frameworkInfo, " on mesos slave ", slaveInfo);
        this.slaveInfo = slaveInfo;
    }

    @Override
    public void reregistered(ExecutorDriver driver, SlaveInfo slaveInfo) {
        LOGGER.debug("ReRegistered");
    }

    @Override
    public void disconnected(ExecutorDriver driver) {
        LOGGER.info("Disconnected");
    }

    @Override
    public void launchTask(final ExecutorDriver driver, final TaskInfo task) {
        new Thread(new Runnable() {
            public void run() {
                Builder statusBuilder = TaskStatus.newBuilder()
                        .setTaskId(task.getTaskId());
                try {
                    NMTaskConfig taskConfig = GSON.fromJson(task.getData().toStringUtf8(), NMTaskConfig.class);
                    LOGGER.info("TaskConfig: ", taskConfig);
                    ProcessBuilder processBuilder = buildProcessBuilder(task, taskConfig);
                    MyriadExecutor.this.process = processBuilder.start();

                    int waitFor = MyriadExecutor.this.process.waitFor();

                    if (waitFor == 0) {
                        statusBuilder.setState(TaskState.TASK_FINISHED);
                    } else {
                        statusBuilder.setState(TaskState.TASK_FAILED);
                    }
                } catch (InterruptedException | IOException e) {
                    LOGGER.error("launchTask", e);
                    statusBuilder.setState(TaskState.TASK_FAILED);
                } catch (RuntimeException e) {
                    LOGGER.error("launchTask", e);
                    statusBuilder.setState(TaskState.TASK_FAILED);
                    throw e;
                } finally {
                    driver.sendStatusUpdate(statusBuilder.build());
                }
            }
        }).start();

        TaskStatus status = TaskStatus.newBuilder()
                .setTaskId(task.getTaskId())
                .setState(TaskState.TASK_RUNNING)
                .build();
        driver.sendStatusUpdate(status);
    }

    private ProcessBuilder buildProcessBuilder(TaskInfo task, NMTaskConfig taskConfig) {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "$YARN_HOME/bin/yarn nodemanager");

        Map<String, String> environment = processBuilder.environment();

        Map<String, String> yarnEnvironmentMap = taskConfig.getYarnEnvironment();
        if (yarnEnvironmentMap != null) {
            for (Map.Entry<String, String> yarnEnvironment : yarnEnvironmentMap.entrySet()) {
                environment.put(yarnEnvironment.getKey(), yarnEnvironment.getValue());
            }
        }

        String envNMOptions = getNMOpts(taskConfig);
        LOGGER.info(ENV_YARN_NODEMANAGER_OPTS, ": ", envNMOptions);

        if (environment.containsKey(ENV_YARN_NODEMANAGER_OPTS)) {
            String existingOpts = environment.get(ENV_YARN_NODEMANAGER_OPTS);
            environment.put(ENV_YARN_NODEMANAGER_OPTS, existingOpts + " " + envNMOptions);
        } else {
            environment.put(ENV_YARN_NODEMANAGER_OPTS, envNMOptions);
        }

        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return processBuilder;
    }

    private void makeWritable(String path) {
        File file = new File(path);
        if (!file.setWritable(true, false)) {
            LOGGER.error(path, " is not writable");
        }
    }

    private String getNMOpts(NMTaskConfig taskConfig) {
        String envNMOptions = "";

        // If cgroups are enabled then configure
        if (taskConfig.getCgroups()) {
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS, VAL_YARN_NM_CONTAINER_EXECUTOR_CLASS);
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_RH_CLASS, VAL_YARN_NM_LCE_RH_CLASS);

            String containerId = getContainerId();

            makeWritable("/sys/fs/cgroup/cpu/mesos/" + containerId);

            // TODO: Configure hierarchy
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_CGROUPS_HIERARCHY, "mesos/" + containerId);
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_CGROUPS_MOUNT, "true");
            // TODO: Make it configurable
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_CGROUPS_MOUNT_PATH, "/sys/fs/cgroup");
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_GROUP, "root");
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_HOME, taskConfig.getYarnEnvironment().get("YARN_HOME"));
        } else {
            // Otherwise configure to use Default
            envNMOptions += String.format(PROPERTY_FORMAT, KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS, DEFAULT_YARN_NM_CONTAINER_EXECUTOR_CLASS);
        }
        envNMOptions += String.format(PROPERTY_FORMAT, KEY_NM_RESOURCE_CPU_VCORES, taskConfig.getAdvertisableCpus() + "");
        envNMOptions += String.format(PROPERTY_FORMAT, KEY_NM_RESOURCE_MEM_MB, taskConfig.getAdvertisableMem() + "");
        return envNMOptions;
    }

    public String getContainerId() {
        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
        String[] split = cwd.split("/");
        return split[split.length - 1];
    }

    @Override
    public void killTask(ExecutorDriver driver, TaskID taskId) {
        LOGGER.debug("KillTask received for taskId: " + taskId.getValue());
        this.process.destroy();
        TaskStatus status = TaskStatus.newBuilder()
                .setTaskId(taskId)
                .setState(TaskState.TASK_KILLED)
                .build();
        driver.sendStatusUpdate(status);
    }

    @Override
    public void frameworkMessage(ExecutorDriver driver, byte[] data) {
        LOGGER.info("Framework message received: ", new String(data, Charset.defaultCharset()));
    }

    @Override
    public void shutdown(ExecutorDriver driver) {
        LOGGER.debug("Shutdown");
    }

    @Override
    public void error(ExecutorDriver driver, String message) {
        LOGGER.error("Error message: " + message);
    }
}
