package com.github.learntocode2013;

import java.time.Duration;

public interface Task extends Comparable<Task> {
  default int compareTo(Task t) {
    return duration().compareTo(t.duration());
  }
  Duration duration();
}
