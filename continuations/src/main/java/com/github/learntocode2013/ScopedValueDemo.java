package com.github.learntocode2013;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import jdk.incubator.concurrent.ScopedValue;
import lombok.SneakyThrows;

public class ScopedValueDemo {
  private static final Logger logger = Logger.getLogger(ScopedValueDemo.class.getName());
  private static final ScopedValue<String> SCOPED_VALUE_1 = ScopedValue.newInstance();
  private static final ScopedValue<String> SCOPED_VALUE_2 = ScopedValue.newInstance();
  public static void main(String[] args){
//    demoWithRunnable();
//    demoWithCallable();
//    demoScopedValueAccessFrom_NonMain_Thread();
//    demoScopedValue_With_ES();
    demoScopedValue_Rebind();
  }

  private static void demoScopedValue_Rebind() {
    final Random rand = new Random();
    Runnable taskB = () -> {
      logger.info(() -> Thread.currentThread().toString()
          + " | taskB before sleep SCOPED_VALUE_1 | "
          + (SCOPED_VALUE_1.isBound() ? SCOPED_VALUE_1.get() : "NOT_BOUND"));
      logger.info(() -> Thread.currentThread().toString()
          + " | taskB before sleep  SCOPED_VALUE_2 | "
          + (SCOPED_VALUE_2.isBound() ? SCOPED_VALUE_2.get() : "NOT_BOUND"));
      try {
        Thread.sleep(Duration.ofSeconds(rand.nextLong(5)));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      logger.info(() -> Thread.currentThread().toString()
          + " | taskB after sleep SCOPED_VALUE_1 | "
          + (SCOPED_VALUE_1.isBound() ? SCOPED_VALUE_1.get() : "NOT_BOUND"));
      logger.info(() -> Thread.currentThread().toString()
          + " | taskB after sleep SCOPED_VALUE_2 | "
          + (SCOPED_VALUE_2.isBound() ? SCOPED_VALUE_2.get() : "NOT_BOUND"));
    };

    Runnable taskA = () -> {
      logger.info(() -> Thread.currentThread().toString()
          + " | taskA before sleep SCOPED_VALUE_1 | "
          + (SCOPED_VALUE_1.isBound() ? SCOPED_VALUE_1.get() : "NOT_BOUND"));
      logger.info(() -> Thread.currentThread().toString()
          + " | taskA before sleep SCOPED_VALUE_2 | "
          + (SCOPED_VALUE_2.isBound() ? SCOPED_VALUE_2.get() : "NOT_BOUND"));
      ScopedValue
          .where(SCOPED_VALUE_1, "NA") //Re-bind operation b4 calling taskB
          .where(SCOPED_VALUE_2, "API_KEY")
          .run(taskB);
      logger.info(() -> Thread.currentThread().toString()
          + " | taskA after calling taskB SCOPED_VALUE_1 | "
          + (SCOPED_VALUE_1.isBound() ? SCOPED_VALUE_1.get() : "NOT_BOUND"));
      logger.info(() -> Thread.currentThread().toString()
          + " | taskA after calling taskB SCOPED_VALUE_2 | "
          + (SCOPED_VALUE_2.isBound() ? SCOPED_VALUE_2.get() : "NOT_BOUND"));
    };

    ScopedValue.where(SCOPED_VALUE_1, "Master-key").run(taskA);
  }

  private static void demoScopedValue_With_ES() {
    final Random rand = new Random();
    Runnable task = () -> {
      logger.info(() -> Thread.currentThread().toString()
          + " | before sleep | "
          + (SCOPED_VALUE_1.isBound() && SCOPED_VALUE_2.isBound() ?
          SCOPED_VALUE_1.get() + SCOPED_VALUE_2.get() : " NOT_BOUND"));

      try {
        Thread.sleep(Duration.ofSeconds(rand.nextLong(5)));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      logger.info(() -> Thread.currentThread().toString()
          + " | after sleep | "
          + (SCOPED_VALUE_1.isBound() && SCOPED_VALUE_2.isBound() ?
          SCOPED_VALUE_1.get() + SCOPED_VALUE_2.get() : " NOT_BOUND"));
    };

    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      for(var i = 0; i < 10; i++) {
        final int copyOf = i;
        es.submit(
            () -> ScopedValue
                .where(SCOPED_VALUE_1, "LuckyGenie-"+copyOf)
                .where(SCOPED_VALUE_2, "Kaboom-" + copyOf)
                .run(task)
        );
      }
    }
  }

  @SneakyThrows
  private static void demoScopedValueAccessFrom_NonMain_Thread() {
    Runnable taskR =
        () -> {
          logger.info(() -> Thread.currentThread().toString());
          logger.info(() -> "After | Is bound ? " + SCOPED_VALUE_1.isBound());
          logger.info(() -> "Runnable " + SCOPED_VALUE_1.orElse("nothing"));
        };
    Callable<String> taskC = () -> {
      logger.info(() -> "Callable " + SCOPED_VALUE_1.orElse("nothing"));
      return SCOPED_VALUE_1.orElse("nothing");
    };
    Thread t1 = new Thread(() -> ScopedValue.where(SCOPED_VALUE_1, "ScopedValue-Demo").run(taskR));
    Thread t2 = new Thread(() -> {
      try {
        ScopedValue.where(SCOPED_VALUE_1, "ScopedValue-Demo").call(taskC);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread vt1 = Thread.ofVirtual()
        .name("vt-taskR")
        .unstarted(() -> ScopedValue.where(SCOPED_VALUE_1, "ScopedValue-Demo").run(taskR));

    Thread vt2 = Thread.ofVirtual()
        .name("vt-taskC")
        .unstarted(() -> {
          try {
            ScopedValue.where(SCOPED_VALUE_1, "ScopedValue-Demo").call(taskC);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });

    t1.start(); t2.start(); vt1.start(); vt2.start();
    t1.join(); t2.join(); vt1.join(); vt2.join();
    logger.info(() -> "Scoped value state after task completion: "
        + SCOPED_VALUE_1.orElse("NOT_SET"));
  }

  private static void demoWithRunnable() {
    ScopedValue<String> ssv = ScopedValue.newInstance();
    logger.info(() -> "Before | Is bound ? " + ssv.isBound());
    Runnable taskR =
        () -> {
          logger.info(() -> Thread.currentThread().toString());
          logger.info(() -> "After | Is bound ? " + ssv.isBound());
          logger.info(() -> "Hello " + ssv.orElse("you"));
        };
    ScopedValue.where(ssv, "Dibakar").run(taskR);
    logger.info(() -> "Goodbye " + ssv.orElse("you"));
  }

  @SneakyThrows
  private static void demoWithCallable() {
    ScopedValue<String> ssv = ScopedValue.newInstance();
    Callable<String> taskC = () -> {
      logger.info(() -> "Hello " + ssv.orElse("developer"));
      return ssv.orElse("Nothing");
    };
    var name = ScopedValue.where(ssv,"Dibakar", taskC);
    logger.info(() -> "Goodbye " + name);
  }
}
