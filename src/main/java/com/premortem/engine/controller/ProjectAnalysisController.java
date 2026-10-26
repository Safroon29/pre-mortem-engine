package com.premortem.engine.controller;

import com.premortem.engine.service.ProjectAnalyzerService;
import com.premortem.engine.model.RiskAssessment;
import com.premortem.engine.repository.RiskAssessmentRepository;
import com.premortem.engine.repository.UserRepository;
import com.premortem.engine.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProjectAnalysisController {
    private final ProjectAnalyzerService analyzer;
    private final RiskAssessmentRepository assessments;
    private final UserRepository users;
    private final ProjectRepository projects;
    public ProjectAnalysisController(ProjectAnalyzerService analyzer, RiskAssessmentRepository assessments, UserRepository users, ProjectRepository projects) {
        this.analyzer = analyzer; this.assessments = assessments; this.users = users; this.projects = projects;
    }

    @GetMapping("/project-analysis")
    public String page(Model model, Authentication auth) {
        model.addAttribute("activePage", "scan");
        model.addAttribute("projects", projects.findByOwnerEmailOrderByCreatedAtDesc(auth.getName()));
        return "project-analysis";
    }

    @PostMapping("/project-analysis")
    public String analyze(@RequestParam("projectFile") MultipartFile projectFile,
                          @RequestParam(value = "projectId", required = false) Long projectId,
                          Model model, Authentication auth) {
        model.addAttribute("activePage", "scan");
        model.addAttribute("projects", projects.findByOwnerEmailOrderByCreatedAtDesc(auth.getName()));
        try {
            var linkedProject = projectId == null ? null : projects.findByIdAndOwnerEmail(projectId, auth.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Select a project from your own project list, or choose No linked project."));
            var result = analyzer.analyze(projectFile);
            model.addAttribute("result", result);
            // Persist each automatically detected finding into assessment history.
            var owner = users.findByEmailIgnoreCase(auth.getName()).orElseThrow();
            for (var finding : result.findings()) {
                RiskAssessment item = new RiskAssessment();
                item.setProjectName(linkedProject == null ? result.uploadedFile().replaceAll("\\.[^.]+$", "") : linkedProject.getProjectName());
                item.setProjectType(linkedProject == null ? "Software" : linkedProject.getProjectType());
                item.setProjectDescription((linkedProject == null ? "Standalone uploaded project" : linkedProject.getDescription()) + "\nAutomatically scanned " + result.filesScanned() + " readable project files. Screening health score: " + result.healthScore() + "/100.");
                item.setProject(linkedProject);
                item.setFailureScenario(finding.title());
                item.setRootCause(finding.impact());
                int probability = finding.severity().equals("CRITICAL") ? 5 : finding.severity().equals("HIGH") ? 4 : finding.severity().equals("MEDIUM") ? 3 : 2;
                int impact = finding.severity().equals("CRITICAL") ? 5 : finding.severity().equals("HIGH") ? 4 : finding.severity().equals("MEDIUM") ? 3 : 2;
                item.setProbability(probability); item.setImpact(impact);
                item.setRiskScore(probability * impact);
                item.setRiskLevel(probability * impact >= 15 ? "HIGH" : probability * impact >= 6 ? "MEDIUM" : "LOW");
                item.setWarningSigns("Detected by automated source/configuration scan. Evidence: " + finding.evidence());
                item.setMitigationPlan(finding.prevention()); item.setOwner(owner);
                assessments.save(item);
            }
            return "project-analysis";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "project-analysis";
        } catch (Exception ex) {
            model.addAttribute("error", "Could not inspect this file. Check that it is a valid ZIP or readable text file, then try again.");
            return "project-analysis";
        }
    }
}
