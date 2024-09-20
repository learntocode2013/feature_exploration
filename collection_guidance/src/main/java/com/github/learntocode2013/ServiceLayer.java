package com.github.learntocode2013;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServiceLayer {
  private Map<String, Set<Task>> projects = new HashMap<>();
  private Map<String, Duration> durations = new HashMap<>();

  public void addTask(String projectName, Task task) {
    projects.merge(projectName, Set.of(task),
        (oldValue, newValue) -> { oldValue.addAll(newValue); return oldValue; });
    durations.merge(projectName, task.duration(), Duration::plus);
  }

  public Duration getTotalDuration(String projectName) {
    return durations.get(projectName);
  }
}
