package com.sq.config;

import com.sq.entity.User;
import com.sq.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.default-admin.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.default-admin.password:admin123}")
    private String defaultAdminPassword;

    @Bean
    public CommandLineRunner initDefaultAdmin(UserService userService) {
        return args -> {
            // Check if admin exists
            if (userService.findByUsername(defaultAdminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(defaultAdminUsername);
                admin.setPassword(defaultAdminPassword); // raw password, will be encoded in UserService
                admin.setRole("ADMIN");

                userService.saveUser(admin); // handles encoding & role normalization
                log.info("✅ Default admin account created: {} / {}", defaultAdminUsername, defaultAdminPassword);
            } else {
                log.info("ℹ Admin account already exists. Skipping creation.");
            }
        };
    }
}
