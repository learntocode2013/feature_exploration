package com.github.learntocode2013;

import java.time.Duration;
import java.util.List;
import lombok.SneakyThrows;

public class FollowersRepository {
  @SneakyThrows
  public List<Follower> findFollowersByUserId(String id) {
    System.out.printf("About to fetch followers of user: %s %n", id);
    Thread.sleep(Duration.ofSeconds(2));
    throw new RuntimeException("Datastore un-reachable !!!");
//    return List.of(new Follower("Elon Musk"));
  }

  @SneakyThrows
  public UserFollowersCount findFollowersCountByUserId(String id) {
    System.out.printf("About to fetch follower count for user: %s %n", id);
    Thread.sleep(Duration.ofSeconds(2));
    return new UserFollowersCount(100);
  }
}
