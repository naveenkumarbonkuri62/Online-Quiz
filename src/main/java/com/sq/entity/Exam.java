package com.sq.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic details
    private String title;
    private String description;
    private int durationMinutes;
    private String createdBy;
    private LocalDateTime createdDate;

    // Release settings
    private boolean releaseToAll = false;

    @ManyToMany
    @JoinTable(
            name = "exam_allowed_users",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> allowedUsers;

    // Questions for this exam — fetched via SUBSELECT to avoid multiple bag fetch
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Question> questions;

    @PrePersist
    public void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public List<Question> getQuestions() { return questions; }
    public boolean isReleaseToAll() { return releaseToAll; }
    public List<User> getAllowedUsers() { return allowedUsers; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        if (questions != null) {
            questions.forEach(q -> q.setExam(this)); // maintain bidirectional link
        }
    }

    public void setReleaseToAll(boolean releaseToAll) { this.releaseToAll = releaseToAll; }
    public void setAllowedUsers(List<User> allowedUsers) { this.allowedUsers = allowedUsers; }
}
