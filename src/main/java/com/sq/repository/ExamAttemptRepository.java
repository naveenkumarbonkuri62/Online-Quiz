package com.sq.repository;

import com.sq.entity.ExamAttempt;
import com.sq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {

    /** Eagerly load only Exam (questions & options via SUBSELECT) for a given user */
    @Query("""
        SELECT ea FROM ExamAttempt ea
        JOIN FETCH ea.exam e
        WHERE ea.user = :user
    """)
    List<ExamAttempt> findByUserWithExam(@Param("user") User user);

    /** Secure single attempt fetch by ID and username */
    Optional<ExamAttempt> findByIdAndUserUsername(Long attemptId, String username);

    /** Load attempt & exam only; questions & options come via SUBSELECT */
    @Query("""
        SELECT ea FROM ExamAttempt ea
        JOIN FETCH ea.exam e
        WHERE ea.id = :attemptId
    """)
    Optional<ExamAttempt> findByIdWithExamQuestionsAndOptions(@Param("attemptId") Long attemptId);

    /** ✅ NEW: Load all attempts for a given exam (with User data for export) */
    @Query("""
        SELECT ea FROM ExamAttempt ea
        JOIN FETCH ea.user u
        WHERE ea.exam.id = :examId
    """)
    List<ExamAttempt> findByExamIdWithUser(@Param("examId") Long examId);
}
