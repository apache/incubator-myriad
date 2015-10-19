package com.ebay.myriad.scheduler;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to keep map of the constraints
 *
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
