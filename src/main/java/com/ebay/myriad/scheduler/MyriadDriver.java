/**
 * Copyright 2012-2014 eBay Software Foundation, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ebay.myriad.scheduler;

import com.ebay.myriad.configuration.MyriadConfiguration;
import com.ebay.myriad.state.SchedulerState;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang.StringUtils;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos.Credential;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.FrameworkInfo;
import org.apache.mesos.Protos.FrameworkInfo.Builder;
import org.apache.mesos.Protos.Status;
import org.apache.mesos.Protos.TaskID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * The driver for the Myriad scheduler
 */
public class MyriadDriver {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyriadDriver.class);

    private final MesosSchedulerDriver driver;

    @Inject
    public MyriadDriver(final MyriadScheduler scheduler,
                        final MyriadConfiguration cfg,
                        final SchedulerState schedulerState) {
        Builder frameworkInfoBuilder = FrameworkInfo.newBuilder().setUser("")
                .setName(cfg.getFrameworkName())
                .setCheckpoint(cfg.isCheckpoint())
                .setFailoverTimeout(cfg.getFrameworkFailoverTimeout());

        if (StringUtils.isNotEmpty(cfg.getFrameworkRole())) {
            frameworkInfoBuilder.setRole(cfg.getFrameworkRole());
        }

        FrameworkID frameworkId;
        try {
            frameworkId = schedulerState.getMyriadState().getFrameworkID();
            if (frameworkId != null) {
                LOGGER.info("Attempting to re-register with frameworkId: {}", frameworkId.getValue());
                frameworkInfoBuilder.setId(frameworkId);
            }
        } catch (InterruptedException | ExecutionException | InvalidProtocolBufferException e) {
            LOGGER.error("Error fetching frameworkId", e);
            throw new RuntimeException(e);
        }

        String mesosAuthenticationPrincipal = cfg.getMesosAuthenticationPrincipal();
        String mesosAuthenticationSecretFilename = cfg.getMesosAuthenticationSecretFilename();
        if (StringUtils.isNotEmpty(mesosAuthenticationPrincipal)) {
            frameworkInfoBuilder.setPrincipal(mesosAuthenticationPrincipal);

            Credential.Builder credentialBuilder = Credential.newBuilder();
            credentialBuilder.setPrincipal(mesosAuthenticationPrincipal);
            if (StringUtils.isNotEmpty(mesosAuthenticationSecretFilename)) {
                try  {
                    credentialBuilder.setSecret(ByteString.readFrom(new FileInputStream(mesosAuthenticationSecretFilename)));
                } catch (FileNotFoundException ex) {
                    LOGGER.error("Mesos authentication secret file was not found", ex);
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    LOGGER.error("Error reading Mesos authentication secret file", ex);
                    throw new RuntimeException(ex);
                }
            }
            this.driver = new MesosSchedulerDriver(scheduler, frameworkInfoBuilder.build(), cfg.getMesosMaster(), credentialBuilder.build());
        } else {
            this.driver = new MesosSchedulerDriver(scheduler,
                    frameworkInfoBuilder.build(), cfg.getMesosMaster());
        }
    }

    public Status start() {
        LOGGER.info("Starting driver");
        Status status = driver.start();
        LOGGER.info("Driver started with status: {}", status);
        return status;
    }

    public Status kill(final TaskID taskId) {
        LOGGER.info("Killing task {}", taskId);
        Status status = driver.killTask(taskId);
        LOGGER.info("Task {} killed with status: {}", taskId, status);
        return status;
    }

    public Status abort() {
        LOGGER.info("Aborting driver");
        Status status = driver.abort();
        LOGGER.info("Driver aborted with status: {}", status);
        return status;
    }
}
