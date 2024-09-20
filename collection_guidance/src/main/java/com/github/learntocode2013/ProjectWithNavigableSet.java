package com.github.learntocode2013;


import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

public record ProjectWithNavigableSet(String name, NavigableSet<Task> tasks) {
  public ProjectWithNavigableSet(String name, NavigableSet<Task> tasks) {
    this.name = name;
    var defensiveCopy = new TreeSet<Task>(tasks);
    this.tasks = Collections.unmodifiableNavigableSet(defensiveCopy);
  }

  public static void main(String[] args){
    var tasks =
        new TreeSet<Task>(
            Arrays.asList(
                new CodingTask("code ui", Duration.ofDays(1)),
                new CodingTask("code db", Duration.ofDays(10))));
    System.out.println("Tasks at hand: " + tasks);
    var project = new ProjectWithNavigableSet("Aim for Immutability of records", tasks);
    System.out.println("Project tasks before mutation: " + project.tasks());
    project.tasks().add(new PhoneTask("Call vendor", "xxxx", Duration.ofMinutes(10)));
    System.out.println("Project tasks after mutation: " + project.tasks());
  }
}
