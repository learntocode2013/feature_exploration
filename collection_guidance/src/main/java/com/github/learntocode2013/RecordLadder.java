package com.github.learntocode2013;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import lombok.Getter;

record PlayerRanking(Person player, int ranking) {}

public class RecordLadder {
  private static final Logger logger = Logger.getLogger(RecordLadder.class.getName());

  @Getter
  final private List<PlayerRanking> ladder;

  public RecordLadder(List<Person> people, List<Integer> rankings) {
    this.ladder = new ArrayList<>();
    for(var i = 0; i < people.size(); i++) {
      ladder.add(new PlayerRanking(people.get(i), rankings.get(i)));
    }
  }

  public void exchange(Person winner, Person loser) {
    var winnerLocation = locate(winner);
    var loserLocation   = locate(loser);
    var winnerRanking = ladder.get(loserLocation).ranking();
    ladder.set(winnerLocation, new PlayerRanking(winner, winnerRanking));
    ladder.set(loserLocation, new PlayerRanking(loser, winnerRanking + 1));
  }

  private int locate(Person person) {
    for(var i = 0; i < ladder.size(); i++) {
      if(ladder.get(i).player().equals(person)) {
        return i;
      }
    }
    return -1;
  }

  public static void main(String[] args){
    var peter = new Person("Peter", 33);
    var paul = new Person("Paul", 31);
    var mary = new Person("Mary", 30);

    var recordLadder = new RecordLadder(
        new ArrayList<>(List.of(peter, paul, mary)),
        new ArrayList<>(List.of(2, 3, 1))
    );

    recordLadder.exchange(paul, peter);

    assert recordLadder.getLadder().get(recordLadder.locate(paul)).ranking() == 2
        &&  recordLadder.getLadder().get(recordLadder.locate(peter)).ranking() == 3;
    //Sorting is easier
    recordLadder.getLadder().sort(Comparator.comparingInt(pr -> pr.player().age()));
    assert recordLadder.getLadder().stream().map(l -> l.player().name()).toList().equals(List.of("Mary", "Paul", "Peter"));
  }
}
