package com.github.learntocode2013;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class VirtualThreadReentrantLockDeadLock {
  public static void main(String[] args){
    final boolean shouldPin = args.length == 0 || Boolean.parseBoolean(args[0]);
    final ReentrantLock lock = new ReentrantLock(true);

    lock.lock();

    Runnable takeLock =
        () -> {
          try {
            System.out.printf("%s waiting to acquire a lock...%n", Thread.currentThread());
            lock.lock();
            System.out.printf("%s acquired lock...%n", Thread.currentThread());
          } finally{
              lock.unlock();
            System.out.printf("%s released lock...%n", Thread.currentThread());
          }
        };

    Thread unpinnedThread = Thread.ofVirtual().name("unpinned").start(takeLock);

    List<Thread> pinnedThreads =
        IntStream.range(0, Runtime.getRuntime().availableProcessors())
            .mapToObj(
                i ->
                    Thread.ofVirtual()
                        .name("pinned-" + i)
                        .start(
                            () -> {
                              if (shouldPin) {
                                synchronized (new Object()) {
                                  takeLock.run();
                                }
                              } else {
                                takeLock.run();
                              }
                            }))
            .toList();

    System.out.println("Before lock unlock | Is Locked locked ? " + lock.isLocked() + " | Hold count: " + lock.getQueueLength());
    lock.unlock();

    Stream.concat(Stream.of(unpinnedThread), pinnedThreads.stream())
        .peek(thread -> System.out.println("Is Locked locked ? " + lock.isLocked() + " | " + thread + " is in queue ? " + lock.hasQueuedThread(thread)))
        .forEach(
            thread -> {
              try {
                if (!thread.join(Duration.ofSeconds(10))) {
                  throw new RuntimeException("Deadlock detected");
                }
              } catch (InterruptedException iex) {
                throw new RuntimeException(iex);
              }
            });
  }
}
