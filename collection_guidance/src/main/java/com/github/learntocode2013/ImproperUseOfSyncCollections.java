package com.github.learntocode2013;

import java.util.ArrayList;
import java.util.Collections;
import lombok.SneakyThrows;

public class ImproperUseOfSyncCollections {
  @SneakyThrows
  public static void main(String[] args){
    var people = new ArrayList<Person>();
    people.add(new Person("Dibakar", 40));
    people.add(new Person("Venket", 50));

    var threadSafePeople = Collections.synchronizedList(people);
    Runnable task =
        () -> {
            // Client side locking
//          synchronized (ImproperUseOfSyncCollections.class) {
            while (!threadSafePeople.isEmpty()) {
              System.out.println("" + Thread.currentThread().getName() + " read " + threadSafePeople.get(0));
              threadSafePeople.remove(0);
            }
//          }
        };
    var t1 = new Thread(task, "T1");
    var t2 = new Thread(task, "T2");

    t1.start();t2.start();
    t1.join();t2.join();
  }
}
