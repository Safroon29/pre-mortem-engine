package com.premortem.engine.controller;

import com.premortem.engine.model.Project;
import com.premortem.engine.repository.ProjectRepository;
import com.premortem.engine.repository.UserRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Controller
public class ProjectController {
    private final ProjectRepository projects;
    private final UserRepository users;
    private final Path uploadRoot = Paths.get("uploads", "projects").toAbsolutePath().normalize();

    public ProjectController(ProjectRepository projects, UserRepository users) {
        this.projects = projects;
        this.users = users;
    }

    @GetMapping("/projects")
    public String list(Authentication auth, Model model) {
        model.addAttribute("projects", projects.findByOwnerEmailOrderByCreatedAtDesc(auth.getName()));
        model.addAttribute("activePage", "projects");
        return "projects";
    }

    // Canonical route plus aliases so older sidebar links continue to work.
    @GetMapping({"/create-project", "/projects/create", "/projects/new", "/projects/create/"})
    public String newProject(Model model) {
        model.addAttribute("form", new ProjectForm());
        model.addAttribute("activePage", "create");
        return "project-form";
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String create(@ModelAttribute("form") ProjectForm form,
                         @RequestParam(value = "supportingDocument", required = false) MultipartFile supportingDocument,
                         Authentication auth, Model model) {
        if (form.getProjectName() == null || form.getProjectName().isBlank()
                || form.getDescription() == null || form.getDescription().isBlank()) {
            model.addAttribute("error", "Enter a project name and description to continue.");
            model.addAttribute("activePage", "create");
            return "project-form";
        }
        if (supportingDocument != null && !supportingDocument.isEmpty()) {
            String original = StringUtils.cleanPath(supportingDocument.getOriginalFilename() == null ? "document" : supportingDocument.getOriginalFilename());
            String lower = original.toLowerCase(Locale.ROOT);
            if (!(lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".pdf") || lower.endsWith(".txt") || lower.endsWith(".md") || lower.endsWith(".rtf"))) {
                model.addAttribute("error", "Supported documents: Word (.doc/.docx), PDF, TXT, Markdown or RTF.");
                model.addAttribute("activePage", "create");
                return "project-form";
            }
            if (supportingDocument.getSize() > 10L * 1024 * 1024) {
                model.addAttribute("error", "The supporting document must be 10 MB or smaller.");
                model.addAttribute("activePage", "create");
                return "project-form";
            }
        }
        Project project = new Project();
        project.setProjectName(form.getProjectName().trim());
        project.setProjectType(form.getProjectType() == null || form.getProjectType().isBlank() ? "Software" : form.getProjectType().trim());
        project.setDescription(form.getDescription().trim());
        project.setOwner(users.findByEmailIgnoreCase(auth.getName()).orElseThrow());
        project = projects.save(project);

        if (supportingDocument != null && !supportingDocument.isEmpty()) {
            try {
                Path projectDir = uploadRoot.resolve(String.valueOf(project.getId())).normalize();
                if (!projectDir.startsWith(uploadRoot)) throw new IOException("Invalid document location");
                Files.createDirectories(projectDir);
                String original = StringUtils.cleanPath(supportingDocument.getOriginalFilename() == null ? "document" : supportingDocument.getOriginalFilename());
                String safeName = Paths.get(original).getFileName().toString().replaceAll("[^A-Za-z0-9._-]", "_");
                Path destination = projectDir.resolve(safeName).normalize();
                if (!destination.startsWith(projectDir)) throw new IOException("Invalid document name");
                supportingDocument.transferTo(destination);
                project.setDocumentName(safeName);
                project.setDocumentPath(destination.toString());
                projects.save(project);
            } catch (IOException | RuntimeException ex) {
                model.addAttribute("error", "Project was created, but the document could not be saved. Open the project and try uploading it again.");
                model.addAttribute("activePage", "create");
                model.addAttribute("createdProjectId", project.getId());
                return "project-form";
            }
        }
        return "redirect:/projects?created";
    }

    @GetMapping("/projects/{id}/document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication auth) {
        Project project = projects.findByIdAndOwnerEmail(id, auth.getName()).orElseThrow();
        if (project.getDocumentPath() == null || project.getDocumentName() == null) {
            return ResponseEntity.notFound().build();
        }
        Path path = Paths.get(project.getDocumentPath()).toAbsolutePath().normalize();
        if (!path.startsWith(uploadRoot) || !Files.isRegularFile(path)) return ResponseEntity.notFound().build();
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + project.getDocumentName().replace("\"", "") + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        model.addAttribute("project", projects.findByIdAndOwnerEmail(id, auth.getName()).orElseThrow());
        model.addAttribute("activePage", "projects");
        return "project-detail";
    }

    public static class ProjectForm {
        private String projectName;
        private String projectType = "Software";
        private String description;
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getProjectType() { return projectType; }
        public void setProjectType(String projectType) { this.projectType = projectType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
