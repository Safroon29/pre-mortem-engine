package com.premortem.engine.repository;

import com.premortem.engine.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerEmailOrderByCreatedAtDesc(String email);
    Optional<Project> findByIdAndOwnerEmail(Long id, String email);
    long countByOwnerEmail(String email);
}
