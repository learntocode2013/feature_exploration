package com.github.learntocode2013;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import jdk.incubator.concurrent.StructuredTaskScope;

public class NestedStructuredTaskScope {
  private static final Logger logger = Logger.getLogger(NestedStructuredTaskScope.class.getName());
  private static final String TOP_TASK_NAME  = "Portfolio review";
  private static final String CALC_TASK_NAME = "Valuation computation subtask";
  public static void main(String[] args){
    var portfolio = new Portfolio(
        List.of(new Stock("TWLO", 300_000)),
        List.of(new Crypto("BTC", 400_123)),
        List.of(new RealEstate("Hiranandani", 200_000)));
    var subject = new NestedStructuredTaskScope();
    subject.reviewPortfolio(portfolio);
  }

  public void reviewPortfolio(Portfolio portfolio) {
    try (var mainScope =
        new StructuredTaskScope.ShutdownOnFailure(TOP_TASK_NAME, Thread.ofVirtual().factory())) {
        var stockPricesFut  = mainScope.fork(() -> fetchStockPrices(portfolio.stocks()));
        var cryptoPricesFut = mainScope.fork(() -> fetchCryptoValues(portfolio.cryptos()));
        var estatePricesFut = mainScope.fork(() -> fetchRealEstateEstimates(portfolio.estates()));

        mainScope.join().throwIfFailed();

        var stockPrices  = stockPricesFut.get();
        var cryptoPrices = cryptoPricesFut.get();
        var estatePrices = estatePricesFut.get();

      try (var calculationScope =
          new StructuredTaskScope.ShutdownOnFailure(
              CALC_TASK_NAME, Thread.ofVirtual().factory())) {
        var totalValueFut      = calculationScope.fork(() -> calculateTotalValue(stockPrices,
            cryptoPrices, estatePrices));
        var assetAllocationFut = calculationScope.fork(() -> calculateAssetAllocation(stockPrices,
            cryptoPrices, estatePrices));
        var riskProfileFut     = calculationScope.fork(() -> assessRiskProfile(stockPrices,
            cryptoPrices, estatePrices));

        calculationScope.join().throwIfFailed();

        var totalValue = totalValueFut.get();
        var assetAllocation = assetAllocationFut.get();
        var riskProfile = riskProfileFut.get();

        logger.info(() -> "Total value of your assets: " + totalValue);
        logger.info(() -> "Your asset allocation: " + assetAllocation);
        logger.info(() -> "Your risk profile: " + riskProfile);
      }

    } catch (InterruptedException | ExecutionException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Map<String, Double> fetchStockPrices(List<Stock> stocks) {
    try(var scope = new CollectingTaskScope<Map<String, Double>>()) {
      stocks.forEach(stock -> scope.fork(() -> fetchStockPrice(stock)));
      scope.join();
      var results = scope.getResults();
      Map<String, Double> mergedMap = new HashMap<>();
      for(var stockPriceInfo : results) {
        mergedMap.putAll(stockPriceInfo);
      }
      return mergedMap;
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Map<String, Double> fetchStockPrice(Stock stock) {
    return Map.of(stock.ticker(), stock.price());
  }

  private Map<String, Double> fetchCryptoPrice(Crypto crypto) {
    return Map.of(crypto.symbol(), crypto.price());
  }

  private Map<String, Double> fetchRealEstatePrice(RealEstate estate) {
    return Map.of(estate.propertyName(), estate.valuation());
  }

  private Map<String, Double> fetchCryptoValues(List<Crypto> cryptos) {
    try(var scope = new CollectingTaskScope<Map<String, Double>>()) {
      cryptos.forEach(crypto -> scope.fork(() -> fetchCryptoPrice(crypto)));
      scope.join();
      var results = scope.getResults();
      Map<String, Double> mergedMap = new HashMap<>();
      for(var map : results) {
        mergedMap.putAll(map);
      }
      return mergedMap;
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Map<String, Double> fetchRealEstateEstimates(List<RealEstate> estates) {
    try(var scope = new CollectingTaskScope<Map<String, Double>>()) {
      estates.forEach(estate -> scope.fork(() -> fetchRealEstatePrice(estate)));
      scope.join();
      var results = scope.getResults();
      Map<String, Double> mergedMap = new HashMap<>();
      for(var map : results) {
        mergedMap.putAll(map);
      }
      return mergedMap;
    } catch (InterruptedException ex) {
        throw new RuntimeException(ex);
    }
  }

  private double calculateTotalValue(
      Map<String, Double> stockPrices,
      Map<String, Double> cryptoPrices,
      Map<String, Double> estatePrices) {
    return stockPrices.values().stream().reduce((a,b) -> a.doubleValue() + b.doubleValue()).get() +
        cryptoPrices.values().stream().reduce((a,b) -> a.doubleValue() + b.doubleValue()).get() +
        estatePrices.values().stream().reduce((a,b) -> a.doubleValue() + b.doubleValue()).get();
  }

  private Map<String, Double> calculateAssetAllocation(
      Map<String, Double> stockPrices,
      Map<String, Double> cryptoPrices,
      Map<String, Double> estatePrices
  ) {
    double netWorth = calculateTotalValue(stockPrices, cryptoPrices, estatePrices);
    double stocksWorth = stockPrices.values().stream().reduce((a, b) -> a.doubleValue() + b.doubleValue()).get();
    double cryptoWorth = cryptoPrices.values().stream().reduce((a, b) -> a.doubleValue() + b.doubleValue()).get();
    double estateWorth = estatePrices.values().stream().reduce((a, b) -> a.doubleValue() + b.doubleValue()).get();
    Map<String, Double> result = new HashMap<>();
    result.put("Stocks", (stocksWorth/netWorth)*100);
    result.put("Crypto", (cryptoWorth/netWorth)*100);
    result.put("Real Estate", (estateWorth/netWorth)*100);
    throw new RuntimeException("Failed to calculate asset allocation");
//    return result;
  }

  private String assessRiskProfile(
      Map<String, Double> stockPrices,
      Map<String, Double> cryptoPrices,
      Map<String, Double> estatePrices) {

    var assetAllocation = calculateAssetAllocation(stockPrices, cryptoPrices, estatePrices);

    if (assetAllocation.get("Crypto") > assetAllocation.get("Stocks")) {
      return "Risk taker";
    } else if(assetAllocation.get("Stocks") > assetAllocation.get("Real Estate")) {
      return "Aggressive";
    }
    return "Moderate";
  }

}
