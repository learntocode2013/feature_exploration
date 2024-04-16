package com.github.learntocode2013;

import lombok.Getter;

@Getter
public class UserFollowersCount {
  private final int followersCount;

  public UserFollowersCount(int count) {
    this.followersCount = count;
  }
}
