package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Balance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class DevTaskFiveOutputCaptureTest {

  @Autowired
  private KafkaProducer kafkaProducer;

  @Autowired
  private UserPopulator userPopulator;

  @Autowired
  private FileLoader fileLoader;

  @Autowired
  private BalanceQuerier balanceQuerier;

  @Test
  void capture_task_five_output() throws InterruptedException {
    userPopulator.populate();
    String[] transactionLines = fileLoader.loadStrings("/test_data/rueiwoqp.tyruei");
    for (String transactionLine : transactionLines) {
      kafkaProducer.send(transactionLine);
    }
    Thread.sleep(2000);

    StringBuilder sb = new StringBuilder();
    sb.append("---begin output ---\n");
    for (int i = 0; i < 13; i++) {
      Balance balance = balanceQuerier.query((long) i);
      sb.append(balance.toString()).append("\n");
      System.out.println(balance.toString());
    }
    sb.append("---end output ---\n");

    try {
      Path outDir = Path.of("target");
      Files.createDirectories(outDir);
      Path out = outDir.resolve("taskfive-output.txt");
      Files.writeString(out, sb.toString(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.print(sb.toString());
  }
}
