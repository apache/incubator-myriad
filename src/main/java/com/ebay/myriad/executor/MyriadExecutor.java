package com.ebay.myriad.executor;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos.*;
import org.apache.mesos.Protos.TaskStatus.Builder;

import java.io.IOException;

public class MyriadExecutor implements Executor {
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

    private SlaveInfo slaveInfo;

    private Process process;

    /*
    make_cgroups_dir = Process(
  name = 'make_cgroups_dir',
  cmdline = "MY_TASK_ID=`pwd | awk -F'/' '{ print $(NF-1) }'` && echo %s && echo 'hadoop' | sudo -S chown -R root:root %s && echo 'hadoop' | sudo -S chmod -R 777 %s && mkdir -p %s && echo 'hadoop' | sudo -S chown -R root:root %s && echo 'hadoop' | sudo -S chmod -R 777 %s" % (CGROUP_DIR_NM, CGROUP_DIR_TASK, CGROUP_DIR_TASK, CGROUP_DIR_NM, CGROUP_DIR_TASK, CGROUP_DIR_TASK)
)
     */
//    private static final String MAKE_CGROUPS_DIR = "";

  //cmdline = "MY_TASK_ID=`pwd | awk -F'/' '{ print $(NF-1) }'` && echo 'hadoop' | sudo -S sed -i \"s@mesos.*/hadoop-yarn@mesos/$MY_TASK_ID/hadoop-yarn@g\" /usr/local/hadoop/etc/hadoop/yarn-site.xml"
//    private static final String CONFIGURE_CGROUPS = "";

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
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "$YARN_HOME/bin/yarn nodemanager");
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return processBuilder;
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
