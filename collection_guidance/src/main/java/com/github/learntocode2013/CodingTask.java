package com.github.learntocode2013;

import java.time.Duration;

public record CodingTask(String spec, Duration duration) implements Task {}
