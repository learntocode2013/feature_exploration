package com.github.learntocode2013;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import lombok.SneakyThrows;

public class UserInfoRepository {
  @SneakyThrows
  public UserInfo findUserInfoById(String id) {
    System.out.printf("About to fetch info of user: %s %n", id);
    Thread.sleep(Duration.ofSeconds(5));
    return new UserInfo(
        "Dibakar",
        "learntocode2013",
        LocalDate.of(1984, Month.FEBRUARY, 20)
    );
  }
}
