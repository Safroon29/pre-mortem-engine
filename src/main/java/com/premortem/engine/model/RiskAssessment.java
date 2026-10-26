package com.premortem.engine.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String projectName;
    @Column(nullable = false, length = 80)
    private String projectType;
    @Column(nullable = false, length = 3000)
    private String projectDescription;
    @Column(nullable = false, length = 180)
    private String failureScenario;
    @Column(nullable = false, length = 2000)
    private String rootCause;
    @Column(nullable = false)
    private int probability;
    @Column(nullable = false)
    private int impact;
    @Column(nullable = false)
    private int riskScore;
    @Column(nullable = false, length = 20)
    private String riskLevel;
    @Column(nullable = false, length = 2000)
    private String mitigationPlan;
    @Column(length = 1000)
    private String warningSigns;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    public Long getId() { return id; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String v) { projectName = v; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String v) { projectType = v; }
    public String getProjectDescription() { return projectDescription; }
    public void setProjectDescription(String v) { projectDescription = v; }
    public String getFailureScenario() { return failureScenario; }
    public void setFailureScenario(String v) { failureScenario = v; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String v) { rootCause = v; }
    public int getProbability() { return probability; }
    public void setProbability(int v) { probability = v; }
    public int getImpact() { return impact; }
    public void setImpact(int v) { impact = v; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int v) { riskScore = v; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String v) { riskLevel = v; }
    public String getMitigationPlan() { return mitigationPlan; }
    public void setMitigationPlan(String v) { mitigationPlan = v; }
    public String getWarningSigns() { return warningSigns; }
    public void setWarningSigns(String v) { warningSigns = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
}
