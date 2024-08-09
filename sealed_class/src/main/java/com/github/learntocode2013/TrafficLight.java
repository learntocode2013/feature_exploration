package com.github.learntocode2013;

public sealed interface TrafficLight permits Experimental, GreenLight, RedLight, YellowLight {
  boolean isOn();
  boolean isOff();
  String name();
}
