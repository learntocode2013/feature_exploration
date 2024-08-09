package com.github.learntocode2013;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class ThroughputExample {
  private static final Logger logger = Logger.getLogger(ThroughputExample.class.getName());
  private static final int NUM_OF_TASKS = 10_000;
  private static final Duration AVG_RESPONSE_TIME = Duration.ofMillis(500);
  // Simulate an IO-Bound task
  private static final Runnable IO_BOUND_TASK = () -> {
    try {
      Thread.sleep(AVG_RESPONSE_TIME);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  };
  public static void main(String[] args){
    List<BenchmarkResult> results = new java.util.ArrayList<>(List.of(
        displayBenchmark("Virtual Threads", Executors.newVirtualThreadPerTaskExecutor(),
            IO_BOUND_TASK, NUM_OF_TASKS),
        displayBenchmark("Fixed thread pool (100)", Executors.newFixedThreadPool(100),
            IO_BOUND_TASK, NUM_OF_TASKS),
        displayBenchmark("Fixed thread pool (500)", Executors.newFixedThreadPool(500),
            IO_BOUND_TASK, NUM_OF_TASKS),
        displayBenchmark("Fixed thread pool (1000)", Executors.newFixedThreadPool(1000),
            IO_BOUND_TASK, NUM_OF_TASKS)
    ));
    results.sort(Comparator.comparingLong(BenchmarkResult::duration));
    results.forEach(r -> logger.info(() -> r.type + " | Tasks completed: " + r.tasksCompleted + " | Total time taken: " + r.duration
        + " ms | " + computeThroughput(r.duration, r.tasksCompleted)));


  }

  private static BenchmarkResult displayBenchmark(String type, ExecutorService es,
      Runnable task, int taskCount) {
    Instant start = Instant.now();
    AtomicInteger tasksCompleted = new AtomicInteger(0);
    try(es) {
      IntStream.rangeClosed(1, taskCount)
          .forEach(i -> {
            es.execute(() -> {
              task.run();
              tasksCompleted.incrementAndGet();
            });
          });
    }
    long duration = Duration.between(start, Instant.now()).toMillis();

    return new BenchmarkResult(type, tasksCompleted.get(), duration);
  }

  private static String computeThroughput(long duration, int completedTasks) {
    if (duration > completedTasks) {
      duration = (long) (duration * 0.10);
    }
    return "Throughput: " + completedTasks/duration + " /ms";
  }

  private record BenchmarkResult(String type, int tasksCompleted, long duration) {}
}
