package com.github.learntocode2013;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.SneakyThrows;

public class ThreadPerRequest {
  enum ThreadType { VIRTUAL, PLATFORM }

  @SneakyThrows
  public static void main(String[] args){
    useSynchronousCode();
    useAsynchronousCode().join();
  }

  private static void demoThroughPutWithVt() {
    new ThreadPerRequest()
//        .handleRequests(new PrintMessage("I am running inside thread - "), ThreadType.PLATFORM);
        .handleRequests(new PrintMessage("I am running inside thread - "), ThreadType.VIRTUAL);
  }

  private static CompletableFuture<Float> useAsynchronousCode() {
    return CompletableFuture.supplyAsync(ThreadPerRequest::readPriceInEur)
        .thenCombine(
            CompletableFuture.supplyAsync(ThreadPerRequest::readExchangeRateEurToUsd),
            (euros, exchangeRate) -> euros * exchangeRate)
        .thenCombine(
            CompletableFuture.supplyAsync(ThreadPerRequest::fetchApplicableTaxRate),
            (netAmount, taxRate) -> netAmount * (1 + taxRate))
        .whenComplete(
            (grossAmountInUsd, err) -> {
              if (Objects.isNull(err)) {
                System.out.printf("Gross amount in usd: %f %n", grossAmountInUsd);
              }
            });
  }

  private static void useSynchronousCode() throws InterruptedException, ExecutionException {
    try(var eServ  = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<Integer> eurosFut   = eServ.submit(ThreadPerRequest::readPriceInEur);
      Future<Float> eurosToUsdRateFut = eServ.submit(ThreadPerRequest::readExchangeRateEurToUsd);
      float netAmount = eurosFut.get() * eurosToUsdRateFut.get();
      Future<Float> taxRateFut = eServ.submit(ThreadPerRequest::fetchApplicableTaxRate);
      float grossAmountInUsd = netAmount * (1 + taxRateFut.get());
      System.out.printf("Gross amount in usd: %f %n", grossAmountInUsd);
    }
  }

  @SneakyThrows
  private void handleRequests(PrintMessage request, ThreadType threadType) {
    final int MAX_THREADS = 1_000_000_000;
    final CountDownLatch latch = new CountDownLatch(MAX_THREADS);
    for(var i = 0; i < MAX_THREADS; i++) {
      switch (threadType) {
        case VIRTUAL:
          handleWithVirtualThreads(request, latch);
          break;
        case PLATFORM:
          handleWithPlatformThreads(request, latch);
          break;
        default:
          System.out.println("Program was written only for virtual or platform thread type");
      }
    }

    latch.await();
  }

  private static void handleWithVirtualThreads(PrintMessage request, CountDownLatch latch) {
    var vthread = Thread.ofVirtual().unstarted(() -> {
      // Do some blocking operation maybe which blocks the thread and hence keeps the thread active;
      // only then can we see the limits of platform threads.
      try{
        Thread.sleep(Duration.ofSeconds(1));
      } catch (InterruptedException ex) {

      }
      System.out.printf("%s - %s %n", request.message(), Thread.currentThread().getName());
      latch.countDown();
    });
    vthread.start();
  }

  private static void handleWithPlatformThreads(PrintMessage request, CountDownLatch latch) {
    var thread = new Thread(() -> {
      // Do some blocking operation maybe which blocks the thread and hence keeps the thread active;
      // only then can we see the limits of platform threads.
      try{
        Thread.sleep(Duration.ofSeconds(1));
      } catch (InterruptedException ex) {

      }
      System.out.printf("%s - %s %n", request.message(), Thread.currentThread().getName());
      latch.countDown();
    });
    thread.start();
  }


  private record PrintMessage(String message) {}

  /* Blocking I/O operation. */
  private static int readPriceInEur() {
    System.out.printf("[%s] Fetching price in euros from an external service...%n",
        Thread.currentThread().getName().isEmpty() ? Thread.currentThread().isVirtual() : Thread.currentThread().getName());
    return sleepAndGet(100, Duration.ofSeconds(10));
  }

  /* Blocking I/O operation. */
  private static float readExchangeRateEurToUsd() {
    System.out.printf("[%s] Fetching exchange rate for euros to usd from an external service...%n",
        Thread.currentThread().getName().isEmpty() ? Thread.currentThread().isVirtual() : Thread.currentThread().getName());
    return sleepAndGet(0.8f, Duration.ofSeconds(10));
  }

  private static float fetchApplicableTaxRate() {
    System.out.printf("[%s] Fetching tax rate in United States from an external service %n",
        Thread.currentThread().getName().isEmpty() ? Thread.currentThread().isVirtual() : Thread.currentThread().getName());
    return sleepAndGet(0.37f, Duration.ofSeconds(10));
  }

  private static float sleepAndGet(float val, Duration period) {
    try {
      Thread.sleep(period);
    } catch (InterruptedException iex) {}
    return val;
  }

  private static int sleepAndGet(int val, Duration period) {
    try {
      Thread.sleep(period);
    } catch (InterruptedException iex) {}
    return val;
  }
}
