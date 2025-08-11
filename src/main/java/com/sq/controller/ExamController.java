package com.sq.controller;

import com.sq.entity.Exam;
import com.sq.entity.ExamAttempt;
import com.sq.entity.User;
import com.sq.service.ExamAttemptService;
import com.sq.service.ExamService;
import com.sq.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class ExamController {

    private final ExamService examService;
    private final ExamAttemptService attemptService;
    private final UserService userService;

    public ExamController(ExamService examService,
                          ExamAttemptService attemptService,
                          UserService userService) {
        this.examService = examService;
        this.attemptService = attemptService;
        this.userService = userService;
    }

    /** Show exam-taking page for logged-in user */
    @GetMapping("/take-exam/{id}")
    public String takeExam(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        Exam exam = examService.getExamByIdWithQuestionsAndOptions(id);
        if (exam == null) {
            return "redirect:/user/dashboard";
        }
        model.addAttribute("exam", exam);
        return "take-exam";
    }

    /** Submit user's answers (supports early exit flag) */
    @PostMapping("/submit-exam/{id}")
    public String submitExam(@PathVariable Long id,
                             @RequestParam Map<String, String> params,
                             @RequestParam(value = "earlyExit", required = false) String earlyExit,
                             Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        // Extract answers: questionId -> optionId
        Map<Long, Long> answers = new HashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("q_")) {
                try {
                    Long qId = Long.parseLong(key.substring(2));
                    Long optId = Long.parseLong(value);
                    answers.put(qId, optId);
                } catch (NumberFormatException ignored) {}
            }
        });

        boolean isEarlyExit = (earlyExit != null);
        attemptService.evaluateAndSaveAttempt(id, user, answers, isEarlyExit);

        if (isEarlyExit) {
            System.out.println("⚠ User exited the exam early!");
        }
        return "redirect:/user/dashboard";
    }

    /** List all attempts for current user */
    @GetMapping("/results")
    public String results(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        List<ExamAttempt> attempts = attemptService.getAttemptsByUser(user);
        model.addAttribute("attempts", attempts);
        return "exam-results-list";
    }

    /** Show details of a single attempt */
    @GetMapping("/result/{attemptId}")
    public String resultDetails(@PathVariable Long attemptId,
                                Model model,
                                Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        ExamAttempt attempt = attemptService.getAttemptById(attemptId);
        if (attempt == null || !attempt.getUser().getUsername().equals(user.getUsername())) {
            return "redirect:/user/results";
        }

        // Trigger SUBSELECT to load questions & options
        attempt.getExam().getQuestions().forEach(q -> q.getOptions().size());

        // Pass data for template
        model.addAttribute("attempt", attempt);
        model.addAttribute("answers", attempt.getAnswers()); // ✅ Needed for correct/wrong highlighting
        return "exam-result-details";
    }
}
