package com.github.learntocode2013;

import lombok.Setter;

@Setter
public final class YellowLight implements TrafficLight {
  private boolean active = false;

  @Override
  public boolean isOn() {
    return active;
  }

  @Override
  public boolean isOff() {
    return !active;
  }

  @Override
  public String name() {
    return "Yellow";
  }
}