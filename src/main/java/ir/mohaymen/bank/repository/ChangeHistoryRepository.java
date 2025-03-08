package ir.mohaymen.bank.repository;

import ir.mohaymen.bank.model.ChangeHistory;
import ir.mohaymen.bank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeHistoryRepository  extends JpaRepository<ChangeHistory, Long> {
    ChangeHistory findByCustomer(Customer customer);
}
