package com.github.learntocode2013;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class WebServerHandler implements HttpHandler {
  private static final Logger logger = Logger.getLogger(WebServerHandler.class.getName());
  private static final int PERMITS = 20;
  private static final Semaphore semaphore = new Semaphore(PERMITS);
  private static final AtomicInteger requestIdCounter = new AtomicInteger(0);
  private boolean withLock;
  private final Callable<String> task = () -> {
    Thread.sleep(Duration.ofMillis(200));
    return "Request id_" + requestIdCounter.incrementAndGet();
  };

  public WebServerHandler(boolean withLock) {
    this.withLock = withLock;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String response = null;
    // Constructs the response text
    response = constructResponseText();
    respondBackToClient(exchange, response);
  }

  private void respondBackToClient(HttpExchange exchange, String response) throws IOException {
    exchange.sendResponseHeaders(200, null == response ? 0 : response.length());
    var outStream = exchange.getResponseBody();
    outStream.write(response == null ? new byte[0] : response.getBytes());
  }

  private String constructResponseText() {
    String response;
    if (withLock) {
      try {
        semaphore.acquire();
        response = task.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally{
        semaphore.release();
      }
    } else {
      try {
        response = task.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return response;
  }
}
