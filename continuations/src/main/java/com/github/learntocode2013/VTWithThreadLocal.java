package com.github.learntocode2013;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;
import lombok.SneakyThrows;

//Purpose: Virtual Threads work with ThreadLocal
public class VTWithThreadLocal implements Runnable {
  private static final Logger logger = Logger.getLogger(VTWithThreadLocal.class.getName());
  private static final ThreadLocal<StringBuilder> threadLocal = ThreadLocal.
      withInitial(() -> new StringBuilder("No initial value..."));
  private static final Random rand = new Random();

  @SneakyThrows
  @Override
  public void run() {
    threadLocal.set(new StringBuilder(Thread.currentThread().toString()));
    logger.info(() -> "-> before sleep - " + threadLocal.get() + " | " + Thread.currentThread().toString());
    Thread.sleep(Duration.ofSeconds(rand.nextInt(10)));
    logger.info(() -> "-> after sleep - " + threadLocal.get() + " | " + Thread.currentThread().toString());
    threadLocal.remove();
  }

  static final class SimpleThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
      return Thread.ofVirtual().name("vt-", 1).unstarted(r); // virtual thread
    }
  }

  public static void main(String[] args){
    var testSubject = new VTWithThreadLocal();
    try (var es = Executors.newThreadPerTaskExecutor(new SimpleThreadFactory())) {
      for(var i = 0; i < 5; i++) {
        es.submit(testSubject);
      }
    }
  }
}
