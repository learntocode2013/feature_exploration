package com.github.learntocode2013;

import java.time.Duration;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class VTWithSyncCode {
  private final Logger logger = Logger.getLogger(VTWithSyncCode.class.getName());
  public static void main(String[] args){
    new VTWithSyncCode().execute();
  }

  @SneakyThrows
  public void execute() {
    final SynchronousQueue<Integer> queue = new SynchronousQueue<>();
    Runnable task =
        () -> {
          logger.info(() -> Thread.currentThread().getName() + " sleeping for 5 seconds");
          try {
            Thread.sleep(Duration.ofSeconds(5));
          } catch (InterruptedException iex) {
          }
          logger.info(() -> "Running " + Thread.currentThread().getName());
          queue.add(Integer.MAX_VALUE);
        };
    logger.info(() -> "Before running the task...");
    var vThread = Thread.ofVirtual().start(task);
    logger.info(vThread::toString);

    logger.info(() -> Thread.currentThread().getName() + " cannot take from queue yet...");
    int max = queue.take();
    logger.info(vThread::toString);
    logger.info(() -> "After running the task...max:" + max);
  }
}
