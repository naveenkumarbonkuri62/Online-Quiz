package com.sq.service;

import com.sq.entity.Exam;
import com.sq.entity.User;
import com.sq.repository.ExamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepo;

    public ExamService(ExamRepository examRepo) {
        this.examRepo = examRepo;
    }

    /**
     * Loads an Exam by ID (basic fetch, no guaranteed collection initialization).
     */
    public Exam getExamById(Long id) {
        return examRepo.findById(id).orElse(null);
    }

    /**
     * Loads an Exam by ID with questions & options populated via SUBSELECT fetch mode.
     * Avoids MultipleBagFetchException and preserves ordering for List collections.
     */
    public Exam getExamByIdWithQuestionsAndOptions(Long id) {
        return examRepo.findByIdWithQuestionsAndOptions(id).orElse(null);
    }

    /**
     * Gets all exams created by a specific admin username.
     */
    public List<Exam> getExamsByAdmin(String adminUsername) {
        return examRepo.findByCreatedBy(adminUsername);
    }

    /**
     * Gets all exams the given user can take — either released to all or specifically assigned.
     */
    public List<Exam> getAvailableExamsForUser(User user) {
        if (user == null) return List.of();
        return examRepo.findAvailableExamsForUser(user);
    }

    /**
     * Saves or updates an exam.
     */
    public void saveExam(Exam exam) {
        examRepo.save(exam);
    }

    /**
     * Deletes an exam by ID.
     */
    public void deleteExamById(Long id) {
        examRepo.deleteById(id);
    }
}
