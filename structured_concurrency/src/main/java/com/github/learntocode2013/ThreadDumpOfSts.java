package com.github.learntocode2013;

import java.util.List;
import java.util.UUID;
import jdk.incubator.concurrent.StructuredTaskScope;
import lombok.SneakyThrows;

public class ThreadDumpOfSts {
  private static final String TOP_TASK_NAME  = "Product Aggregator";

  @SneakyThrows
  public ProductInfo fetchProductInfo(String id) {
    try (var scope =
        new StructuredTaskScope.ShutdownOnFailure(TOP_TASK_NAME, Thread.ofVirtual().factory())) {
      var productSubTask = scope.fork(() -> fetchProduct(id));
      var reviewsSubTask = scope.fork(() -> fetchProductReviews(id));
      scope.join()
          .throwIfFailed();
      return new ProductInfo(productSubTask.get(), reviewsSubTask.get());
    }
  }

  private Product fetchProduct(String id) {
    while(true) {

    }
  }

  private List<Review> fetchProductReviews(String id) {
    while (true) {

    }
  }

  public static void main(String[] args){
    var subject = new ThreadDumpOfSts();
    var productInfo = subject.fetchProductInfo(UUID.randomUUID().toString());
    System.out.println(productInfo);
  }

  public record ProductInfo(Product product, List<Review> reviews) {}
  public record Product(String label, String id) {}
  public record Review(String text, int rating) {}
}
