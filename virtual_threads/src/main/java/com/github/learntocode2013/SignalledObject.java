package com.github.learntocode2013;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class SignalledObject {
  private static final Logger logger = Logger.getLogger(SignalledObject.class.getName());
  private static final AtomicInteger counter = new AtomicInteger(0);

  public void callWait() throws InterruptedException {
    if(counter.incrementAndGet() >= 0) {
      logger.warning(() -> "Missed signal detected. Will not make "
          + Thread.currentThread() +" wait to keep sanity");
      return;
    }
    logger.info(() -> Thread.currentThread() + " called wait | counter b4 wait: " + counter.get());
    this.wait();
    logger.info(() -> Thread.currentThread() + " called notify | counter after wait: " + counter.get());
  }

  public void callNotify() {
    logger.info(() -> Thread.currentThread() + " called notify | counter b4 notify: " + counter.get());
    counter.decrementAndGet();
    this.notify();
    logger.info(() -> Thread.currentThread() + " called notify | counter after notify: " + counter.get());
  }
}
