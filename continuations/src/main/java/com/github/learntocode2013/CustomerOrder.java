package com.github.learntocode2013;


import java.time.Duration;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import lombok.SneakyThrows;

//Demonstrates setting thread context via ThreadLocal
public record CustomerOrder(int customerId) implements Runnable {
  private static final Logger logger = Logger.getLogger(CustomerOrder.class.getName());
  private static final Random rand   = new Random();
  private static final ThreadLocal<Order> customerOrder = new ThreadLocal<>();

  @SneakyThrows
  @Override
  public void run() {
    logger.info(() -> "Given customer id: " + customerId() + " | "
        + customerOrder.get() + " | " + Thread.currentThread().getName());
    customerOrder.set(new Order(customerId));
    Thread.sleep(Duration.ofSeconds(rand.nextInt(10)));
    logger.info(() -> "Given customer id: " + customerId() + " | "
        + customerOrder.get() + " | " + Thread.currentThread().getName());
    customerOrder.remove();
  }

  public static void main(String[] args){
    // Orders from three different customers
    var co1 = new CustomerOrder(1);
    var co2 = new CustomerOrder(2);
    var co3 = new CustomerOrder(3);
    var orders = new CustomerOrder[] {co1, co2, co3};

    // Thread per request model
    Thread[] threads = new Thread[3];
    for(var i = 0; i < orders.length; i++) {
      threads[i] = new Thread(orders[i], "cust-thread-"+i);
      threads[i].start();
    }

    IntStream.rangeClosed(0,2).forEach(i -> {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
