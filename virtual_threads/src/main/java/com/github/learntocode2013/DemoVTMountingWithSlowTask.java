package com.github.learntocode2013;

import java.time.Duration;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class DemoVTMountingWithSlowTask {
  private final Logger logger = Logger.getLogger(DemoVTMountingWithSlowTask.class.getName());
  private final Runnable slowTask = () -> {
    logger.info(() -> Thread.currentThread().toString() + " | Working...");
    logger.info(() -> Thread.currentThread().toString() + " | Blocking...");
    while(true) {
      if(Thread.currentThread().isInterrupted()) {
        Thread.currentThread().interrupt();
        break;
      }
    }
//    try {
//      Thread.sleep(Duration.ofSeconds(5));
//    } catch (InterruptedException iex) {}

    logger.info(() -> Thread.currentThread().toString() + " | work done...");
  };
  private final Runnable fastTask = () -> {
    logger.info(() -> Thread.currentThread().toString() + " | Working...");
    logger.info(() -> Thread.currentThread().toString() + " | Blocking...");
    try {
      Thread.sleep(Duration.ofSeconds(1));
    } catch (InterruptedException iex) {}
    logger.info(() -> Thread.currentThread().toString() + " | work done...");
  };

  public static void main(String[] args){
    // Limit the carrier thread parallelism
    System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "2");
    new DemoVTMountingWithSlowTask().execute();
  }

  @SneakyThrows
  private void execute() {
    Thread slowVt = Thread.ofVirtual().name("slow-",0).start(slowTask);
    Thread fastVt = Thread.ofVirtual().name("fast-",0).start(fastTask);
    slowVt.join(Duration.ofSeconds(6));
    fastVt.join();
  }
}
