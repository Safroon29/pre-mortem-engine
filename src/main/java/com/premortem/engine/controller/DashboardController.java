package com.premortem.engine.controller;

import com.premortem.engine.repository.RiskAssessmentRepository;
import com.premortem.engine.repository.UserRepository;
import com.premortem.engine.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final RiskAssessmentRepository assessments;
    private final UserRepository users;
    private final ProjectRepository projects;
    public DashboardController(RiskAssessmentRepository assessments, UserRepository users, ProjectRepository projects) {
        this.assessments = assessments; this.users = users; this.projects = projects;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        var user = users.findByEmailIgnoreCase(email).orElseThrow();
        model.addAttribute("displayName", user.getDisplayName());
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("projectCount", projects.countByOwnerEmail(email));
        model.addAttribute("projects", projects.findByOwnerEmailOrderByCreatedAtDesc(email).stream().limit(4).toList());
        model.addAttribute("total", assessments.countByOwnerEmail(email));
        model.addAttribute("high", assessments.countByOwnerEmailAndRiskLevel(email, "HIGH"));
        model.addAttribute("medium", assessments.countByOwnerEmailAndRiskLevel(email, "MEDIUM"));
        model.addAttribute("low", assessments.countByOwnerEmailAndRiskLevel(email, "LOW"));
        model.addAttribute("recent", assessments.findByOwnerEmailOrderByCreatedAtDesc(email).stream().limit(5).toList());
        return "dashboard";
    }
}
