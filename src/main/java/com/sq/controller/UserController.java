package com.sq.controller;

import com.sq.entity.Exam;
import com.sq.entity.ExamAttempt;
import com.sq.entity.User;
import com.sq.service.ExamAttemptService;
import com.sq.service.ExamService;
import com.sq.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    private final ExamService examService;
    private final UserService userService;
    private final ExamAttemptService attemptService;

    public UserController(ExamService examService,
                          UserService userService,
                          ExamAttemptService attemptService) {
        this.examService = examService;
        this.userService = userService;
        this.attemptService = attemptService;
    }

    /**
     * Shows the user dashboard with available exams and user's results.
     */
    @GetMapping("/dashboard")
    public String userDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found in database"));

        // Exams the user can take
        List<Exam> availableExams = examService.getAvailableExamsForUser(currentUser);

        // Exam attempts by the user
        List<ExamAttempt> attempts = attemptService.getAttemptsByUser(currentUser);

        // Optional filtering: Hide exams the user has already attempted
        Set<Long> attemptedExamIds = attempts.stream()
                .map(a -> a.getExam().getId())
                .collect(Collectors.toSet());
        availableExams = availableExams.stream()
                .filter(e -> !attemptedExamIds.contains(e.getId()))
                .toList();

        // Add model attributes
        model.addAttribute("exams", availableExams);
        model.addAttribute("attempts", attempts);
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("user", currentUser); // if more info needed in template

        return "user-dashboard";
    }
}
