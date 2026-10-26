package com.premortem.engine.controller;

import com.premortem.engine.repository.RiskAssessmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AssessmentController {
    private final RiskAssessmentRepository assessments;

    public AssessmentController(RiskAssessmentRepository assessments) {
        this.assessments = assessments;
    }

    // Assessment pages are generated from findings created by Scan Project.
    @GetMapping("/assessments")
    public String reports(Authentication auth, Model model) {
        model.addAttribute("assessments", assessments.findByOwnerEmailOrderByCreatedAtDesc(auth.getName()));
        model.addAttribute("activePage", "assessment");
        return "assessments";
    }

    @GetMapping("/assessments/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("activePage", "assessment");
        model.addAttribute("assessment", assessments.findByIdAndOwnerEmail(id, auth.getName()).orElseThrow());
        return "assessment-detail";
    }

    @PostMapping("/assessments/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth) {
        var item = assessments.findByIdAndOwnerEmail(id, auth.getName()).orElseThrow();
        assessments.delete(item);
        return "redirect:/assessments?deleted";
    }
}
