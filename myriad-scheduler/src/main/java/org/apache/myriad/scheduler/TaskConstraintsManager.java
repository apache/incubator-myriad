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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myriad.scheduler;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to keep map of the constraints
 */
public class TaskConstraintsManager {

  /**
   * Since all the additions will happen during init time, there is no need to make this map Concurrent
   * if/when later on it will change we may need to change HashMap to Concurrent one
   */
  private Map<String, TaskConstraints> taskConstraintsMap = new HashMap<>();

  public TaskConstraints getConstraints(String taskPrefix) {
    return taskConstraintsMap.get(taskPrefix);
  }

  public void addTaskConstraints(final String taskPrefix, final TaskConstraints taskConstraints) {
    if (taskConstraints != null) {
      taskConstraintsMap.put(taskPrefix, taskConstraints);
    }
  }

  public boolean exists(String taskPrefix) {
    return taskConstraintsMap.containsKey(taskPrefix);
  }
}
