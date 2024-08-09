package com.github.learntocode2013;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class PlatformThreadCost {
  private static final Logger logger = Logger.getLogger(PlatformThreadCost.class.getName());
  private static final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();
  private static final Runnable TASK = () -> queue.add(1);

  public static void main(String[] args) throws InterruptedException {
    var subject = new PlatformThreadCost();
    var t1 = new Thread(subject::printTaskCompletionTime_ThreadPerTask);
    var t2 = new Thread(subject::printTaskCompletionTime_ThreadPool);
    var t3 = new Thread(subject::printTaskCompletionTime_SameThread);
    var t4 = new Thread(subject::printTaskCompletionTime_VT);

    t1.start();t2.start();t3.start();t4.start();
    t1.join();t2.join();t3.join();t4.join();
  }

  @SneakyThrows
  private void printTaskCompletionTime_ThreadPerTask() {
    long start = System.nanoTime();
    int runs = 200_000;
    for (var i = 0; i < 3; i++) {
      for (var j = 0; j < runs; j++) {
        new Thread(TASK).start();
      }
      for (var k = 0; k < runs; k++) {
        queue.take();
      }
    }
    long timeTaken = System.nanoTime() - start;
    logger.info(() -> "Time taken to complete a task in a new thread: " + timeTaken/runs + " nano-seconds");
  }

  @SneakyThrows
  private void printTaskCompletionTime_ThreadPool() {
    long start = System.nanoTime();
    int runs = 200_000;
    try(var es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
      for(var i = 0; i < 3; i++) {
        for(var j = 0; j < runs; j++) {
          es.execute(TASK);
        }
        for(var k = 0; k < runs; k++) {
          queue.take();
        }
      }
    }
    long timeTaken = System.nanoTime() - start;
    logger.info(() -> "Time taken to complete a task in thread pool: " + timeTaken/runs + " nano-seconds");
  }

  @SneakyThrows
  private void printTaskCompletionTime_SameThread() {
    long start = System.nanoTime();
    int runs = 200_000;
    for(var i = 0; i < 3; i++) {
      for(var j = 0; j < runs; j++) {
        TASK.run();
      }
      for(var k = 0; k < runs; k++) {
        queue.take();
      }
    }
    long timeTaken = System.nanoTime() - start;
    logger.info(() -> "Time taken to complete a task in the same thread: " + timeTaken/runs + " nano-seconds");
  }

  @SneakyThrows
  private void printTaskCompletionTime_VT() {
    long start = System.nanoTime();
    int runs = 200_000;
    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      for(var i = 0; i < 3; i++) {
        for(var j = 0; j < runs; j++) {
          es.execute(TASK);
        }
        for(var k = 0; k < runs; k++) {
          queue.take();
        }
      }
    }
    long timeTaken = System.nanoTime() - start;
    logger.info(() -> "Time taken to complete a task in virtual thread: " + timeTaken/runs + " nano-seconds");
  }


}
