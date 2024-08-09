package com.github.learntocode2013;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class AssemblyLine {
  public static final Logger logger = Logger.getLogger(AssemblyLine.class.getName());
  private static final int PRODUCERS = 3;
  private static final int CONSUMERS = 2;
  private static final int MAX_QUEUE_SIZE_ALLOWED = 5;
  private static final int MAX_CONSUMERS_ALLOWED  = 50;
  private static final Semaphore producerService  = new Semaphore(PRODUCERS);
  private static final Semaphore consumerService  = new Semaphore(CONSUMERS);
  private static final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
  private static final Random rand = new Random();
  private static final Duration MAX_PRODUCER_TIME_SECONDS = Duration.ofSeconds(1);
  private static final Duration MAX_CONSUMER_TIME_SECONDS = Duration.ofSeconds(10);
  private static final int TIMEOUT_MS = MAX_PRODUCER_TIME_SECONDS.plus(MAX_CONSUMER_TIME_SECONDS)
      .multipliedBy(PRODUCERS + CONSUMERS).toMillisPart();
  private static long extraProdTime;
  private static long EXTRA_PROD_TIME_MS = Duration.ofSeconds(4).toMillis();
  private static final long SLOW_DOWN_PRODUCER_DELAY_MS = Duration.ofSeconds(150).toMillis();

  private static volatile boolean producerRunning = false;
  private static volatile boolean consumerRunning = false;

  private static final ExecutorService producerPool = Executors.newVirtualThreadPerTaskExecutor();
  private static final ExecutorService consumerPool = Executors.newVirtualThreadPerTaskExecutor();
  private static ScheduledExecutorService queueMonitorSvc;
  private static ScheduledExecutorService slowDownSvc;

  private static final Producer producer = new Producer();
  private static final Consumer consumer = new Consumer();
  private static final AtomicInteger numOfConsumers = new AtomicInteger(0);
  private static final AtomicBoolean removeConsumer = new AtomicBoolean(false);

  @SneakyThrows
  public static void main(String[] args){
    AssemblyLine.startAssemblyLine();
    Thread.sleep(Duration.ofSeconds(60));
    AssemblyLine.stopAssemblyLine();

    Thread.sleep(Duration.ofSeconds(2));
    AssemblyLine.startAssemblyLine();
    Thread.sleep(Duration.ofSeconds(10));
    AssemblyLine.stopAssemblyLine();
  }

  public static void startAssemblyLine() {
    if (producerRunning || consumerRunning) {
      logger.warning(() -> "Assembly line already running");
      logger.info("Remaining bulbs from previous line: " + queue);
      return;
    }
    logger.info("Starting assembly line....");
    logger.info("Remaining bulbs from previous line: " + queue);

    producerRunning = true;
    startProducersUsingSemaphores();

    consumerRunning = true;
    startConsumersUsingSemaphores();
    monitorQueueSize();
    slowDownProducer();
  }

  private static void startProducersUsingSemaphores() {
    for (var i = 0; i < PRODUCERS; i++) {
      limitVirtualThreadsWithSemaphores(producerService, producer);
    }
  }
  private static void startConsumersUsingSemaphores() {
    for (var i = 0; i < CONSUMERS; i++) {
      limitVirtualThreadsWithSemaphores(consumerService, consumer);
    }
  }

  private static void limitVirtualThreadsWithSemaphores(Semaphore semaphore, Runnable task) {
    Thread.ofVirtual().start(
        () -> {
          try {
            semaphore.acquire();
            task.run();
          } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            iex.printStackTrace();
          } finally{
            semaphore.release();
          }

        }
    );
  }

  public static void stopAssemblyLine() {
    logger.info(() -> "Stopping assembly line...");
    boolean isProducerDown = shutdownWorkerPool(producerPool);
    boolean isConsumerDown = shutdownWorkerPool(consumerPool);
    boolean schedulersDown = shutdownSchedulers();
    if (!isProducerDown || !isConsumerDown || !schedulersDown) {
      logger.severe("Failed to stop assembly line!!!");
      System.exit(1);
    }
    logger.info(() -> "Assembly line was successfully stopped...");
  }

  private static boolean shutdownSchedulers() {
    if (!producerRunning || !consumerRunning) {
      return shutdownWorkerPool(queueMonitorSvc) && shutdownWorkerPool(slowDownSvc);
    }
    return false;
  }

  private static boolean shutdownProducer() {
    producerRunning = false;
    return shutdownWorkerPool(producerPool);
  }
  private static boolean shutdownConsumer() {
    consumerRunning = false;
    return shutdownWorkerPool(consumerPool);
  }

  static class Producer implements Runnable {

    @Override
    public void run() {
      while(producerRunning) {
        String bulb = "bulb-" + rand.nextInt(1000);
        //Simulate producer verifying the light bulb
        try {
          Thread.sleep(MAX_PRODUCER_TIME_SECONDS.plusMillis(extraProdTime));
          queue.offer(bulb);
          logger.info(() -> "PRODUCER - " + Thread.currentThread().toString()
              + " finished checking " + bulb);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          logger.severe(e.getMessage());
          break;
        }
      }
    }
  }

  static class Consumer implements Runnable {

    @Override
    public void run() {
      while(consumerRunning) {
        if (removeConsumer.get()) {
          numOfConsumers.decrementAndGet();
          removeConsumer.set(false);
          Thread.currentThread().interrupt();
        }
        if (queue.isEmpty()) {
          continue;
        }
        String bulb = queue.poll();
        // Simulate consumer spending time in packing the bulb
        try {
          Thread.sleep(MAX_CONSUMER_TIME_SECONDS);
          logger.info(() -> "CONSUMER -" + Thread.currentThread() + " packed " + bulb);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          e.printStackTrace();
          break;
        }
      }
    }
  }

  private static void monitorQueueSize() {
    queueMonitorSvc = Executors.newSingleThreadScheduledExecutor();
    queueMonitorSvc.scheduleAtFixedRate(() -> {
      if (queue.size() > MAX_QUEUE_SIZE_ALLOWED
          && numOfConsumers.get() < MAX_CONSUMERS_ALLOWED) {
        addConsumer();
      } else {
        removeConsumer();
      }
    }, 5, 3, TimeUnit.SECONDS);
  }

  private static void removeConsumer() {
    logger.info(() -> "### Removing a consumer due to reduced task load of - " + queue.size());
    removeConsumer.set(true);
  }

  private static void addConsumer() {
    logger.info(() -> "### Adding a new consumer due to task backlog of - " + queue.size());
    if (consumerService.availablePermits() == 0) {
      consumerService.release();
    }
    limitVirtualThreadsWithSemaphores(consumerService, consumer);
    numOfConsumers.incrementAndGet();
  }

  // Simulate decrease in production rate
  private static void slowDownProducer() {
    slowDownSvc = Executors.newSingleThreadScheduledExecutor();
    slowDownSvc.schedule(() -> {
      logger.warning(() -> "### Slowing down production rate of bulbs");
      extraProdTime = EXTRA_PROD_TIME_MS;
    },SLOW_DOWN_PRODUCER_DELAY_MS, TimeUnit.MILLISECONDS);
  }

  private static boolean shutdownWorkerPool(ExecutorService es) {
    es.shutdown();
    try {
      if (!es.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
        es.shutdownNow(); // enough time was given for task completion. No more
        return es.awaitTermination(TIMEOUT_MS, TimeUnit.MILLISECONDS);
      }
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
    return false;
  }
}
