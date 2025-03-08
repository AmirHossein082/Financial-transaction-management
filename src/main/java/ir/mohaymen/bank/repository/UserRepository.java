package ir.mohaymen.bank.repository;

import ir.mohaymen.bank.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
