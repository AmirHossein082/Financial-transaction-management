package ir.mohaymen.bank.kafka;

import ir.mohaymen.bank.model.Transaction;
import ir.mohaymen.bank.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.io.IOException;


@Service
public class TransactionConsumer {

    @Autowired
    private TransactionRepository transactionRepository; // Service to handle DB operations

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON deserialization

    @KafkaListener(topics = "transaction-topic", groupId = "transaction-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransaction(String record) {
        try {
            Transaction transactions = objectMapper.readValue(record, Transaction.class);

            // Save transaction to PostgreSQL
            transactionRepository.save(transactions);

            System.out.println("✅ Transaction saved: " + transactions);
        } catch (IOException e) {
            System.err.println("❌ Error deserializing transaction: " + e.getMessage());
        }
    }
}

