package com.github.learntocode2013;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class DemoMutableSetElems {
  private static final Logger logger = Logger.getLogger(DemoMutableSetElems.class.getName());
  public static void main(String[] args){
    var tasks = new HashSet<Task>(Set.of(new CodingTask("code ui", Duration.ofDays(2))));
    var myProject = new Project("Personal_Finance_App", tasks);
    var projectSet = new HashSet<Project>(Set.of(myProject));
    if(projectSet.contains(myProject)) {
      logger.info(() -> "First assertion passed");
    } else {
      throw new RuntimeException("First assertion failed");
    }
    logger.info(() -> "Hashcode before mutation: " + myProject.hashCode());
    myProject.addTask(new CodingTask("Code backend", Duration.ofDays(3)));
    if (!projectSet.contains(myProject)) {
      logger.info(() -> "Hashcode after mutation: " + myProject.hashCode());
      logger.info(() -> "Second assertion passed pointing to hashcode difference");
    }else {
      throw new RuntimeException("Second assertion failed");
    }
  }
}
