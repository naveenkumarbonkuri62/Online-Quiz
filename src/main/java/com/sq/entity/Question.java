package com.sq.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private int timeLimitSeconds; // Time allowed for THIS question

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    // Options fetched via SUBSELECT to avoid multiple bag fetch
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Option> options;

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public String getText() { return text; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public Exam getExam() { return exam; }
    public List<Option> getOptions() { return options; }

    public void setId(Long id) { this.id = id; }
    public void setText(String text) { this.text = text; }
    public void setTimeLimitSeconds(int timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public void setExam(Exam exam) { this.exam = exam; }

    public void setOptions(List<Option> options) {
        this.options = options;
        if (options != null) {
            options.forEach(o -> o.setQuestion(this));
        }
    }
}
