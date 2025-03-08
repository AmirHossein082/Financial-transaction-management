package ir.mohaymen.bank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.mohaymen.bank.model.Transaction;
import ir.mohaymen.bank.repository.AccountRepository;
import ir.mohaymen.bank.repository.TransactionRepository;
import ir.mohaymen.bank.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/api")
public class TransactionController {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @GetMapping
    public String mainMenu(){
        logger.info("This is main menu!");
        return "mainMenu";
    }

    @GetMapping("/transaction")
    public String transactionMenu(){
        logger.info("Accessed transaction menu.");
        return "transactionMenu";
    }

    @GetMapping("/transaction/deposit")
    public String depositAccountNumber(Model model) {
        model.addAttribute("transaction", new Transaction());
        logger.info("Navigating to deposit transaction form.");
        return "depositTransaction";
    }

    @PostMapping("/transaction/deposit")
    public String depositAction(@ModelAttribute Transaction transaction, Model model) throws JsonProcessingException {
        logger.info("Processing deposit transaction: {}", transaction);
        model.addAttribute("transaction", transaction);
        return transactionService.handleDeposit(transaction);
    }

    @GetMapping("/transaction/withdraw")
    public String withdrawAccountNumber(Model model) {
        logger.info("Navigating to withdraw transaction form.");
        model.addAttribute("transaction", new Transaction());
        return "withdrawTransaction";
    }

    @PostMapping("/transaction/withdraw")
    public String withdrawAction(@ModelAttribute Transaction transaction, Model model) throws JsonProcessingException {
        logger.info("Processing withdrawal transaction: {}", transaction);
        model.addAttribute("transaction", transaction);
        return transactionService.handelWithdraw(transaction);
    }

    @GetMapping("/transaction/transfer")
    public String transferAccountNumbers(Model model){
        model.addAttribute("transaction", new Transaction());
        logger.info("Navigating to transfer transaction form.");
        return "transferTransaction";
    }

    @PostMapping("/transaction/transfer")
    public String transfer(@ModelAttribute Transaction transaction, Model model) throws JsonProcessingException {
        logger.info("Processing transfer transaction: {}", transaction);
        model.addAttribute("transaction", transaction);
        return transactionService.handleTransfer(transaction);
    }

    @GetMapping("/transaction/archive")
    public String getTransactions(
            @RequestParam(required = false) String originatingAccountNumber,
            @RequestParam(required = false) String destinationAccountNumber,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String amount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model){
        logger.info("Fetching transactions with filters - Originating Account: {}, Destination Account: {}, Type: {}, Amount: {}, Start Date: {}, End Date: {}, Page: {}, Size: {}",
                originatingAccountNumber, destinationAccountNumber, type, amount, startDate, endDate, page, size);

        Page<Transaction> transactionPage = transactionService.getTransactions(type, originatingAccountNumber,
                destinationAccountNumber, amount, startDate, endDate, PageRequest.of(page, size));

        model.addAttribute("transaction", transactionPage.getContent());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("originatingAccountNumber", originatingAccountNumber);
        model.addAttribute("destinationAccountNumber", destinationAccountNumber);
        model.addAttribute("type", type);
        model.addAttribute("amount", amount);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        logger.info("Transaction archive loaded. Total transactions found: {}", transactionPage.getTotalElements());
        return "filterForm";
    }

    @GetMapping("/transaction/transactionTracking")
    public String tracking(Model model){
        model.addAttribute("transaction", new Transaction());
        logger.info("Navigating to transaction tracking form.");
        return "transactionTracking";
    }

    @GetMapping("/transaction/trackingInformation")
    public String getTrackingInformation(@ModelAttribute("transaction") Transaction newTransaction, Model model){
        logger.info("Tracking transaction with tracking code: {}", newTransaction.getTrackingCode());
        Transaction transaction = transactionRepository.findByTrackingCode(newTransaction.getTrackingCode());
        if (transaction == null){
            logger.warn("Invalid tracking code: {}", newTransaction.getTrackingCode());
            return "invalidTrackingCode";
        }
        model.addAttribute("transaction", transaction);
        if (transaction.getDone()) {
            logger.info("Transaction with tracking code {} is completed.", newTransaction.getTrackingCode());
            return "trueTrackingInformation";
        }else {
            logger.info("Transaction with tracking code {} is pending.", newTransaction.getTrackingCode());
            return "falseTrackingInformation";
        }
    }
}
