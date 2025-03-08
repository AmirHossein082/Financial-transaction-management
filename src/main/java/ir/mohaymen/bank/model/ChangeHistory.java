package ir.mohaymen.bank.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String modifiedBy; // نام کاربری که تغییرات را انجام داده

    private LocalDateTime changeDate; // تاریخ و ساعت تغییر

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }


    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }
}

