package com.github.learntocode2013;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import jdk.incubator.concurrent.StructuredTaskScope;

public class LimitingTaskScope<T> extends StructuredTaskScope<T> {
  private final Semaphore threadPermits;

  public LimitingTaskScope(int maxConcurrency) {
    super("LimitingTaskScope", Thread.ofVirtual().factory());
    this.threadPermits = new Semaphore(maxConcurrency);
  }

  @Override
  public <U extends T> Future<U> fork(Callable<? extends U> task) {
    try{
      threadPermits.acquire();
      return super.fork(task);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(ex);
    }
  }

  @Override
  protected void handleComplete(Future<T> future) {
    try {
      super.handleComplete(future);
    } finally{
      threadPermits.release();
    }
  }
}
