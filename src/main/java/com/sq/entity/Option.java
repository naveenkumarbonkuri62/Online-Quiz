package com.sq.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "options")
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    // ===== Getters =====
    public Long getId() { return id; }
    public String getText() { return text; }
    public boolean isCorrect() { return isCorrect; }
    public Question getQuestion() { return question; }

    // ===== Setters =====
    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setCorrect(boolean isCorrect) { this.isCorrect = isCorrect; }
    public void setQuestion(Question question) { this.question = question; }
}
