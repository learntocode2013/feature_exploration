package com.github.learntocode2013;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class CompleteUserProfile {
  private final String userId;
  private final String username;
  private final int age;
  private final List<Follower> followers;
  private final int followersCount;
}
