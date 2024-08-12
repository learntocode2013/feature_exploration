package com.github.learntocode2013;

import java.util.List;

public record Portfolio(List<Stock> stocks, List<Crypto> cryptos, List<RealEstate> estates) {}
