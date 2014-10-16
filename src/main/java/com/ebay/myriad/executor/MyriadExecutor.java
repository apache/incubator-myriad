package com.ebay.myriad.executor;

import com.google.gson.Gson;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos.*;
import org.apache.mesos.Protos.TaskStatus.Builder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class MyriadExecutor implements Executor {
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
        System.out.println("Starting MyriadExecutor...");
        MesosExecutorDriver driver = new MesosExecutorDriver(
                new MyriadExecutor());
        System.exit(driver.run() == Status.DRIVER_STOPPED ? 0 : 1);
    }

    @Override
    public void registered(ExecutorDriver driver, ExecutorInfo executorInfo,
                           FrameworkInfo frameworkInfo, SlaveInfo slaveInfo) {
        System.out.println("Registered "+ executorInfo +" for framework " + frameworkInfo + " on mesos slave " + slaveInfo);
        this.slaveInfo = slaveInfo;
    }

    @Override
    public void reregistered(ExecutorDriver driver, SlaveInfo slaveInfo) {
        System.out.println("ReRegistered");
    }

    @Override
    public void disconnected(ExecutorDriver driver) {
        System.out.println("Disconnected");
    }

    @Override
    public void launchTask(final ExecutorDriver driver, final TaskInfo task) {
        new Thread(new Runnable() {
            public void run() {
                Builder statusBuilder = TaskStatus.newBuilder().setTaskId(
                        task.getTaskId());
                try {
                    NMTaskConfig taskConfig = GSON.fromJson(task.getData()
                            .toStringUtf8(), NMTaskConfig.class);
                    System.out.println("TaskConfig: " + taskConfig);
                    ProcessBuilder processBuilder = buildProcessBuilder(task,
                            taskConfig);
                    MyriadExecutor.this.process = processBuilder.start();

                    int waitFor = MyriadExecutor.this.process.waitFor();

                    if (waitFor == 0) {
                        statusBuilder.setState(TaskState.TASK_FINISHED);
                    } else {
                        statusBuilder.setState(TaskState.TASK_FAILED);
                    }
                } catch (InterruptedException | IOException e) {
                    System.out.println(e);
                    statusBuilder.setState(TaskState.TASK_FAILED);
                } catch (RuntimeException e) {
                    System.out.println(e);
                    statusBuilder.setState(TaskState.TASK_FAILED);
                    throw e;
                } finally {
                    driver.sendStatusUpdate(statusBuilder.build());
                }
            }
        }).start();
        TaskStatus status = TaskStatus.newBuilder().setTaskId(task.getTaskId())
                .setState(TaskState.TASK_RUNNING).build();
        driver.sendStatusUpdate(status);
    }

    private ProcessBuilder buildProcessBuilder(TaskInfo task,
                                               NMTaskConfig taskConfig) {
        ProcessBuilder processBuilder = new ProcessBuilder("sudo", "-E", "-u", taskConfig.getUser(), "-H", "bash", "-c", "$YARN_HOME/bin/yarn nodemanager");

        Map<String, String> environment = processBuilder.environment();

        Map<String, String> yarnEnvironment = taskConfig.getYarnEnvironment();
        if (yarnEnvironment != null) {
            Set<String> keys = yarnEnvironment.keySet();
            for (String key : keys) {
                String val = yarnEnvironment.get(key);
                environment.put(key, val);
            }
        }

        String ENV_NM_OPTS = getNMOpts(taskConfig);
        System.out.printf("%s: %s", ENV_YARN_NODEMANAGER_OPTS, ENV_NM_OPTS);

        if (environment.containsKey(ENV_YARN_NODEMANAGER_OPTS)) {
            String existingOpts = environment.get(ENV_YARN_NODEMANAGER_OPTS);
            environment.put(ENV_YARN_NODEMANAGER_OPTS, existingOpts + " " + ENV_NM_OPTS);
        } else {
            environment.put(ENV_YARN_NODEMANAGER_OPTS, ENV_NM_OPTS);
        }
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return processBuilder;
    }

    private void makeWritable(String path) {
        File file = new File(path);
        file.setWritable(true, false);
    }

    private String getNMOpts(NMTaskConfig taskConfig) {
        String ENV_NM_OPTS = "";

        // If cgroups are enabled then configure
        if (taskConfig.getCgroups()) {
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS, VAL_YARN_NM_CONTAINER_EXECUTOR_CLASS);
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_RH_CLASS, VAL_YARN_NM_LCE_RH_CLASS);

            String containerId = getContainerId();

            makeWritable("/sys/fs/cgroup/cpu/mesos/" + containerId);

            // TODO: Configure hierarchy
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_CGROUPS_HIERARCHY, "mesos/" + containerId);
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_CGROUPS_MOUNT, "true");
            // TODO: Make it configurable
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_CGROUPS_MOUNT_PATH, "/sys/fs/cgroup");
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_LCE_GROUP, "root");
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_HOME, taskConfig.getYarnEnvironment().get("YARN_HOME"));
        } else {
            // Otherwise configure to use Default
            ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_YARN_NM_CONTAINER_EXECUTOR_CLASS, DEFAULT_YARN_NM_CONTAINER_EXECUTOR_CLASS);
        }
        ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_NM_RESOURCE_CPU_VCORES, taskConfig.getAdvertisableCpus()+"");
        ENV_NM_OPTS += String.format(PROPERTY_FORMAT, KEY_NM_RESOURCE_MEM_MB, taskConfig.getAdvertisableMem()+"");
        return ENV_NM_OPTS;
    }

    public String getContainerId() {
        String cwd = Paths.get(".").toAbsolutePath().normalize().toString();
        String[] split = cwd.split("/");
        return split[split.length - 1];
    }

    @Override
    public void killTask(ExecutorDriver driver, TaskID taskId) {
        System.out.println("KillTask received for taskId: " + taskId.getValue());
        this.process.destroy();
        TaskStatus status = TaskStatus.newBuilder().setTaskId(taskId)
                .setState(TaskState.TASK_KILLED).build();
        driver.sendStatusUpdate(status);
    }

    @Override
    public void frameworkMessage(ExecutorDriver driver, byte[] data) {
        System.out.println("Framework message received: " + new String(data));
    }

    @Override
    public void shutdown(ExecutorDriver driver) {
        System.out.println("Shutdown");
    }

    @Override
    public void error(ExecutorDriver driver, String message) {
        System.out.println("Error message: " + message);
    }
}
