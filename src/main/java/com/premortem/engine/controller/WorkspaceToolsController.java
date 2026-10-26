package com.premortem.engine.controller;

import com.premortem.engine.repository.RiskAssessmentRepository;
import com.premortem.engine.service.ProjectAnalyzerService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WorkspaceToolsController {
    private final RiskAssessmentRepository assessments;
    private final ProjectAnalyzerService analyzer;
    public WorkspaceToolsController(RiskAssessmentRepository assessments, ProjectAnalyzerService analyzer) { this.assessments = assessments; this.analyzer = analyzer; }

    @GetMapping("/simulations")
    public String simulations(Authentication auth, Model model) {
        model.addAttribute("activePage", "simulations");
        model.addAttribute("assessments", assessments.findByOwnerEmailOrderByCreatedAtDesc(auth.getName()));
        return "simulations";
    }

    @PostMapping("/simulations/analyze")
    public String analyzeForSimulation(@RequestParam("projectFile") MultipartFile projectFile, Authentication auth, Model model) {
        model.addAttribute("activePage", "simulations");
        model.addAttribute("assessments", assessments.findByOwnerEmailOrderByCreatedAtDesc(auth.getName()));
        try { model.addAttribute("scanResult", analyzer.analyze(projectFile)); }
        catch (Exception ex) { model.addAttribute("uploadError", ex instanceof IllegalArgumentException ? ex.getMessage() : "Could not analyze this file. Upload a supported project ZIP or text file."); }
        return "simulations";
    }
}
