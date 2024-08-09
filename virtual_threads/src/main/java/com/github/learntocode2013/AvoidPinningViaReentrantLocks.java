package com.github.learntocode2013;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class AvoidPinningViaReentrantLocks {
  private static final Logger logger = Logger.getLogger(AvoidPinningViaReentrantLocks.class.getName());
  private final ReentrantLock lock = new ReentrantLock();
  private final int NUMBER_OF_TASKS = 25;
  public static void main(String[] args){
    var subject = new AvoidPinningViaReentrantLocks();
//    subject.demoVTPinning();
    subject.demoVT_WithLocks();
  }

  private void demoVT_WithLocks() {
    logger.info(() -> "Number of processors: " + Runtime.getRuntime().availableProcessors());
    Runnable task = this::nonPinningAction;
    submitForExecution(task);
  }

  private void demoVTPinning() {
    logger.info(() -> "Number of processors: " + Runtime.getRuntime().availableProcessors());
    Runnable task = this::blockingAction;
    submitForExecution(task);
  }

  private void submitForExecution(Runnable task) {
    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      for (var i = 0; i < NUMBER_OF_TASKS; i++) {
        es.submit(task);
      }
    }
  }

  @SneakyThrows
  private void blockingAction() {
    synchronized (AvoidPinningViaReentrantLocks.class) {
      logger.info(() -> "BlockingAction (b4 sleep) owning thread - " + Thread.currentThread());
      Thread.sleep(Duration.ofSeconds(5));
      logger.info(() -> "BlockingAction (after sleep) owning thread - " + Thread.currentThread());
    }
  }

  @SneakyThrows
  private void nonPinningAction() {
    lock.lock();
    logger.info(() -> "nonPinningAction (b4 sleep) owning thread - " + Thread.currentThread());
    Thread.sleep(Duration.ofSeconds(5));
    lock.unlock();
    logger.info(() -> "nonPinningAction (after sleep) owning thread - " + Thread.currentThread());
  }
}
