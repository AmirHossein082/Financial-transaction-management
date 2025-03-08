package ir.mohaymen.bank.service;

import ir.mohaymen.bank.model.Account;
import ir.mohaymen.bank.model.AccountState;
import ir.mohaymen.bank.model.ChangeHistory;
import ir.mohaymen.bank.model.Customer;
import ir.mohaymen.bank.repository.AccountRepository;
import ir.mohaymen.bank.repository.ChangeHistoryRepository;
import ir.mohaymen.bank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

@Service
public class AccountService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChangeHistoryRepository changeHistoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public String saveCustomer(Customer customer) throws ParseException {
        logger.info("Entering saveCustomer() for National ID: {}", customer.getNationalId());
        Customer existingCustomer = customerRepository.findByNationalId(customer.getNationalId());
        if (existingCustomer != null) {
            logger.warn("Save customer error: Duplicate National ID - {}", customer.getNationalId());
            return "duplicateNationalId";
        }

        if (!isValidBirthDate(customer.getBirthDate())) {
            logger.warn("Save customer error: Invalid Birth Date - {}", customer.getBirthDate());
            return "invalidBirthDate";
        }
        String modifiedBy = getAuthenticatedUserName();

        Account account = new Account();
        account.setCustomer(customer);
        String accountNumber = generateAccountNumber();
        account.setAccountNumber(accountNumber);
        account.setCreatedAt(new Date().toString());
        account.setAccountState(AccountState.ACTIVE.toString());
        customer.setAccountNumber(accountNumber);
        customer.setLastModifiedBy(modifiedBy);
        customer.setLastModifiedDate(LocalDateTime.now().toString());
        customer.setUpdatedBy(modifiedBy);

        customerRepository.save(customer);
        accountRepository.save(account);
        logger.info("Customer saved successfully with Account Number: {}", accountNumber);

        return "result";

    }
    private boolean isValidBirthDate(String birthDate) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("mm-dd-yyyy");
        Date date = format.parse(birthDate);

        return date.before(new Date());
    }

    private String generateAccountNumber() {
        Random random = new Random();
        long number = 10000000000000L + (long) (random.nextDouble() * 90000000000000L);
        return String.valueOf(number);
    }

    @Transactional
    public String updateAccount(Account existingAccount, Customer existingCustomer, Customer customer, String accountState) throws ParseException {
        logger.info("Entering updateAccount() for Account Number: {}", existingAccount.getAccountNumber());
        if (!isValidBirthDate(customer.getBirthDate())) {
            logger.warn("Update error: Invalid birth date - {}", customer.getBirthDate());
            return "invalidBirthDate";
        }

        if (accountState != null) {
            logger.info("Account state updated to: {}", accountState);
            existingAccount.setAccountState(accountState);
        }
        updateCustomerFields(existingCustomer, customer);

        String modifiedBy = getAuthenticatedUserName();

        existingCustomer.setLastModifiedBy(modifiedBy);
        existingCustomer.setLastModifiedDate(LocalDateTime.now().toString());
        existingCustomer.setUpdatedBy(modifiedBy);

        ChangeHistory changeHistory = new ChangeHistory();
        changeHistory.setCustomer(existingCustomer);
        changeHistory.setModifiedBy(modifiedBy);
        changeHistory.setChangeDate(LocalDateTime.now());

        changeHistoryRepository.save(changeHistory);
        customerRepository.save(existingCustomer);
        logger.info("Customer update successful for Account Number: {}", existingAccount.getAccountNumber());
        return "result";
    }

    private String getAuthenticatedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    private void updateCustomerFields(Customer existingCustomer, Customer customer){
        if (customer.getFirstName() != null) {
            existingCustomer.setFirstName(customer.getFirstName());
        }
        if (customer.getLastName() != null) {
            existingCustomer.setLastName(customer.getLastName());
        }
        if (customer.getAddress() != null) {
            existingCustomer.setAddress(customer.getAddress());
        }
        if (customer.getBirthDate() != null) {
            existingCustomer.setBirthDate(customer.getBirthDate());
        }
        if (customer.getNationalId() != null) {
            existingCustomer.setNationalId(customer.getNationalId());
        }
        if (customer.getMobileNumber() != null) {
            existingCustomer.setMobileNumber(customer.getMobileNumber());
        }
        if (customer.getPostalCode() != null) {
            existingCustomer.setPostalCode(customer.getPostalCode());
        }
        if (customer.getUserType() != null) {
            existingCustomer.setUserType(customer.getUserType());
        }
        logger.debug("Updated customer fields: {}", existingCustomer);
    }

}
