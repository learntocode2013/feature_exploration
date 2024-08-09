package com.github.learntocode2013;


import java.time.Duration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class DemoSealedClasses {
  @Test
  @SneakyThrows
  public void demoInheritance() {
    var rtl = new RedLight();
    rtl.setActive(true);
    printTrafficLightStatus(rtl);

    for(var i = 0; i < 10; i++) {
      System.out.print("x x x ");
      Thread.sleep(Duration.ofSeconds(1));
    }

    System.out.println();
    var gtl = new GreenLight();
    gtl.setActive(true);
    printTrafficLightStatus(gtl);
    System.out.print("> > > > > >");
  }

  void printTrafficLightStatus(TrafficLight light) {
    System.out.printf("%s light is %s right now...%n", light.name(), light.isOn() ? "On" : "Off");
  }
}
