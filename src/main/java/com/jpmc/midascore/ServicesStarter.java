package com.jpmc.midascore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;

@Component
public class ServicesStarter {
  private static final Logger logger = LoggerFactory.getLogger(ServicesStarter.class);
  private Process externalProcess;

  @EventListener(ApplicationReadyEvent.class)
  public void startServices() {
    try {
      ProcessBuilder pb = new ProcessBuilder("java", "-jar", "services/transaction-incentive-api.jar");
      pb.inheritIO();
      externalProcess = pb.start();
      logger.info("Started incentives service process (pid unknown). Waiting for port 8080 to become available...");

      // wait for port 8080 to be available
      Instant end = Instant.now().plus(Duration.ofSeconds(20));
      boolean ok = false;
      while (Instant.now().isBefore(end)) {
        try (Socket s = new Socket()) {
          s.connect(new InetSocketAddress("localhost", 8080), 1000);
          ok = true;
          break;
        } catch (IOException e) {
          Thread.sleep(500);
        }
      }
      if (ok) {
        logger.info("Incentives service appears to be up on port 8080.");
      } else {
        logger.warn("Timed out waiting for incentives service to become available on port 8080.");
      }

    } catch (Exception e) {
      logger.warn("Failed to start external incentives service: {}", e.getMessage());
    }
  }

  @PreDestroy
  public void stopServices() {
    if (externalProcess != null && externalProcess.isAlive()) {
      logger.info("Stopping incentives service process");
      externalProcess.destroy();
    }
  }
}
