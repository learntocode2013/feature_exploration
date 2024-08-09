package com.github.learntocode2013;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.Future.State;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdk.incubator.concurrent.ScopedValue;
import jdk.incubator.concurrent.StructuredTaskScope;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
@Getter
public class CustomStructuredTaskScope {
  private static final Logger logger = Logger.getLogger(CustomStructuredTaskScope.class.getName());
  private static final ScopedValue<String> USER = ScopedValue.newInstance();
  private static final ScopedValue<String> LOC  = ScopedValue.newInstance();
  private static final ScopedValue<String> DEST = ScopedValue.newInstance();
  private static final ScopedValue<Double> CAR_ONE_DISCOUNT = ScopedValue.newInstance();
  private static final ScopedValue<Boolean> PUBLIC_TRANSPORT_TICKET = ScopedValue.newInstance();
  private static final String HEADER = "----------- Travel recommendation(s) for %s------";
  private String loggedInUser;

  public CustomStructuredTaskScope(final String user) {
    this.loggedInUser = user;
  }

  public static void main(String[] args){
    var source = "Bangalore"; var dest = "Mumbai";
    var subject = new CustomStructuredTaskScope();

    logger.info(() -> String.format(HEADER,
        Optional.ofNullable(subject.getLoggedInUser()).orElse("guest user")));

    TravelOffer travelOffer =
        Optional.ofNullable(subject.getLoggedInUser())
            .map(
                user -> {
                  try {
                    return ScopedValue.where(USER, subject.getLoggedInUser())
                        .call(() -> subject.fetchTravelOffers(source, dest));
                  } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                  }
                })
            .orElseGet(() -> subject.fetchTravelOffers(source, dest));

    logger.info(() -> "Travel offer for you: " + travelOffer);
  }

  @SneakyThrows
  public TravelOffer fetchTravelOffers(String loc, String dest) {
    return ScopedValue
        .where(LOC, loc)
        .where(DEST, dest)
        .call(() -> {
          try (var sts = new TravelScope()) {
            if (USER.isBound()) {
              sts.fork(this::fetchRideSharingOffers);
            } else {
              logger.warning(() -> "Ride sharing offers can only be applied to logged in users !!!");
            }
            sts.fork(() -> ScopedValue
                .where(PUBLIC_TRANSPORT_TICKET, true)
                .call(this::fetchPublicTransportOffers)
            );

            sts.joinUntil(Instant.now().plusSeconds(10));
            return sts.recommendedTravelOffer();
          }
        });
  }

  @SneakyThrows
  public PublicTransportOffer fetchPublicTransportOffers() {
    logger.info(() -> "Fetching public transport offer for " + USER.orElse("anonymous"));
    try(var cSts = new PublicTransportScope()) {
      cSts.fork(PublicTransport::busTransportServer);
      cSts.fork(PublicTransport::trainTransportServer);
      cSts.fork(PublicTransport::subwayTransportServer);
      cSts.fork(PublicTransport::tramTransportServer);
      cSts.joinUntil(Instant.now().plusSeconds(1));
      return cSts.recommendedPublicTransport();
    }
  }

  @SneakyThrows
  public RideSharingOffer fetchRideSharingOffers() {
    logger.info(
        () ->
            "Fetching ride sharing options for " +
                USER.orElseThrow(() -> new RuntimeException("No logged in user found !!!")));
    try(var sts = new PrivateTransportScope()){
      sts.fork(RideSharing::uberServer);
      sts.fork(RideSharing::olaServer);
      sts.fork(() -> ScopedValue
          .where(CAR_ONE_DISCOUNT, 0.5)
          .call(RideSharing::topCarServer));

      sts.joinUntil(Instant.now().plusSeconds(1));
      return sts.recommendedRideShareOffer();
    }
  }

  public record TravelOffer(RideSharingOffer rOffer, PublicTransportOffer ptOffer) {}

  public record RideSharingOffer(String company,
                                 Duration minutesToYou,
                                 Duration minutesToDest,
                                 double price) implements Travel {};
  static class RideSharingException extends RuntimeException {
    RideSharingException() {
      super("Failed to fetch private ride");
    }
  }

  static class PublicTransportException extends RuntimeException {
    PublicTransportException() {
      super("Failed to fetch public transport ride");
    }
  }

  public record PublicTransportOffer(String transportType, String station, LocalTime goTime)
      implements Travel{};


  static class RideSharing {
    static RideSharingOffer uberServer() {
//      throw new RuntimeException("Uber server un-reachable");
      return new RideSharingOffer("Uber", Duration.ofMinutes(10), Duration.ofDays(2),
          priceForTravel(LOC.get(), DEST.get(), "Uber"));
    }

    static RideSharingOffer olaServer() {
//      throw new RuntimeException("Ola server un-reachable");
      return new RideSharingOffer("Ola", Duration.ofMinutes(10), Duration.ofDays(2),
          priceForTravel(LOC.get(), DEST.get(), "Ola"));
    }

    static RideSharingOffer topCarServer() {
      throw new RuntimeException("CarOne server un-reachable");
//      return new RideSharingOffer("CarOne", Duration.ofMinutes(10), Duration.ofDays(2),
//      priceForTravel(LOC.get(), DEST.get(), "CarOne"));
    }
  }

  class PublicTransport {
    static List<PublicTransportOffer> busTransportServer() {
      if (PUBLIC_TRANSPORT_TICKET.isBound() && PUBLIC_TRANSPORT_TICKET.get()) {
        return List.of(
            new PublicTransportOffer("bus", "Majestic", LocalTime.now().plusHours(2)),
            new PublicTransportOffer("bus", "Majestic", LocalTime.now().plusHours(4)),
            new PublicTransportOffer("bus", "Majestic", LocalTime.now().plusHours(6))
        );
      } else {
        return List.of();
      }
    }
    static List<PublicTransportOffer> subwayTransportServer() {
      if (PUBLIC_TRANSPORT_TICKET.isBound() && PUBLIC_TRANSPORT_TICKET.get()) {
        return List.of(
            new PublicTransportOffer("subway", "Majestic", LocalTime.now().plusHours(2)),
            new PublicTransportOffer("subway", "Majestic", LocalTime.now().plusHours(4)),
            new PublicTransportOffer("subway", "Majestic", LocalTime.now().plusHours(6))
        );
      } else {
        return List.of();
      }
    }
    static List<PublicTransportOffer> trainTransportServer() {
      if (PUBLIC_TRANSPORT_TICKET.isBound() && PUBLIC_TRANSPORT_TICKET.get()) {
        return List.of(
            new PublicTransportOffer("train", "Majestic", LocalTime.now().plusHours(1)),
            new PublicTransportOffer("train", "Majestic", LocalTime.now().plusHours(4)),
            new PublicTransportOffer("train", "Majestic", LocalTime.now().plusHours(12))
        );
      } else {
        return List.of();
      }
    }
    static List<PublicTransportOffer> tramTransportServer() {
      if (PUBLIC_TRANSPORT_TICKET.isBound() && PUBLIC_TRANSPORT_TICKET.get()) {
        return List.of(
            new PublicTransportOffer("tram", "Majestic", LocalTime.now().plusHours(4)),
            new PublicTransportOffer("tram", "Majestic", LocalTime.now().plusHours(6)),
            new PublicTransportOffer("tram", "Majestic", LocalTime.now().plusHours(8))
        );
      } else {
        return List.of();
      }
    }

  }

  class TravelScope extends StructuredTaskScope<Travel> {
    private RideSharingOffer rideSharingOffer;
    private RideSharingException rideSharingException;
    private PublicTransportOffer publicTransportOffer;
    private PublicTransportException publicTransportException;

    @Override
    protected void handleComplete(Future<Travel> future) {
      State state = future.state();
      switch (state) {
        case SUCCESS -> {
          switch (future.resultNow().getClass().getName().split("\\$")[1]) {
            case "RideSharingOffer" -> {
              rideSharingOffer = (RideSharingOffer)future.resultNow();
            }
            case "PublicTransportOffer" -> {
              publicTransportOffer = (PublicTransportOffer)future.resultNow();
            }
          }
        }
        case FAILED -> {
          Throwable exception = future.exceptionNow();
          switch (exception.getClass().getName().split("\\$")[1]) {
            case "RideSharingException" -> {
              rideSharingException = (RideSharingException) exception;
            }
            case "PublicTransportException" -> {
              publicTransportException = (PublicTransportException) exception;
            }
          }
        }
        case RUNNING -> {
          throw new IllegalStateException("Travel task is still running...");
        }
      }
    }

    TravelOffer recommendedTravelOffer() {
      return new TravelOffer(rideSharingOffer, publicTransportOffer);
    }
  }

  class PrivateTransportScope extends StructuredTaskScope<RideSharingOffer> {
    private List<RideSharingOffer> result = new CopyOnWriteArrayList<>();
    private List<Throwable> exceptions = new CopyOnWriteArrayList<>();

    @Override
    protected void handleComplete(Future<RideSharingOffer> future) {
      State rideShareTaskState = future.state();
      switch (rideShareTaskState) {
        case SUCCESS -> {
          result.add(future.resultNow());
        }
        case FAILED -> {
          exceptions.add(future.exceptionNow());
        }
        case RUNNING -> {
          throw new IllegalStateException("Task to find ride share offer is still running...");
        }
      }
    }

    RideSharingOffer recommendedRideShareOffer() {
      return result.stream()
          .min(Comparator.comparingDouble(RideSharingOffer::price))
          .orElseThrow(() -> {
            RideSharingException exceptionWrapper = new RideSharingException();
            exceptions.forEach(exceptionWrapper::addSuppressed);
            return exceptionWrapper;
          });
    }
  }

  class PublicTransportScope extends StructuredTaskScope<List<PublicTransportOffer>> {
    // We just want mutations to be thread safe.
    private final List<List<PublicTransportOffer>> result = new CopyOnWriteArrayList<>();
    private final List<Throwable> exceptions = new CopyOnWriteArrayList<>();

    @Override
    protected void handleComplete(Future<List<PublicTransportOffer>> future) {
      switch (future.state()) {
        case SUCCESS -> {
          result.add(future.resultNow());
        }
        case FAILED -> {
          exceptions.add(future.exceptionNow());
        }
        case RUNNING -> {
          throw new IllegalStateException("Task is not completed yet...");
        }
      }
    }

    public PublicTransportOffer recommendedPublicTransport() {
      return result.stream()
          .flatMap(Collection::stream)
          .min(Comparator.comparing(PublicTransportOffer::goTime))
          .orElseThrow(() -> {
            PublicTransportException wrappedExp = new PublicTransportException();
            exceptions.forEach(wrappedExp::addSuppressed);
            return wrappedExp;
          });
    }
  }

  public static double priceForTravel(String loc, String dest, String company) {
    return switch (company) {
      case "Uber" -> {
        if(loc.equals("Bangalore") && dest.equals("Mumbai")) {
          yield 10_000;
        } else {
          yield 9000;
        }
      }
      case "Ola" -> {
        if(loc.equals("Bangalore") && dest.equals("Mumbai")) {
          yield 11_000;
        } else {
          yield 9000;
        }
      }
      case "CarOne" -> {
        if(loc.equals("Bangalore") && dest.equals("Mumbai")) {
          yield 12_000;
        } else {
          yield 9000;
        }
      }
      default -> -1;
    };
  }
}
