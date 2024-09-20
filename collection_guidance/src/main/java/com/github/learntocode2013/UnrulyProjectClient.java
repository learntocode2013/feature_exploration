package com.github.learntocode2013;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class UnrulyProjectClient {
  private static final Logger logger = Logger.getLogger(UnrulyProjectClient.class.getName());
  public static void main(String[] args){
    Set<Task> tasks = new HashSet<>();
    tasks.add(new CodingTask("Engine", Duration.ofDays(5)));
    tasks.add(new CodingTask("Orchestration", Duration.ofDays(10)));

    var project1 = new Project("Recommendation engine", tasks);
    var project2 = new Project("Shipping workflow", tasks);

    logger.info(() -> "Project-1 tasks b4 mod: " + project1.getTasks());
    logger.info(() -> "Project-2 tasks b4 mod: " + project2.getTasks());

    project1.removeTask(project1.getTasks().toArray(new Task[0])[0]);
    logger.info("\n\n");

    logger.info(() -> "Project-1 tasks after mod: " + project1.getTasks());
    logger.info(() -> "Project-2 tasks after mod: " + project2.getTasks());

    logger.info("\n\n");

    // Not expected from a client
    var pTasks = project1.getTasks();
    pTasks.add(new PhoneTask("Marketing call", "999999", Duration.ofMinutes(5)));
    logger.info(() -> "Project-1 tasks after second mod: " + project1.getTasks());

  }
}
