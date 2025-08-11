package com.sq.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users") // avoid reserved word 'user'
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String role; // "USER" or "ADMIN"

    // ✅ Inverse mapping to exams a user is allowed to take
    @ManyToMany(mappedBy = "allowedUsers")
    private List<Exam> assignedExams;

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Exam> getAssignedExams() { return assignedExams; }
    public void setAssignedExams(List<Exam> assignedExams) { this.assignedExams = assignedExams; }
}
