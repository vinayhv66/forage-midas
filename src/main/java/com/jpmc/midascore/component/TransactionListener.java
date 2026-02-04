package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionListener {
  private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final org.springframework.web.client.RestTemplate restTemplate;

  public TransactionListener(UserRepository userRepository, TransactionRepository transactionRepository,
      org.springframework.web.client.RestTemplate restTemplate) {
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.restTemplate = restTemplate;
  }

  @KafkaListener(topics = "${general.kafka-topic}")
  @Transactional
  public void receive(String message) {
    logger.info("Received raw transaction line: {}", message);
    try {
      String[] parts = message.split(",\\s*");
      long senderId = Long.parseLong(parts[0]);
      long recipientId = Long.parseLong(parts[1]);
      float amount = Float.parseFloat(parts[2]);

      UserRecord sender = userRepository.findById(senderId);
      UserRecord recipient = userRepository.findById(recipientId);

      if (sender == null) {
        logger.info("Discarding transaction: unknown sender id {}", senderId);
        return;
      }
      if (recipient == null) {
        logger.info("Discarding transaction: unknown recipient id {}", recipientId);
        return;
      }

      if (sender.getBalance() >= amount) {
        // build transaction for incentive API
        com.jpmc.midascore.foundation.Transaction tx = new com.jpmc.midascore.foundation.Transaction(senderId,
            recipientId, amount);
        float incentiveAmount = 0.0f;
        try {
          com.jpmc.midascore.foundation.Incentive incentive = restTemplate
              .postForObject("http://localhost:8080/incentive", tx, com.jpmc.midascore.foundation.Incentive.class);
          if (incentive != null) {
            incentiveAmount = incentive.getAmount();
          }
        } catch (Exception e) {
          logger.warn("Failed to call incentive API for transaction {}: {}", tx, e.getMessage());
          incentiveAmount = 0.0f;
        }

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmount);
        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord tr = new TransactionRecord(sender, recipient, amount, incentiveAmount);
        transactionRepository.save(tr);

        logger.info(
            "Processed transaction: {} -> {} amount {} incentive {} (sender {} new balance={}, recipient {} new balance={})",
            sender.getName(), recipient.getName(), amount, incentiveAmount,
            sender.getName(), sender.getBalance(), recipient.getName(), recipient.getBalance());

        // helper log so we can inspect wilbur's balance during testing (wilbur has id 9
        // in test data)
        try {
          UserRecord wilbur = userRepository.findById(9);
          if (wilbur != null) {
            logger.info("WILBUR_BALANCE: {}", wilbur.getBalance());
          }
        } catch (Exception ignore) {
        }
      } else {
        logger.info("Discarding transaction from {} to {} for {}: insufficient funds (balance={})",
            sender.getName(), recipient.getName(), amount, sender.getBalance());
      }

    } catch (Exception e) {
      logger.warn("Failed to process transaction line '{}': {}", message, e.getMessage());
    }
  }
}
