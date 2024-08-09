package com.github.learntocode2013;

import java.time.Duration;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class VTWithWaitNotify {
  private static final Logger logger  = Logger.getLogger(VTWithWaitNotify.class.getName());
  private static final SignalledObject monitor = new SignalledObject();

  public static void main(String[] args){
    demo();
  }

  @SneakyThrows
  private static void demo() {
    Thread wThread = Thread.ofVirtual().unstarted(() -> {
      synchronized (monitor) {
        try {
          logger.info(() -> Thread.currentThread() + " is in " + Thread.currentThread().getState());
          monitor.callWait();
          logger.info(() -> Thread.currentThread() + " is in " + Thread.currentThread().getState());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });
    wThread.setName("wThread");

    Thread nThread = Thread.ofVirtual().unstarted(() -> {
      synchronized (monitor) {
        logger.info(() -> Thread.currentThread() + " is in " + Thread.currentThread().getState());
        monitor.callNotify();
        logger.info(() -> Thread.currentThread() + " is in " + Thread.currentThread().getState());
      }
    });
    nThread.setName("nThread");

    nThread.start();
    logger.info(() -> nThread + " is in " + nThread.getState());
//    Thread.sleep(Duration.ofSeconds(2));
    logger.info(() -> wThread + " is in " + wThread.getState());

    wThread.start();

    Thread.sleep(Duration.ofSeconds(2));
    logger.info(() -> wThread + " is in " + wThread.getState());

    logger.info(() -> nThread + " is in " + nThread.getState());
    wThread.join();nThread.join();
  }
}
