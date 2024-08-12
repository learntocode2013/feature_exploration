package com.github.learntocode2013;

import static java.util.concurrent.Future.State.CANCELLED;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import jdk.incubator.concurrent.StructuredTaskScope;

public class CollectingTaskScope<T> extends StructuredTaskScope<T> {
  private final Queue<T> results    = new ConcurrentLinkedQueue<>();
  private final Queue<Throwable> exceptions = new ConcurrentLinkedQueue<>();
  private final Thread owner;
  private boolean joined = false;

  public CollectingTaskScope() {
    this.owner = Thread.currentThread();
  }

  @Override
  protected void handleComplete(Future<T> future) {
    switch (future.state()) {
      case SUCCESS -> results.add(future.resultNow());
      case FAILED -> exceptions.add(future.exceptionNow());
      case CANCELLED -> throw new IllegalStateException("Unexpected task state - " + CANCELLED);
    }
  }

  public List<T> getResults() {
    ensureOwnerAndJoined();
    return results.stream().toList();
  }

  public Optional<Throwable> exceptions() {
    if (exceptions.isEmpty()) {
      return Optional.empty();
    }
    ensureOwnerAndJoined();
    RuntimeException re = new RuntimeException();
    exceptions.forEach(re::addSuppressed);
    return Optional.of(re);
  }

  @Override
  public CollectingTaskScope<T> join() throws InterruptedException {
    super.join();
    joined = true;
    return this;
  }

  @Override
  public CollectingTaskScope<T> joinUntil(Instant deadline) throws InterruptedException, TimeoutException {
    super.joinUntil(deadline);
    joined = true;
    return this;
  }

  public boolean isOwner(Thread thread) {
    return owner == thread;
  }

  private void ensureOwnerAndJoined() {
    // Check if the current thread is the owner of the task scope
    if (!isOwner(Thread.currentThread()) && !joined) {
      throw new IllegalStateException("Current thread is not the owner of the task scope");
    }
    // Check if the task scope has been joined
    if (!joined) {
      throw new IllegalStateException("Task scope has not been joined");
    }
  }
}
