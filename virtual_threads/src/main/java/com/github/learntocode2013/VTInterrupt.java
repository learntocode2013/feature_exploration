package com.github.learntocode2013;

import java.time.Duration;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class VTInterrupt {
  private static final Logger logger = Logger.getLogger(VTInterrupt.class.getName());
  private static final String LS     = System.lineSeparator();

  public static void main(String[] args){
    var subject = new VTInterrupt();
    subject.demoPlatformThreadInterrupt();
    subject.demoVirtualThreadInterrupt();
  }

  @SneakyThrows
  public void demoPlatformThreadInterrupt() {
    var pThread =
        Thread.ofPlatform()
            .name("PF-Demo-", 0)
            .start(
                () -> {
                  logger.info(
                      () -> "Starting platform thread -> " + Thread.currentThread().getName());
                  for (var i = 0; i < 5; i++) {
                    try {
                      Thread.sleep(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                      logger.warning(() -> Thread.currentThread().getName() + " was interrupted" + LS);
                    }
                  }
                });

    Thread.sleep(Duration.ofMillis(2500));
    printAllExistingThreads();
    pThread.interrupt();
  }

  @SneakyThrows
  public void demoVirtualThreadInterrupt() {
    var vThread =
        Thread.ofVirtual()
            .name("VT-Demo-", 0)
            .start(
                () -> {
                  logger.info(
                      () -> "Starting virtual thread -> " + Thread.currentThread());
                  for (var i = 0; i < 5; i++) {
                    try {
                      Thread.sleep(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                      logger.warning(() -> Thread.currentThread() + " was interrupted" + LS);
                    }
                  }
                });

    Thread.sleep(Duration.ofMillis(2500));
    printAllExistingThreads();
    vThread.interrupt();
  }

  // Thread stack trace does not contain virtual thread trace
  private static void printAllExistingThreads() {
    StringBuilder sb = new StringBuilder();
    sb.append("--------------------------------------------------------------------").append(LS);
    Thread.getAllStackTraces().forEach((key, value) -> sb.append(key).append(LS));
    sb.append("--------------------------------------------------------------------").append(LS);
    logger.info(sb::toString);
  }
}
