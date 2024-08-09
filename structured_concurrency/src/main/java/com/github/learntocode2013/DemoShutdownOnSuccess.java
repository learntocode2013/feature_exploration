package com.github.learntocode2013;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.incubator.concurrent.StructuredTaskScope;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DemoShutdownOnSuccess {
  private static final Logger logger = Logger.getLogger(DemoShutdownOnSuccess.class.getName());
  private final int maxSecondsForResult;

  public Optional<Integer> investMoney() {
    try(var scope = new StructuredTaskScope.ShutdownOnSuccess<Integer>()) {
      Future<Integer> hdfcTaskFut = scope.fork(this::fetchSharePriceOfHdfcBank);
      Future<Integer> relianceTaskFut = scope.fork(this::fetchSharePriceOfReliance);
      scope.join();
//      scope.joinUntil(Instant.now().plusSeconds(maxSecondsForResult));

      if(hdfcTaskFut.isDone()) {
        logger.log(Level.INFO, "HDFC task finished first and returned: {0}", scope.result());
        logger.log(Level.INFO, "Reliance task status is cancelled: {0}", relianceTaskFut.isCancelled());
      } else if (relianceTaskFut.isDone()) {
        logger.log(Level.INFO,
            "Reliance task finished first and returned: {0}", scope.result());
        logger.log(Level.INFO, "Hdfc task status is cancelled: {0}", hdfcTaskFut.isCancelled());
      }
      return Optional.of(scope.result());
    } catch (InterruptedException | ExecutionException cause) {
      logger.log(Level.SEVERE, "Failed to determine amount for equity investing", cause);
    }
    return Optional.empty();
  }

  @SneakyThrows
  private Integer fetchSharePriceOfReliance() {
    Thread.sleep(Duration.ofSeconds(5));
    logger.log(Level.INFO,"fetching Reliance share price");
    return 2346;
  }

  @SneakyThrows
  private Integer fetchSharePriceOfHdfcBank() {
    Thread.sleep(Duration.ofSeconds(5));
    logger.log(Level.INFO,"fetching HDFC bank share price");
    return 1456;
  }

  public static void main(String[] args){
    var testSubject = new DemoShutdownOnSuccess(6);
    for(int i = 0; i < 3; i++) {
      testSubject
          .investMoney()
          .ifPresent(val -> logger.log(Level.INFO, "You need to invest rupees: {0} in equity",val));
    }
  }
}
