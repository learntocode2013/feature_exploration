package com.github.learntocode2013;

import java.time.Duration;

public record PhoneTask(String name, String number, Duration duration) implements Task {}
