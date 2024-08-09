package com.github.learntocode2013;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Future.State;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import jdk.incubator.concurrent.StructuredTaskScope;
import lombok.SneakyThrows;

public class DemoVTWithXS {
  private static final Logger logger = Logger.getLogger(DemoVTWithXS.class.getName());
  public static void main(String[] args){
//    new DemoVTWithXS().buildTestingTeamWithAvailableTester();
//    List<Tester> testers = new DemoVTWithXS().buildTeamWithAllTesters_StructuredConcurrency();
//    logger.info(() -> "Got " + testers.size() + " testers for the team");
//    Tester tester = new DemoVTWithXS().buildTeamWithAnyAvailableTester_StructuredConcurrency();
//    logger.info(() -> "Got " + tester);
    List<Tester> testers = new DemoVTWithXS().buildTeamWithExactTesters_StructuredConcurrency();
    logger.info(() -> "Got " + testers.size() + " testers for the team");
  }

  // All of nothing concept
  @SneakyThrows
  private List<Tester> buildTeamWithExactTesters_StructuredConcurrency() {
    try(var sts = new StructuredTaskScope.ShutdownOnFailure()) {
      List<Future<String>> futureTesters = Stream.of(1, Integer.MAX_VALUE, 2)
          .map(id -> sts.fork(() -> fetchTester(id, Duration.ZERO)))
          .toList();
      sts.join();

      futureTesters.forEach(f -> logger.info(() -> "Future state: " + f.state()));
      sts.throwIfFailed();
      return futureTesters.stream().map(Future::resultNow).map(Tester::new).toList();
    }
  }

  @SneakyThrows
  private Tester buildTeamWithAnyAvailableTester_StructuredConcurrency() {
    try(var sts = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
      // Task submission
      List<Future<String>> futureTesters = IntStream.rangeClosed(1, 5)
          .mapToObj(id -> sts.fork(() -> fetchTester(id, Duration.ofMillis(100))))
          .toList();

      sts.joinUntil(Instant.now().plusMillis(500));

      futureTesters.forEach(f -> logger.info(() -> "Future state: " + f.state()));

      return new Tester(sts.result());
    }
  }

  @SneakyThrows
  private List<Tester> buildTeamWithAllTesters_StructuredConcurrency() {
    try(var sts = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
      List<Future<String>> futureTesters = Stream.of(Integer.MAX_VALUE, 2, Integer.MIN_VALUE)
          .map(id -> sts.fork(() -> fetchTester(id, Duration.ofSeconds(0))))
          .toList();
      sts.joinUntil(Instant.now().plusSeconds(1));

      //Collect successful results
      var testers = futureTesters.stream().filter(f -> f.state() == State.SUCCESS)
          .map(f -> f.resultNow())
          .map(Tester::new)
          .toList();

      // Collect errors
      var errors = futureTesters.stream().filter(f -> f.state() == State.FAILED)
          .map(f -> f.exceptionNow())
          .peek(t -> logger.warning(() -> "Failed to fetch tester due to: " + t.getClass().getName()))
          .toList();

      return testers;
    }
  }

  @SneakyThrows
  public List<Tester> buildTeamWithAllTesters_StreamsImpl() {
    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      var futureTesters = es.invokeAll(List.of(
          () -> fetchTester(Integer.MAX_VALUE, Duration.ofSeconds(0)),
          () -> fetchTester(2, Duration.ofSeconds(0)),
          () -> fetchTester(Integer.MIN_VALUE, Duration.ofSeconds(2))
      ), 1, TimeUnit.SECONDS);
      // Collect all successful results
      var testers =
          futureTesters.stream()
              .filter(f -> f.state() == State.SUCCESS)
              .map(f -> f.resultNow().toString())
              .map(Tester::new)
              .toList();
      // Collect all failures
      var failures = futureTesters
          .stream().filter(f -> f.state() == State.FAILED)
          .map(Future::exceptionNow)
          .peek(t -> logger.warning(() -> "Failed to fetch tester due to: " + t.getClass().getName()))
          .toList();
      return testers;
    }
  }

  @SneakyThrows
  public List<Tester> buildTestingTeamWithAllTesters() {
    List<Tester> result = new ArrayList<>();
    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<Object>> futureTesters = es.invokeAll(List.of(
              () -> fetchTester(Integer.MAX_VALUE, Duration.ofSeconds(0)),
              () -> fetchTester(2, Duration.ofSeconds(0)),
              () -> fetchTester(Integer.MIN_VALUE, Duration.ofSeconds(2))
          ), 1, TimeUnit.SECONDS
      );
      for(var futTester : futureTesters) {
        logger.info(() -> "Analyzing " + futTester + " state...");
        switch (futTester.state()) {
          case SUCCESS -> {
            logger.info(() -> "Got a tester successfully");
            result.add(new Tester((String)futTester.get()));
          }
          case RUNNING -> {
            logger.warning(() -> "Still looking for a tester");
            throw new IllegalStateException("Future is still in the running state");
          }
          case FAILED -> {
            logger.severe(() -> "Failed to fetch a tester due to: " + futTester.exceptionNow());
          }
          case CANCELLED -> {
            logger.severe(() -> "Failed to fetch a tester: " + futTester.state());
          }
        }
      }
    }
    return result;
  }

  @SneakyThrows
  public void buildTestingTeamWithAvailableTester() {
    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      String result = es.invokeAny(List.of(
          () -> fetchTester(1, Duration.ofSeconds(0)),
          () -> fetchTester(2, Duration.ofSeconds(0))
      ), 1, TimeUnit.SECONDS);
      logger.info(() -> result);
    }
  }

  @SneakyThrows
  private String fetchTester(int id, Duration delay) {
    logger.info(() -> "Thread " + Thread.currentThread().toString() + " fetching tester with id: " + id);
    Thread.sleep(delay);
    HttpClient httpClient = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder(URI.create("https://reqres.in/api/users/" + id))
        .GET()
        .build();
    HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
    if (response.statusCode() == 200) {
      return response.body();
    }
    throw new UserPrincipalNotFoundException("Code: " + response.statusCode());
  }

  record Tester(String detail) {}
}
