package ir.mohaymen.bank.repository;

import ir.mohaymen.bank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByNationalId(String nationalId);
}
