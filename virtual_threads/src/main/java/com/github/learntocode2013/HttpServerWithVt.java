package com.github.learntocode2013;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HttpServerWithVt {
  private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
  private static final int MAX_NR_OF_THREADS = 200;
  private static final int SERVER_PORT = 8081;
  private HttpServer server;

  public void startWebServer(boolean withVirtualThread, boolean withLock) throws IOException {
    var server = HttpServer.create(
        new InetSocketAddress(SERVER_PORT), 0);
    server.createContext("/webserver", new WebServerHandler(withLock));
    if (withVirtualThread) {
      server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    } else {
      server.setExecutor(Executors.newFixedThreadPool(MAX_NR_OF_THREADS));
    }
    server.start();
    logger.info(() -> "Server was started on port: " + SERVER_PORT);
    this.server = server;
  }

  public void stopWebServer() {
    server.stop(0);
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    var subject = new HttpServerWithVt();
    subject.startWebServer(true, true);
//    Thread.sleep(Duration.ofSeconds(5));
//    subject.stopWebServer();
  }
}
