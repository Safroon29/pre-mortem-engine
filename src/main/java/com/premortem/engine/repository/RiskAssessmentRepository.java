package com.premortem.engine.repository;

import com.premortem.engine.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    List<RiskAssessment> findByOwnerEmailOrderByCreatedAtDesc(String email);
    Optional<RiskAssessment> findByIdAndOwnerEmail(Long id, String email);
    long countByOwnerEmail(String email);
    long countByOwnerEmailAndRiskLevel(String email, String riskLevel);
}
