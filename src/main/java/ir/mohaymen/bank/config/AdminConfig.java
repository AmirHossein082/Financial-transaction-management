package ir.mohaymen.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AdminConfig {

        @Value("${adminUsername}")
        private String adminUsername;

        @Value("${adminPassword}")
        private String adminPassword;

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }
}
