package com.sq.service;

import com.sq.entity.User;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Support multiple roles stored as comma-separated string
        String[] roles = Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .toArray(String[]::new);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword()) // stored BCrypt hash
                .roles(roles) // Spring adds "ROLE_" prefix automatically
                .build();
    }

    public void saveUser(User user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        user.setRole(user.getRole().trim().toUpperCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);
    }
}
