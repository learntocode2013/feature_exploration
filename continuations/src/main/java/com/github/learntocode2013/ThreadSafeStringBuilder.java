package com.github.learntocode2013;

import java.time.Duration;
import java.util.Random;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class ThreadSafeStringBuilder implements Runnable {
  private static final Logger logger = Logger.getLogger(ThreadSafeStringBuilder.class.getName());
  private static final ThreadLocal<StringBuilder> tl = ThreadLocal
      .withInitial(() -> new StringBuilder("thread-safe"));
  private static final Random rand = new Random();

  @SneakyThrows
  public static void main(String[] args){
    ThreadSafeStringBuilder testSubject = new ThreadSafeStringBuilder();
    for(var i = 0; i < 5; i++) {
      new Thread(testSubject,"Thread-"+i).start();
    }
    Thread.sleep(Duration.ofSeconds(120));
  }

  @SneakyThrows
  @Override
  public void run() {
    logger.info(() -> "-> " + Thread.currentThread().getName() + "[" + tl.get() + "]");
    Thread.sleep(Duration.ofMillis(rand.nextInt(2000)));
    tl.get().append(Thread.currentThread().getName());
    logger.info(() -> "-> " + Thread.currentThread().getName() + "[" + tl.get() + "]");
    tl.set(null);
    logger.info(() -> "-> " + Thread.currentThread().getName() + "[" + tl.get() + "]");
  }
}
