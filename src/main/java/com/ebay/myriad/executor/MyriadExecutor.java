package com.ebay.myriad.executor;

import java.io.IOException;
import java.util.Objects;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class MyriadExecutor implements Executor {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyriadExecutor.class);

	private static final String NM_COMMAND = "sudo -H -u %s bash -c '$YARN_HOME/bin/yarn nodemanager'";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private SlaveInfo slaveInfo;
	private Process process;

	@Override
	public void registered(ExecutorDriver driver, ExecutorInfo executorInfo,
			FrameworkInfo frameworkInfo, SlaveInfo slaveInfo) {
		LOGGER.info("Registered {} for framework {} on mesos slave {}",
				executorInfo, frameworkInfo, slaveInfo);
		this.slaveInfo = slaveInfo;
	}

	@Override
	public void reregistered(ExecutorDriver driver, SlaveInfo slaveInfo) {
		LOGGER.info("ReRegistered");
	}

	@Override
	public void disconnected(ExecutorDriver driver) {
		LOGGER.info("Disconnected");
	}

	@Override
	public void launchTask(ExecutorDriver driver, TaskInfo task) {
		new Thread(new Runnable() {
			public void run() {
				try {
					String command = getCommand(NM_COMMAND, task);
					Builder statusBuilder = TaskStatus.newBuilder().setTaskId(
							task.getTaskId());
					MyriadExecutor.this.process = Runtime.getRuntime().exec(
							command);
					int waitFor = MyriadExecutor.this.process.waitFor();

					if (waitFor == 0) {
						statusBuilder.setState(TaskState.TASK_FINISHED);
					} else {
						statusBuilder.setState(TaskState.TASK_FAILED);
					}
					driver.sendStatusUpdate(statusBuilder.build());
				} catch (IOException e) {
					LOGGER.error(e.getMessage(), e);
				} catch (InterruptedException e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}).start();
		TaskStatus status = TaskStatus.newBuilder().setTaskId(task.getTaskId())
				.setState(TaskState.TASK_RUNNING).build();
		driver.sendStatusUpdate(status);
	}

	@Override
	public void killTask(ExecutorDriver driver, TaskID taskId) {
		LOGGER.info("KillTask received for taskId: {}", taskId.getValue());
		this.process.destroyForcibly();
		TaskStatus status = TaskStatus.newBuilder().setTaskId(taskId)
				.setState(TaskState.TASK_KILLED).build();
		driver.sendStatusUpdate(status);
	}

	@Override
	public void frameworkMessage(ExecutorDriver driver, byte[] data) {
		LOGGER.info("Framework message received: {}", new String(data));
	}

	@Override
	public void shutdown(ExecutorDriver driver) {
		LOGGER.info("Shutdown");
	}

	@Override
	public void error(ExecutorDriver driver, String message) {
		LOGGER.error("Error message: {}", message);
	}

	private String getCommand(String commandTemplate, TaskInfo taskInfo) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(commandTemplate),
				"Command template cannot be null or empty");
		Preconditions.checkArgument(Objects.nonNull(taskInfo),
				"TaskInfo cannot be null");
		Preconditions.checkArgument(Objects.nonNull(taskInfo.getData()),
				"Data field cannot be null");

		String taskJson = taskInfo.getData().toStringUtf8();

		NMTaskConfig taskConfig = null;

		String command = null;
		try {
			taskConfig = MAPPER.readValue(taskJson, NMTaskConfig.class);
			command = String.format(NM_COMMAND, taskConfig.getUser());
		} catch (IOException e) {
			LOGGER.error("Error parsing taskJson: {}", taskJson);
		}

		return command;
	}

	public static void main(String[] args) {
		MesosExecutorDriver driver = new MesosExecutorDriver(
				new MyriadExecutor());
		System.exit(driver.run() == Status.DRIVER_STOPPED ? 0 : 1);
	}
}
