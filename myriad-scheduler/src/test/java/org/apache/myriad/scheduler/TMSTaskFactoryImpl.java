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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myriad.scheduler;

import org.apache.mesos.Protos.*;
import org.apache.myriad.configuration.MyriadConfiguration;
import org.apache.myriad.state.NodeTask;

import javax.inject.Inject;

/**
 * Test implementation of TaskFactory
 *
 */
public class TMSTaskFactoryImpl implements TaskFactory {

  private MyriadConfiguration cfg;
  private TaskUtils taskUtils;

  @Inject
  public TMSTaskFactoryImpl(MyriadConfiguration cfg, TaskUtils taskUtils) {
    this.setCfg(cfg);
    this.setTaskUtils(taskUtils);
  }

  @Override
  public TaskInfo createTask(Offer offer, FrameworkID frameworkId,
                             TaskID taskId, NodeTask nodeTask) {
    return null;
  }

  public MyriadConfiguration getCfg() {
    return cfg;
  }

  public void setCfg(MyriadConfiguration cfg) {
    this.cfg = cfg;
  }

  public TaskUtils getTaskUtils() {
    return taskUtils;
  }

  public void setTaskUtils(TaskUtils taskUtils) {
    this.taskUtils = taskUtils;
  }

  @Override
  public ExecutorInfo getExecutorInfoForSlave(FrameworkID frameworkId,
                                              Offer offer, CommandInfo commandInfo) {
    return null;
  }
}
