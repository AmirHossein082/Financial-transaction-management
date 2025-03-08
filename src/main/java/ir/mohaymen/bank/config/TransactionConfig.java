package ir.mohaymen.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransactionConfig {
    @Value("${transaction.fee.x}")
    private float x;

    @Value("${transaction.fee.y}")
    private int y;

    @Value("${transaction.fee.z}")
    private int z;

    @Value("${bankAccountNumber}")
    private String bankAccountNumber;

    public float getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
}
