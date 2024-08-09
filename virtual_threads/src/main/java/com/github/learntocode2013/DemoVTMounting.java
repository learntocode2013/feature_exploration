package com.github.learntocode2013;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class DemoVTMounting {
  private final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
  private final Logger logger = Logger.getLogger(DemoVTMounting.class.getName());

  static final class SimpleThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      return Thread.ofVirtual().name("vt-", 1).unstarted(r); // virtual thread
//      return Thread.ofPlatform().name("pt-",1).unstarted(r); // classic thread
    }
  }

  private void executePrintTask(int index) {
    logger.info(() -> index + " " + Thread.currentThread().toString());
    try {
      Thread.sleep(Duration.ofSeconds(3));
    } catch (InterruptedException ex) {

    }
    logger.info(() -> index + " " + Thread.currentThread().toString());
  }

  void assignTaskToThreads() {
//    try(var es = Executors.newThreadPerTaskExecutor(new SimpleThreadFactory())) {
    try(var es = Executors.newThreadPerTaskExecutor(new SimpleThreadFactory())) {
      for(var i = 0; i < MAX_THREADS; i++) {
        int index = i;
        es.submit(() -> executePrintTask(index));
      }
    }
  }

  public static void main(String[] args){
//      new DemoVTMounting().createVT();
    new DemoVTMounting().assignTaskToThreads();
  }

  public void createVT() {
    final int NUMBER_OF_TASKS = Runtime.getRuntime().availableProcessors();
    logger.info(String.format("Number of processors: %d", NUMBER_OF_TASKS));
    Runnable task = () -> logger.info(Thread.currentThread().toString());
    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      for(var i = 0; i < NUMBER_OF_TASKS + 4; i++) {
        es.submit(task);
      }
    }
  }
}
