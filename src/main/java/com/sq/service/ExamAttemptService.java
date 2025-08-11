package com.sq.service;

import com.sq.entity.*;
import com.sq.repository.ExamAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ExamAttemptService {

    private final ExamAttemptRepository attemptRepo;
    private final ExamService examService;

    public ExamAttemptService(ExamAttemptRepository attemptRepo, ExamService examService) {
        this.attemptRepo = attemptRepo;
        this.examService = examService;
    }

    /** Evaluate user's answers and save attempt (with earlyExit flag) */
    public ExamAttempt evaluateAndSaveAttempt(Long examId, User user, Map<Long, Long> answers, boolean earlyExit) {
        if (examId == null || user == null) return null;

        Exam exam = examService.getExamByIdWithQuestionsAndOptions(examId);
        if (exam == null) return null;

        Map<Long, Long> safeAnswers = (answers != null) ? answers : Map.of();
        int correctCount = 0;

        for (Question q : exam.getQuestions()) {
            Long selectedOptionId = safeAnswers.get(q.getId());
            if (selectedOptionId != null) {
                boolean isCorrect = q.getOptions()
                        .stream()
                        .anyMatch(op -> op.getId().equals(selectedOptionId) && op.isCorrect());
                if (isCorrect) correctCount++;
            }
        }

        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setExam(exam);
        attempt.setTotalQuestions(exam.getQuestions().size());
        attempt.setCorrectAnswers(correctCount);
        attempt.setScore(correctCount); // ✅ adjust if weight scoring
        attempt.setAttemptDate(LocalDateTime.now());
        attempt.setAnswers(safeAnswers); // ✅ Stored for result page
        attempt.setEarlyExit(earlyExit);

        return attemptRepo.save(attempt);
    }

    /** Overload defaults earlyExit to false */
    public ExamAttempt evaluateAndSaveAttempt(Long examId, User user, Map<Long, Long> answers) {
        return evaluateAndSaveAttempt(examId, user, answers, false);
    }

    /** All attempts for a given user (Exam preloaded, Qs via SUBSELECT) */
    public List<ExamAttempt> getAttemptsByUser(User user) {
        if (user == null) return List.of();
        return attemptRepo.findByUserWithExam(user);
    }

    /** Get full attempt with exam, questions, and options via SUBSELECT */
    public ExamAttempt getAttemptById(Long id) {
        return attemptRepo.findByIdWithExamQuestionsAndOptions(id).orElse(null);
    }

    /** Secure fetch */
    public ExamAttempt getAttemptForUser(Long attemptId, String username) {
        return attemptRepo.findByIdAndUserUsername(attemptId, username).orElse(null);
    }
}
