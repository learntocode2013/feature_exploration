package com.github.learntocode2013;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import lombok.SneakyThrows;

public class SimplifiedAsyncOperations {
  private static final String LS = System.lineSeparator();
  private static final Logger logger = Logger.getLogger(SimplifiedAsyncOperations.class.getName());
  private static final String URI_TEMPLATE = "https://www.omdbapi.com/?type=movie&t=%s&apikey=4913ce67";
  private static final String WRONG_URI_TEMPLATE = "https://www.omdbapi.com/?type=movie&t=%s&apikey=4913ce6";

  public static void main(String[] args){
    var subject = new SimplifiedAsyncOperations();
    subject.demoAsyncAction();
  }

  @SneakyThrows
  public void demoAsyncAction() {
    List<Callable<String>> tasks = new ArrayList<>();
    tasks.add(() -> fetchMovieInfo("Avengers"));
    tasks.add(() -> fetchMovieInfo("Balboa"));
    tasks.add(() -> fetchMovieInfo("Happiness"));

    try(var es = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Future<String>> futureMovies = es.invokeAll(tasks);
      for (var futureMovie : futureMovies) {
        // Blocking call
        var movieInfo = futureMovie.get();
        logger.info(() -> movieInfo + LS);
      }
    }
  }

  @SneakyThrows
  private String fetchMovieInfo(String title) {
    logger.info(() -> Thread.currentThread() + " -> fetching movie info for -> " + title);
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder(URI.create(URI_TEMPLATE.formatted(title)))
        .GET()
        .build();
    var response = client.send(request, BodyHandlers.ofString());
    if (response.statusCode() == 200) {
      return response.body();
    }
    throw new RuntimeException("Failed to fetch movie info for - " + title);
  }
}
