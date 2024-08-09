package com.github.learntocode2013;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class LimitVTWithSemaphore {
  private static final Logger logger = Logger.getLogger(LimitVTWithSemaphore.class.getName());
  private static final int NUMBER_OF_TASKS   = 15;
  private static final int NUMBER_OF_THREADS = 3;
  private final Runnable TASK = () -> {
    logger.info(() -> Thread.currentThread().toString() + " running some task...");
    try {
      Thread.sleep(Duration.ofSeconds(5));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    logger.info(() -> Thread.currentThread().toString() + " completed the task...");
  };

  public static void main(String[] args){
    var subject = new LimitVTWithSemaphore();
    subject.capVirtualThreads();
  }

  @SneakyThrows
  public void capVirtualThreads() {
    final Semaphore vtPermits = new Semaphore(NUMBER_OF_THREADS);
    List<Thread> threads = new ArrayList<>();
    for(var i = 0; i < NUMBER_OF_TASKS; i++) {
      var vt = Thread.ofVirtual().start(() -> {
        try {
          vtPermits.acquire();
          logger.info(() -> Thread.currentThread().toString()
              + " got a permit. Available permits: " + vtPermits.availablePermits());
          TASK.run();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        } finally{
          vtPermits.release();
        }
      });
      threads.add(vt);
    }

    for (var thread : threads) {
      thread.join();
    }
  }
}

