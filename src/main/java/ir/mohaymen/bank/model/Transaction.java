package ir.mohaymen.bank.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    private long id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("originatingAccountNumber")
    private String originatingAccountNumber;
    @JsonProperty("destinationAccountNumber")
    private String destinationAccountNumber;
    @JsonProperty("amount")
    private long amount = 0;
    @JsonProperty("trackingCode")
    private String trackingCode;
    @JsonProperty("date")
    private String date;
    @JsonProperty("done")
    private Boolean isDone;

    private String error;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOriginatingAccountNumber() {
        return originatingAccountNumber;
    }

    public void setOriginatingAccountNumber(String originatingAccountNumber) {
        this.originatingAccountNumber = originatingAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getDone() {
        return isDone;
    }

    public void setDone(Boolean done) {
        isDone = done;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
