package ir.mohaymen.bank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.mohaymen.bank.config.TransactionConfig;
import ir.mohaymen.bank.controller.TransactionSpecification;
import ir.mohaymen.bank.kafka.TransactionProducer;
import ir.mohaymen.bank.model.Account;
import ir.mohaymen.bank.model.AccountState;
import ir.mohaymen.bank.model.Transaction;
import ir.mohaymen.bank.model.TransactionType;
import ir.mohaymen.bank.repository.AccountRepository;
import ir.mohaymen.bank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionConfig transactionConfig;

    @Autowired
    private TransactionProducer transactionProducer;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);


    public String handleDeposit(Transaction transaction) throws JsonProcessingException {

        long amount = transaction.getAmount();
        String destinationAccountNumber = transaction.getDestinationAccountNumber();
        logger.info("Handling deposit: destinationAccount={} amount={}", destinationAccountNumber, amount);

        Account destinationDepositAccount = accountRepository.findByAccountNumber(destinationAccountNumber);
        if (destinationDepositAccount == null){
            logger.warn("Deposit failed: Account {} does not exist!", destinationAccountNumber);
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.DEPOSIT,
                    amount,"", destinationAccountNumber, false, "Account not exist!"));

            return "accountNotExist";
        }
        if (!destinationDepositAccount.getAccountState().equals(AccountState.ACTIVE.toString())){
            logger.warn("Deposit failed: Account {} is not active!", destinationAccountNumber);
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.DEPOSIT,
                    amount, "", destinationAccountNumber, false, "Account status not active!"));
            return "notActiveAccount";
        }
        long balance = destinationDepositAccount.getAccountBalance() + amount;
            destinationDepositAccount.setAccountBalance(balance);
            makeTransaction(transaction, TransactionType.DEPOSIT, amount, "",
                    destinationAccountNumber, true, "no error!");

        transactionProducer.sendTransaction(transaction);
        accountRepository.save(destinationDepositAccount);
        logger.info("Deposit successful: destinationAccount={} newBalance={}", destinationAccountNumber, balance);
        return "result";
    }

    public String handelWithdraw(Transaction transaction) throws JsonProcessingException {
        long amount = transaction.getAmount();
        String originatingAccountNumber = transaction.getOriginatingAccountNumber();
        logger.info("Handling withdrawal: originatingAccount={} amount={}", originatingAccountNumber, amount);

        Account originatingWithdrawAccount = accountRepository.findByAccountNumber(originatingAccountNumber);
        if (originatingWithdrawAccount == null){
            logger.warn("Withdrawal failed: Account {} does not exist!", originatingAccountNumber);
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.WITHDRAW,
                    amount, originatingAccountNumber, "", false, "Account not exist!"));
            return "accountNotExist";
        }
        if (!originatingWithdrawAccount.getAccountState().equals(AccountState.ACTIVE.toString())){
            logger.warn("Withdrawal failed: Account {} is not active!", originatingAccountNumber);
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.WITHDRAW,
                    amount, originatingAccountNumber, "", false, "Account status not active!"));
            return "notActiveAccount";
        }
        if (!isBalanceBiggerThanAmount(originatingWithdrawAccount, amount)){
            logger.warn("Withdrawal failed: Insufficient funds in account {}", originatingAccountNumber);
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.WITHDRAW,
                    amount, originatingAccountNumber, "", false, "Amount is bigger than account balance!"));
            return "amountTooBig";
        }
        long balance = originatingWithdrawAccount.getAccountBalance() - amount;

        originatingWithdrawAccount.setAccountBalance(balance);
        makeTransaction(transaction, TransactionType.WITHDRAW, amount, originatingAccountNumber,
                "", true, "no error!");

        transactionProducer.sendTransaction(transaction);
        accountRepository.save(originatingWithdrawAccount);
        logger.info("Withdrawal successful: originatingAccount={} newBalance={}", originatingAccountNumber, balance);
        return "result";
    }

    public String handleTransfer(Transaction transaction) throws JsonProcessingException {
        long amount = transaction.getAmount();
        String originatingAccountNumber = transaction.getOriginatingAccountNumber();
        String destinationAccountNumber = transaction.getDestinationAccountNumber();
        logger.info("Handling transfer: from={} to={} amount={}", originatingAccountNumber, destinationAccountNumber, amount);

        Account originatingAccount = accountRepository.findByAccountNumber(originatingAccountNumber);
        Account destinationAccount = accountRepository.findByAccountNumber(destinationAccountNumber);

        if (originatingAccount == null || destinationAccount == null){
            logger.warn("Transfer failed: One or both accounts do not exist!");
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.TRANSFER,
                    amount, originatingAccountNumber, destinationAccountNumber, false, "Account not exist!"));
            return "accountNotExist";
        }
        if (!originatingAccount.getAccountState().equals(AccountState.ACTIVE.toString()) ||
            !destinationAccount.getAccountState().equals(AccountState.ACTIVE.toString())){
            logger.warn("Transfer failed: One or both accounts are not active!");
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.TRANSFER,
                    amount, originatingAccountNumber, destinationAccountNumber, false, "Account status not active!"));
            return "notActiveAccount";
        }
        if (Objects.equals(originatingAccountNumber, destinationAccountNumber)){
            logger.warn("Transfer failed: Originating and destination account numbers are the same!");
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.TRANSFER,
                    amount, originatingAccountNumber, destinationAccountNumber, false, "Originating account number and Destination account number are equal!"));
            return "equalAccountNumber";
        }
        int fee = feeCalculator(amount);
        long amountAndFee = amount + fee;

        if (!isBalanceBiggerThanAmount(originatingAccount, amountAndFee)){
            logger.warn("Transfer failed: Insufficient balance in account {}", originatingAccountNumber);
            transactionProducer.sendTransaction(makeTransaction(transaction, TransactionType.TRANSFER,
                    amount, originatingAccountNumber, destinationAccountNumber, false, "Amount is bigger than balance!"));
            return "amountTooBig";
        }
        long originatingAccountBalance = originatingAccount.getAccountBalance() - amountAndFee;
        long destinationAccountBalance = destinationAccount.getAccountBalance() + amount;

        handleBankTransaction(fee, originatingAccountNumber);

        originatingAccount.setAccountBalance(originatingAccountBalance);
        destinationAccount.setAccountBalance(destinationAccountBalance);

        makeTransaction(transaction, TransactionType.TRANSFER, amount, originatingAccountNumber,
                destinationAccountNumber, true, "no error!");
        accountRepository.save(originatingAccount);
        accountRepository.save(destinationAccount);
        transactionProducer.sendTransaction(transaction);
        logger.info("Transfer successful: from={} to={} amount={} fee={} newOriginatingBalance={}",
                originatingAccountNumber, destinationAccountNumber, amount, fee, originatingAccountBalance);
        return "result";
    }

    private String generateTrackingCode() {
        // Get current time in milliseconds
        long timestamp = System.currentTimeMillis();

        // Generate a unique random string (UUID)
        String randomPart = UUID.randomUUID().toString().replace("-", "");

        // Combine timestamp and random string to form the tracking code
        return timestamp + "-" + randomPart;
    }

    private Boolean isBalanceBiggerThanAmount(Account account, long amount){
        long accountBalance = account.getAccountBalance();
        if (accountBalance >= amount)
            return true;
        else
            return false;
    }

    private int feeCalculator(long amount){
        float x = transactionConfig.getX();
        int y = transactionConfig.getY();
        int z = transactionConfig.getZ();
        int fee;

        if (amount < y){
            fee = 500;
        } else if (amount >= y && amount < z){
            fee = (int)(x * amount);
        } else {
            fee = 10000;
        }
        return fee;
    }

    private void handleBankTransaction(int fee, String originatingAccountNumber){
        String bankAccountNumber = transactionConfig.getBankAccountNumber();
        handleBankAccount(fee, bankAccountNumber);
        handleFeeTransaction(fee, originatingAccountNumber, bankAccountNumber);
    }

    private void handleBankAccount(int fee, String bankAccountNumber){

        Account bankAccount = accountRepository.findByAccountNumber(bankAccountNumber);

        if (bankAccount == null){
            bankAccount = new Account();
            bankAccount.setAccountNumber(bankAccountNumber);
            bankAccount.setAccountState(AccountState.ACTIVE.toString());
            bankAccount.setCustomer(null);
            bankAccount.setCreatedAt(LocalDateTime.now().toString());
        }
        bankAccount.setAccountBalance(bankAccount.getAccountBalance() + fee);

        accountRepository.save(bankAccount);
    }

    private void handleFeeTransaction(int fee, String originatingAccountNumber, String bankAccountNumber){
        Transaction feeTransaction = new Transaction();
        makeTransaction(feeTransaction, TransactionType.FEE_WITHDRAW, fee, originatingAccountNumber,
                bankAccountNumber, true, "no error");
        transactionRepository.save(feeTransaction);
    }

    public Page<Transaction> getTransactions(String type, String originatingAccountNumber, String destinationAccountNumber,
                                String amount, String startDate,String endDate, Pageable pageable){
        Specification<Transaction> spec = Specification.where(TransactionSpecification.hasOriginatingAccountNumber(originatingAccountNumber))
                .and(TransactionSpecification.hasDestinationAccountNumber(destinationAccountNumber))
                .and(TransactionSpecification.hasAmount(amount))
                .and(TransactionSpecification.hasDateBetween(startDate, endDate))
                .and(TransactionSpecification.hasType(type));

        return transactionRepository.findAll(spec, pageable);
    }

    private Transaction makeTransaction(Transaction transaction, TransactionType type, long amount,
                                        String originatingAccountNumber, String destinationAccountNumber, Boolean isDone, String error){
        transaction.setTrackingCode(generateTrackingCode());
        transaction.setDone(isDone);
        transaction.setAmount(amount);
        transaction.setType(type.toString());
        transaction.setDate(LocalDateTime.now().toString());
        transaction.setDestinationAccountNumber(destinationAccountNumber);
        transaction.setOriginatingAccountNumber(originatingAccountNumber);
        transaction.setError(error);

        return transaction;
    }

}
