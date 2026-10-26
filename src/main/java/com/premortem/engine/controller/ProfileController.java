package com.premortem.engine.controller;

import com.premortem.engine.repository.RiskAssessmentRepository;
import com.premortem.engine.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {
    private final UserRepository users;
    private final RiskAssessmentRepository assessments;
    public ProfileController(UserRepository users, RiskAssessmentRepository assessments) {
        this.users = users; this.assessments = assessments;
    }
    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        var user = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
        model.addAttribute("profile", user);
        model.addAttribute("assessmentCount", assessments.countByOwnerEmail(auth.getName()));
        model.addAttribute("activePage", "profile");
        return "profile";
    }
}
