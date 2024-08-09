package com.github.learntocode2013;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class DistributedDatabase {
  private static final Logger logger = Logger.getLogger(DistributedDatabase.class.getName());
  private final List<Node> nodes = new ArrayList<>();

  public DistributedDatabase(List<String> hostIpAddresses) {
    Optional.ofNullable(hostIpAddresses)
        .ifPresent(addresses -> addresses.stream().map(Node::new).forEach(nodes::add));
  }

  public boolean write(int newValue) {
    try(var scope = new QuorumTaskScope(3)) {
      for(Node node : nodes) {
        scope.fork(() -> node.write(newValue));
      }
      scope.joinUntil(Instant.now().plusSeconds(2));
      return scope.isQuorumReached();
    } catch (TimeoutException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private record Node(String hostIp) {
    private static final Logger logger = Logger.getLogger(Node.class.getName());

    boolean write(int newValue) {
        logger.info(() -> "Writing " + newValue + " to node with address " + hostIp());
        return true;
      }
    }

  public static void main(String[] args){
    var subject = new DistributedDatabase(List.of(
        "10.172.26.31",
        "10.172.26.28",
        "10.172.26.27",
        "10.172.26.26")
    );
    boolean isWriteSuccess = subject.write(10);
    logger.info(() -> "Did write operation succeed ? " + isWriteSuccess);
  }
}
