package com.github.learntocode2013;

import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserInfo {
  private final String name;
  private final String id;
  private final LocalDate birthDate;
}
