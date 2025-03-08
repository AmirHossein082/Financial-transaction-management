package ir.mohaymen.bank.controller;


import ir.mohaymen.bank.model.Account;
import ir.mohaymen.bank.model.ChangeHistory;
import ir.mohaymen.bank.model.Customer;
import ir.mohaymen.bank.repository.AccountRepository;
import ir.mohaymen.bank.repository.ChangeHistoryRepository;
import ir.mohaymen.bank.repository.CustomerRepository;
import ir.mohaymen.bank.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;



@Controller
@RequestMapping("/api/customers")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChangeHistoryRepository changeHistoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);


    @GetMapping
    public String menu(){
        logger.info("This is account menu!");
        return "accountMenu";
    }

    @GetMapping("/create-account")
    public String accountForm(Model model) {
        logger.info("Navigating to account creation form.");
        model.addAttribute("customer", new Customer());
        return "accountForm";
    }

    @PostMapping("/create-account")
    public String formSubmit(@ModelAttribute Customer customer, Model model) throws Exception {
        model.addAttribute("customer", customer);
        logger.info("Received customer creation request: {}", customer);
        return accountService.saveCustomer(customer);
    }

    @GetMapping("/login")
    public String loginPage() {
        logger.info("Navigating to login page.");
        return "login";
    }

    @GetMapping("/updateForm")
    public String updateForm2(Model model){
        model.addAttribute("account", new Account());
        logger.info("Navigating to account update form.");
        return "accountNumberForm";
    }
    @PostMapping("/updateForm")
    public String showUpdateForm(@ModelAttribute("account") Account newAccount, Model model) {
        logger.info("Received account update request for Account Number: {}", newAccount.getAccountNumber());

        Account account = accountRepository.findByAccountNumber(newAccount.getAccountNumber());
        if (account == null){
            logger.error("Account not found: {}", newAccount.getAccountNumber());
            return "accountNotExist";
        }
        Customer customer = account.getCustomer();
        model.addAttribute("customer", customer);
        model.addAttribute("account",account);
        logger.info("Loaded account details for update.");
        return "updateForm";
    }
    @PostMapping("/update")
    public String updateCustomer(@ModelAttribute("customer") Customer customer,
                                 @RequestParam("accountNumber") String accountNumber,
                                 @RequestParam("accountState") String accountState,
                                 Model model) throws ParseException {
        logger.info("Processing account update: Account Number = {}, New State = {}", accountNumber, accountState);

        Account existingAccount = accountRepository.findByAccountNumber(accountNumber);
        Customer existingCustomer = existingAccount.getCustomer();

        if (existingAccount == null) {
            logger.warn("Account not found for update: {}", accountNumber);
            return "accountNotExist";
        }
        return accountService.updateAccount(existingAccount,existingCustomer,customer,accountState);

    }

    @GetMapping("/accountData")
    public String accountData(Model model){
        model.addAttribute("account", new Account());
        logger.info("Navigating to account data form.");
        return "accountDataForm";
    }

    @GetMapping("/account")
    public String readAccountInformation(@ModelAttribute("account") Account newAccount, Model model){
        logger.info("Fetching account information for Account Number: {}", newAccount.getAccountNumber());
        Account account = accountRepository.findByAccountNumber(newAccount.getAccountNumber());
        if (account == null){
            logger.warn("Account not found: {}", newAccount.getAccountNumber());
            return "accountNotExist";
        }
        Customer customer = account.getCustomer();
        ChangeHistory changeHistory = changeHistoryRepository.findByCustomer(customer);
        model.addAttribute("customer", customer);
        model.addAttribute("account", account);
        model.addAttribute("changeHistory", changeHistory);
        logger.info("Loaded account information for Account Number: {}", newAccount.getAccountNumber());
        return "accountInformation";

    }

    @GetMapping("/accountNumberByNationalId")
    public String accountNumber(Model model){
        model.addAttribute("customer", new Customer());
        logger.info("Navigating to account lookup by national ID.");
        return "accountNumberByNationalId";
    }

    @GetMapping("/accountNumber")
    public String getAccountNumber(@ModelAttribute("customer") Customer newCustomer, Model model){
        logger.info("Fetching account number for National ID: {}", newCustomer.getNationalId());

        Customer customer = customerRepository.findByNationalId(newCustomer.getNationalId());
        if (customer == null){
            logger.error("Invalid National ID: {}", newCustomer.getNationalId());
            return "invalidNationalId";
        }
        model.addAttribute("customer", customer);
        logger.info("Retrieved account number successfully for National ID: {}", newCustomer.getNationalId());
        return "accountNumber";
    }


    @GetMapping("/accountBalanceByAccountNumber")
    public String accountBalance(Model model){
        model.addAttribute("account", new Account());
        logger.info("Navigating to account balance lookup form.");
        return "accountBalanceByAccountNumber";
    }

    @GetMapping("/accountBalance")
    public String getAccountBalance(@ModelAttribute("account") Account newAccount, Model model){
        logger.info("Fetching account balance for Account Number: {}", newAccount.getAccountNumber());
        Account account = accountRepository.findByAccountNumber(newAccount.getAccountNumber());
        if (account == null){
            logger.error("Account not found for balance check: {}", newAccount.getAccountNumber());
            return "accountNotExist";
        }
        model.addAttribute("account", account);
        logger.info("Loaded account balance for Account Number: {}", newAccount.getAccountNumber());
    return "accountBalance";
    }

}
