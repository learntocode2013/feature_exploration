package com.github.learntocode2013;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import jdk.internal.vm.Continuation;
import jdk.internal.vm.ContinuationScope;

public class ContinuationDemo {
  private static final Logger logger = Logger.getLogger(ContinuationDemo.class.getName());
  public static void main(String[] args){
    var cscope = new ContinuationScope("cscope");
    var c =
        new Continuation(
            cscope,
            () -> {
              logger.info(() -> String.format("%s started to run continuation.",
                  Thread.currentThread().getName()));
              Continuation.yield(cscope);
              logger.info(() -> String.format("%s running continuation.",
                  Thread.currentThread().getName()));
              Continuation.yield(cscope);
              logger.info(() -> String.format("%s running continuation.",
                  Thread.currentThread().getName()));
            });

    logger.info(() -> String.format("%s about to run continuation.",
        Thread.currentThread().getName()));
    final AtomicInteger counter = new AtomicInteger(0);
    while(!c.isDone()) {
      c.run();
      logger.info(() -> String.format("Take %d | continuation is done ? %s", counter.get(), c.isDone()));
      counter.incrementAndGet();
    }
  }
}
