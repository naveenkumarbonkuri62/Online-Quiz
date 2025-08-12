package com.sq.controller;

import com.sq.entity.Exam;
import com.sq.entity.ExamAttempt;
import com.sq.entity.Question;
import com.sq.entity.Option;
import com.sq.service.ExamService;
import com.sq.service.UserService;
import com.sq.service.ExamAttemptService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ExamService examService;
    private final UserService userService;
    private final ExamAttemptService attemptService;

    public AdminController(ExamService examService,
                           UserService userService,
                           ExamAttemptService attemptService) {
        this.examService = examService;
        this.userService = userService;
        this.attemptService = attemptService;
    }

    /** Admin dashboard view */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
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
        if (principal == null) return "redirect:/login";
        exam.setCreatedBy(principal.getName());
        exam.setReleaseToAll(true);
        exam.setAllowedUsers(null);
        linkQuestionsAndOptions(exam);
        examService.saveExam(exam);
        redirectAttributes.addFlashAttribute("toastMessage", "✅ Exam created successfully!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/admin/dashboard";
    }

    /** ✅ Export all attempts for a single exam as Excel */
    @GetMapping("/export-exam-attempts/{examId}")
    public void exportExamAttempts(@PathVariable Long examId,
                                   Principal principal,
                                   HttpServletResponse response) throws IOException {

        if (principal == null) {
            response.sendRedirect("/login");
            return;
        }

        Exam exam = examService.getExamById(examId);
        if (exam == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Exam not found");
            return;
        }

        if (exam.getCreatedBy() == null ||
                !exam.getCreatedBy().equalsIgnoreCase(principal.getName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot export this exam.");
            return;
        }

        List<ExamAttempt> attempts = attemptService.getAttemptsByExam(examId);

        // HTTP Response for Excel
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = exam.getTitle().replaceAll("\\s+", "_") + "_attempts.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // Workbook setup
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Exam Attempts");

        // Header style
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setFont(headerFont);

        String[] columns = {"Username", "Score", "Total Questions", "Attempt Date", "Early Exit"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        for (ExamAttempt attempt : attempts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attempt.getUser().getUsername());
            row.createCell(1).setCellValue(attempt.getScore());
            row.createCell(2).setCellValue(attempt.getTotalQuestions());
            row.createCell(3).setCellValue(attempt.getAttemptDate().toString());
            row.createCell(4).setCellValue(attempt.isEarlyExit() ? "Yes" : "No");
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
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
