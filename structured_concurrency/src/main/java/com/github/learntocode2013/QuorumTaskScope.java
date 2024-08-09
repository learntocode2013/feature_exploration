package com.github.learntocode2013;

import java.util.concurrent.Future;
import java.util.concurrent.Future.State;
import java.util.concurrent.atomic.AtomicInteger;
import jdk.incubator.concurrent.StructuredTaskScope;

public class QuorumTaskScope extends StructuredTaskScope<Boolean> {
  private static final AtomicInteger successfulUpdates = new AtomicInteger(0);
  private final int quorumSize;

  public QuorumTaskScope(int quorumSize) {
    super(QuorumTaskScope.class.getName(), Thread.ofVirtual().factory());
    this.quorumSize = quorumSize;
  }

  @Override
  protected void handleComplete(Future<Boolean> future) {
    if (future.state() == State.SUCCESS) {
      successfulUpdates.incrementAndGet();
    }
    if (successfulUpdates.get() >= quorumSize) {
      this.shutdown();
    }
  }

  public boolean isQuorumReached() {
    return successfulUpdates.get() >= quorumSize;
  }

}
