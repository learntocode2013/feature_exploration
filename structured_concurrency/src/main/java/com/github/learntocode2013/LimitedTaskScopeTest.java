package com.github.learntocode2013;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

public class LimitedTaskScopeTest {
  public static void main(String[] args){
    try(var scope = new LimitingTaskScope<>(2)){
      IntStream.rangeClosed(0, 10).forEach(i -> scope.fork(() -> doWork(i)));
      scope.join();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    }
  }

  private static Void doWork(int i) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
    System.out.println("Task-" + i + " started at " + LocalTime.now().format(formatter));
    try{
      Thread.sleep(Duration.ofSeconds(2));
      System.out.println("Task-" + i + " finished at " + LocalTime.now().format(formatter));
      return null;
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    }
  }
}
