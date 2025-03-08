package ir.mohaymen.bank.service;



import ir.mohaymen.bank.config.AdminConfig;
import ir.mohaymen.bank.model.Admin;
import ir.mohaymen.bank.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;


@Component
public class DataInitializer {

    @Autowired
    private AdminConfig adminConfig;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // تغییر نوع به PasswordEncoder

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (userRepository.findByUsername(adminConfig.getAdminUsername()).isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername(adminConfig.getAdminUsername());
            admin.setPassword(passwordEncoder.encode(adminConfig.getAdminPassword())); // هش کردن رمز عبور
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Admin user created successfully!");
        }
    }
}


