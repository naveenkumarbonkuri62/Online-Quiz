package com.sq.repository;

import com.sq.entity.Exam;
import com.sq.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    // Fetch exam only; questions & options via SUBSELECT
    @Query("SELECT e FROM Exam e WHERE e.id = :id")
    Optional<Exam> findByIdWithQuestionsAndOptions(@Param("id") Long id);

    @Query("SELECT e FROM Exam e WHERE e.createdBy = :username")
    List<Exam> findByCreatedBy(@Param("username") String username);

    @Query("""
        SELECT DISTINCT e FROM Exam e
        LEFT JOIN e.allowedUsers au
        WHERE e.releaseToAll = true OR au = :user
    """)
    List<Exam> findAvailableExamsForUser(@Param("user") User user);
}
