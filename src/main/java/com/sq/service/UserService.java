package com.sq.service;

import com.sq.entity.User;
import com.sq.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<User> findUsersByIds(List<Long> ids) {
        return (ids == null || ids.isEmpty()) ? List.of() : userRepo.findAllById(ids);
    }

    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        return Optional.ofNullable(userRepo.findByUsername(username));
    }

    /** Centralized user creation/updation with encoding & role normalization */
    @Transactional
    public User saveUser(User user) {
        if (userRepo.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        // Normalize role
        user.setRole(user.getRole().trim().toUpperCase());
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }
}
