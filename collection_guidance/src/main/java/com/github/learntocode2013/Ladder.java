package com.github.learntocode2013;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import lombok.Getter;

record Person(String name, int age) {}

public class Ladder {
  private static final Logger logger = Logger.getLogger(Ladder.class.getName());
  // Two parallel lists - below
  private final List<Person> people;
  @Getter
  private final List<Integer> rankings;

  public Ladder(List<Person> persons, List<Integer> rankings) {
    this.people = persons;
    this.rankings = rankings;
  }

  public void exchange(Person winner, Person loser) {
    int winnerLocation = people.indexOf(winner);
    int loserLocation  = people.indexOf(loser);
    int winnerRanking = rankings.get(loserLocation);
    rankings.set(winnerLocation, winnerRanking);
    rankings.set(loserLocation, winnerRanking + 1);
  }

  public static void main(String[] args){
    var george = new Person("George", 33);
    var john   = new Person("John", 31);
    var paul = new Person("Paul", 30);

    // George -> index 0 -> rank 2
    // John -> index 1 -> rank 3
    // Paul -> index 2 -> rank 1
    var ladder = new Ladder(new ArrayList<>(List.of(george, john, paul)),
        new ArrayList<>(List.of(2, 3, 1)));

    ladder.exchange(john, george);
    if (ladder.getRankings().equals(List.of(3, 2, 1))) {
      logger.info(() -> "Ranking is proper. Assertion passed.");
    } else {
      logger.severe(() -> "Ranking is not proper. Assertion failed");
    }
  }
}
