package com.premortem.engine.service;

import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {
    public int score(int probability, int impact) {
        if (probability < 1 || probability > 5 || impact < 1 || impact > 5)
            throw new IllegalArgumentException("Probability and impact must be between 1 and 5.");
        return probability * impact;
    }

    public String level(int score) {
        if (score >= 15) return "HIGH";
        if (score >= 6) return "MEDIUM";
        return "LOW";
    }

    public String suggestedMitigation(String level) {
        return switch (level) {
            case "HIGH" -> "Assign an owner immediately, create a contingency plan, test the mitigation, and review this risk weekly.";
            case "MEDIUM" -> "Assign a responsible owner, define an early-warning indicator, and review progress at each project milestone.";
            default -> "Document the risk, monitor it at milestone reviews, and revisit the score if project conditions change.";
        };
    }
}
