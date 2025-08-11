package com.sq.controller;

import com.sq.entity.Exam;
import com.sq.entity.Question;
import com.sq.entity.Option;
import com.sq.service.ExamService;
import com.sq.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ExamService examService;
    private final UserService userService;

    public AdminController(ExamService examService, UserService userService) {
        this.examService = examService;
        this.userService = userService;
    }

    /** Admin dashboard view */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        model.addAttribute("exam", new Exam());
        model.addAttribute("exams", examService.getExamsByAdmin(principal.getName()));
        model.addAttribute("allUsers", userService.getAllUsers());
        return "admin-dashboard";
    }

    /** Create new exam */
    @PostMapping("/create-exam")
    public String createExam(@ModelAttribute Exam exam,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        exam.setCreatedBy(principal.getName());
        exam.setReleaseToAll(true);
        exam.setAllowedUsers(null);
        linkQuestionsAndOptions(exam);

        examService.saveExam(exam);
        redirectAttributes.addFlashAttribute("toastMessage", "✅ Exam created successfully!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/admin/dashboard";
    }

    /** Show edit form */
    @GetMapping("/edit-exam/{id}")
    public String editExam(@PathVariable Long id, Model model, Principal principal) {
        Exam exam = examService.getExamByIdWithQuestionsAndOptions(id);
        if (exam == null) {
            throw new RuntimeException("Exam not found or invalid ID: " + id);
        }
        if (!exam.getCreatedBy().equals(principal.getName())) {
            throw new RuntimeException("Unauthorized access to exam " + id);
        }
        if (exam.getQuestions() == null) {
            exam.setQuestions(new ArrayList<>());
        }
        model.addAttribute("exam", exam);
        model.addAttribute("allUsers", userService.getAllUsers());
        return "edit-exam";
    }

    /** Update existing exam */
    @PostMapping("/edit-exam/{id}")
    public String updateExam(@PathVariable Long id,
                             @ModelAttribute Exam updatedExam,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        Exam existing = examService.getExamByIdWithQuestionsAndOptions(id);
        if (existing == null || !existing.getCreatedBy().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("toastMessage", "⚠ You cannot edit this exam.");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/admin/dashboard";
        }

        existing.setTitle(updatedExam.getTitle());
        existing.setDescription(updatedExam.getDescription());
        existing.setDurationMinutes(updatedExam.getDurationMinutes());

        // Remove deleted questions
        if (existing.getQuestions() == null) existing.setQuestions(new ArrayList<>());
        existing.getQuestions().removeIf(q ->
                updatedExam.getQuestions() == null ||
                        updatedExam.getQuestions().stream().noneMatch(uq -> uq.getId() != null && uq.getId().equals(q.getId()))
        );

        // Add/update questions
        if (updatedExam.getQuestions() != null) {
            for (Question updatedQ : updatedExam.getQuestions()) {
                Question existingQ = existing.getQuestions().stream()
                        .filter(q -> q.getId() != null && q.getId().equals(updatedQ.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingQ == null) {
                    updatedQ.setExam(existing);
                    linkOptions(updatedQ);
                    existing.getQuestions().add(updatedQ);
                } else {
                    existingQ.setText(updatedQ.getText());
                    existingQ.setTimeLimitSeconds(updatedQ.getTimeLimitSeconds());
                    if (existingQ.getOptions() == null) existingQ.setOptions(new ArrayList<>());
                    existingQ.getOptions().removeIf(op ->
                            updatedQ.getOptions() == null ||
                                    updatedQ.getOptions().stream().noneMatch(uo -> uo.getId() != null && uo.getId().equals(op.getId()))
                    );
                    if (updatedQ.getOptions() != null) {
                        for (Option updatedOp : updatedQ.getOptions()) {
                            Option existingOp = existingQ.getOptions().stream()
                                    .filter(op -> op.getId() != null && op.getId().equals(updatedOp.getId()))
                                    .findFirst()
                                    .orElse(null);
                            if (existingOp == null) {
                                updatedOp.setQuestion(existingQ);
                                existingQ.getOptions().add(updatedOp);
                            } else {
                                existingOp.setText(updatedOp.getText());
                                existingOp.setCorrect(updatedOp.isCorrect());
                            }
                        }
                        ensureOneCorrectOption(existingQ);
                    }
                }
            }
        }

        examService.saveExam(existing);
        redirectAttributes.addFlashAttribute("toastMessage", "✅ Exam updated successfully!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/admin/dashboard";
    }

    /** Delete exam */
    @PostMapping("/delete-exam/{id}")
    public String deleteExam(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Exam exam = examService.getExamById(id);
        if (exam != null && exam.getCreatedBy().equals(principal.getName())) {
            examService.deleteExamById(id);
            redirectAttributes.addFlashAttribute("toastMessage", "🗑 Exam deleted successfully!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } else {
            redirectAttributes.addFlashAttribute("toastMessage", "⚠ You cannot delete this exam.");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        return "redirect:/admin/dashboard";
    }

    /** Update release settings */
    @PostMapping("/update-release/{id}")
    public String updateRelease(@PathVariable Long id,
                                @RequestParam boolean releaseToAll,
                                @RequestParam(required = false) List<Long> selectedUserIds,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Exam exam = examService.getExamById(id);
        if (exam != null && exam.getCreatedBy().equals(principal.getName())) {
            exam.setReleaseToAll(releaseToAll);
            if (!releaseToAll && selectedUserIds != null && !selectedUserIds.isEmpty()) {
                exam.setAllowedUsers(userService.findUsersByIds(selectedUserIds));
            } else {
                exam.setAllowedUsers(null);
            }
            examService.saveExam(exam);
            redirectAttributes.addFlashAttribute("toastMessage", "✅ Release settings updated!");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } else {
            redirectAttributes.addFlashAttribute("toastMessage", "⚠ Cannot update release settings.");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        return "redirect:/admin/dashboard";
    }

    /** Helpers */
    private void linkQuestionsAndOptions(Exam exam) {
        if (exam.getQuestions() != null) {
            for (Question q : exam.getQuestions()) {
                q.setExam(exam);
                linkOptions(q);
            }
        }
    }

    private void linkOptions(Question question) {
        if (question.getOptions() != null) {
            question.getOptions().forEach(op -> op.setQuestion(question));
            ensureOneCorrectOption(question);
        }
    }

    private void ensureOneCorrectOption(Question question) {
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            boolean anyCorrect = question.getOptions().stream().anyMatch(Option::isCorrect);
            if (!anyCorrect) {
                question.getOptions().get(0).setCorrect(true);
            }
        }
    }
}
