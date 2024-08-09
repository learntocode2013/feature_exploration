package com.github.learntocode2013;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VTWithCF {
  private static final Logger logger = Logger.getLogger(VTWithCF.class.getName());
  private static final ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
  public static void main(String[] args) throws InterruptedException, ExecutionException {
    List<CompletableFuture<String>> futureTesters = Stream
        .of("Venkat", "Vaugh Vernon", "Dibakar Sen")
        .map(tester -> fetchTester(service, tester))
        .toList();
    CompletableFuture.allOf(futureTesters.toArray(new CompletableFuture[0])).join();
    var testTeam = futureTesters.stream().map(cf -> {
      try {
        return cf.get();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toSet());
    logger.info(() -> "Testing team: " + testTeam);
  }

  private static CompletableFuture<String> fetchTester1(ExecutorService es) {
    return fetchTester(es, "Venkat");
  }

  private static CompletableFuture<String> fetchTester2(ExecutorService es) {
    return fetchTester(es, "Vaugh Vernon");
  }

  private static CompletableFuture<String> fetchTester3(ExecutorService es) {
    return fetchTester(es, "Dibakar Sen");
  }

  private static CompletableFuture<String> fetchTester(ExecutorService es, String tester) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        logger.info(() -> Thread.currentThread() + " -> Fetching details for tester - " + tester);
        Thread.sleep(Duration.ofSeconds(2));
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return tester;
    }, es);
  }


}
