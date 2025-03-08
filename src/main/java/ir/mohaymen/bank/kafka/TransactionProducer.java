package ir.mohaymen.bank.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.mohaymen.bank.model.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;


    public TransactionProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(Transaction transaction) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(transaction);
        kafkaTemplate.send("transaction-topic", jsonString);
    }
}
