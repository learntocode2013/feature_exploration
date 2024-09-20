package com.github.learntocode2013;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class Project {
  @Getter
  private final String name;
  private final NavigableSet<Task> tasks;
  private Duration totalDuration = Duration.ZERO;

  public Project(String name, Collection<Task> tasks) {
    this.name  = name;
    this.tasks = new TreeSet<>(tasks);
    this.totalDuration = tasks
        .stream()
        .map(Task::duration)
        .reduce(Duration.ZERO, Duration::plus);
  }

  public void addTask(Task t) {
    tasks.add(t);
    totalDuration = totalDuration.plus(t.duration());
  }

  public void removeTask(Task t) {
    tasks.remove(t);
    totalDuration = totalDuration.minus(t.duration());
  }

  public Set<Task> getTasks() {
    return Collections.unmodifiableSet(tasks);
  }
}
