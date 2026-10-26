package com.premortem.engine.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class ProjectAnalyzerService {
    private static final long MAX_UNCOMPRESSED_BYTES = 12L * 1024 * 1024;
    private static final int MAX_ENTRIES = 1500;
    private static final Set<String> TEXT_EXTENSIONS = Set.of(
        ".java", ".kt", ".py", ".js", ".ts", ".jsx", ".tsx", ".html", ".css", ".xml", ".yml", ".yaml",
        ".properties", ".md", ".txt", ".json", ".sql", ".sh", ".bat", ".gradle", ".toml", ".ini", ".cfg", ".env.example"
    );

    public AnalysisResult analyze(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a project ZIP, README, or text file first.");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("upload");
        String lower = name.toLowerCase(Locale.ROOT);
        Map<String, String> documents = new LinkedHashMap<>();
        if (lower.endsWith(".zip")) {
            readZip(file.getBytes(), documents);
        } else if (isTextFile(lower)) {
            if (file.getSize() > MAX_UNCOMPRESSED_BYTES) throw new IllegalArgumentException("File is too large. Maximum upload size is 12 MB.");
            documents.put(name, new String(file.getBytes(), StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("Supported uploads: ZIP project archive, README.md, .txt, .xml, .json, .properties, .yml, or source-code text files.");
        }
        if (documents.isEmpty()) throw new IllegalArgumentException("No readable project files were found. Upload a ZIP containing source/configuration files or a README.");

        StringBuilder corpusBuilder = new StringBuilder();
        for (var e : documents.entrySet()) corpusBuilder.append("\n--- FILE: ").append(e.getKey()).append(" ---\n").append(e.getValue()).append('\n');
        String corpus = corpusBuilder.toString();
        String all = corpus.toLowerCase(Locale.ROOT);
        List<Finding> findings = new ArrayList<>();
        int riskPoints = 2; // baseline uncertainty: a static scan cannot prove production readiness

        if (!containsAny(documents.keySet(), "readme.md", "readme.txt", "readme")) {
            findings.add(f("No README or setup guide detected", "HIGH", 2, "A new developer may not know how to configure, run, or verify the project.", "Add a README with prerequisites, setup commands, environment variables, database setup, test commands, and expected output.", "Repository file scan")); riskPoints += 2;
        }
        if (!containsAny(documents.keySet(), "pom.xml", "build.gradle", "build.gradle.kts", "package.json", "requirements.txt", "pyproject.toml", "go.mod", "cargo.toml")) {
            findings.add(f("No common build/dependency manifest detected", "MEDIUM", 1, "Dependencies and reproducible build steps may be unclear, or the upload may not contain the project root.", "Include the root build manifest and lockfile where applicable. Confirm a clean build on a second machine.", "Repository file scan")); riskPoints += 1;
        }
        if (!containsPath(documents.keySet(), "/src/test/", "test/", "tests/", "__tests__/", "spec/")) {
            findings.add(f("No test files detected", "HIGH", 2, "Regressions may reach users because the uploaded files do not show automated tests.", "Add unit tests for core logic, integration tests for persistence/API flows, and a CI test run.", "Repository file scan")); riskPoints += 2;
        }
        if (hasLikelyHardCodedSecret(corpus)) {
            findings.add(f("Possible hard-coded secret or credential", "CRITICAL", 3, "Configuration text contains a credential-like pattern. This may be a placeholder, so verify before treating it as a confirmed secret.", "Move secrets to environment variables or a secret manager; rotate any real credential committed to the archive; provide only redacted examples.", "Credential-pattern scan (verify manually)")); riskPoints += 3;
        }
        if (containsAnyText(all, "ddl-auto=update", "ddl-auto: update", "create-drop", "spring.jpa.hibernate.ddl-auto=update")) {
            findings.add(f("Development-style database schema management", "MEDIUM", 1, "Automatic schema changes can be risky when deployed against important data.", "Use versioned migrations such as Flyway/Liquibase and validate schema changes in staging before production.", "Configuration scan")); riskPoints += 1;
        }
        if (containsAnyText(all, "debug=true", "debug: true", "show-sql=true", "show-sql: true", "server.error.include-message=always")) {
            findings.add(f("Potentially verbose runtime diagnostics", "MEDIUM", 1, "Debug output or detailed errors can disclose implementation details in a deployed environment.", "Disable debug/SQL logging and detailed error messages in production profiles.", "Configuration scan")); riskPoints += 1;
        }
        if (containsAnyText(all, "http://", "trust-all", "trustall", "disable_ssl", "verify=false", "hostnameverifier")) {
            findings.add(f("Review insecure transport or trust configuration", "MEDIUM", 1, "The source includes HTTP or TLS-verification-related text. Some matches may be harmless documentation or local development URLs.", "Review each match; require HTTPS/TLS verification for production connections and document any intentional local-only exception.", "Pattern scan (manual review required)")); riskPoints += 1;
        }
        if (!containsAnyText(all, "@valid", "@validated", "@notblank", "@notnull", "maxlength", "max-file-size", "max-request-size")) {
            findings.add(f("Input validation / upload limits are not obvious", "MEDIUM", 1, "The scanned text did not reveal common validation or request-size controls; checks may exist elsewhere.", "Validate inputs on the server, cap uploads and request bodies, and reject unexpected file types.", "Pattern scan; absence is not proof")); riskPoints += 1;
        }
        if (!containsAnyText(all, "license", "licence", "copyright")) {
            findings.add(f("License terms not detected", "LOW", 0, "Reuse and distribution permissions may be unclear if this project is shared publicly.", "Choose an appropriate license and add a LICENSE file if you intend to distribute the project.", "Repository file scan"));
        }
        // Evidence-based heuristic assessment: the uploaded project is the source of truth.
        // No user-entered probability, impact, failure scenario, or risk description is used.
        if (containsAnyText(all, "@test", "@springboottest", "junit", "pytest", "describe(", "it(", "test(", "unittest")) {
            // A positive signal is shown in the strengths section below; it does not erase other findings.
        } else {
            findings.add(f("Automated test coverage is not evident", "HIGH", 2,
                "The uploaded files do not show common test frameworks or test declarations. Core behavior may regress unnoticed.",
                "Add tests for core business rules, authentication, validation, and failure paths; run them in continuous integration.",
                "Source-pattern scan; confirm in the repository"));
            riskPoints += 2;
        }
        if (!containsAnyText(all, "@controlleradvice", "@exceptionhandler", "try {", "catch (", "except ", "errorboundary", "error handler")) {
            findings.add(f("Centralized error handling is not evident", "MEDIUM", 1,
                "The scan did not find common error-handling patterns. Failures may produce inconsistent responses or be difficult to diagnose.",
                "Add consistent exception handling, safe user-facing errors, and structured logs that avoid sensitive data.",
                "Source-pattern scan; absence is not proof"));
            riskPoints += 1;
        }
        if (containsAnyText(all, "spring-boot-starter-security", "springsecurity", "@preauthorize", "csrf", "oauth2", "jwt")) {
            // security-related implementation markers are a positive signal, not a guarantee of secure configuration
        } else if (containsAnyText(all, "login", "password", "authentication", "session")) {
            findings.add(f("Authentication-related code needs a security review", "HIGH", 2,
                "The project appears to handle identity or passwords, but common security configuration markers were not detected.",
                "Review password hashing, authorization on every protected route, session security, CSRF protection, and account recovery.",
                "Authentication keyword scan; manual verification required"));
            riskPoints += 2;
        }
        int score = Math.max(1, Math.min(10, riskPoints));
        String verdict = score > 8 ? "PAUSE — NOT WORTH PROCEEDING IN ITS CURRENT STATE" : score >= 6 ? "CONDITIONAL — PROCEED ONLY WITH A MITIGATION PLAN" : "WORTH PROCEEDING — KEEP PREVENTIVE CONTROLS IN PLACE";
        String summary = score > 8
            ? "The scan found enough high-priority indicators to pause a launch or major commitment until critical findings are verified and mitigated. This is a screening result, not a final business decision."
            : score >= 6
            ? "The project may be viable, but address the highest-priority findings before release and reassess after fixes."
            : "The scan did not detect enough indicators to cross the high-risk threshold. This does not prove the project is safe or guaranteed to succeed.";
        List<String> strengths = new ArrayList<>();
        if (containsAny(documents.keySet(), "readme.md", "readme.txt", "readme")) strengths.add("Project documentation is present");
        if (containsAny(documents.keySet(), "pom.xml", "build.gradle", "build.gradle.kts", "package.json", "requirements.txt", "pyproject.toml", "go.mod", "cargo.toml")) strengths.add("A recognizable build or dependency manifest is present");
        if (containsPath(documents.keySet(), "/src/test/", "test/", "tests/", "__tests__/", "spec/")) strengths.add("Test-related files are included");
        if (containsAnyText(all, "spring-boot-starter-security", "springsecurity", "@preauthorize", "csrf", "oauth2")) strengths.add("Security-related implementation markers are present (configuration still needs review)");
        if (containsAnyText(all, "@valid", "@validated", "@notblank", "@notnull", "maxlength")) strengths.add("Input-validation markers are present");
        if (containsAnyText(all, "@controlleradvice", "@exceptionhandler", "try {", "catch (", "except ", "errorboundary")) strengths.add("Error-handling patterns are present");
        if (strengths.isEmpty()) strengths.add("No strong positive signals could be confirmed from the uploaded evidence yet");
        int healthScore = Math.max(0, Math.min(100, 100 - score * 8 - (findings.stream().anyMatch(x -> x.severity().equals("CRITICAL")) ? 10 : 0) + Math.min(12, strengths.size() * 3)));
        String maturity = healthScore >= 80 ? "Strong baseline" : healthScore >= 60 ? "Developing" : healthScore >= 40 ? "Needs focused improvement" : "High attention needed";
        return new AnalysisResult(name, documents.size(), score, verdict, summary, findings, strengths, healthScore, maturity,
            List.of("Fix CRITICAL and HIGH findings first, then rescan.", "Run the full build and automated tests from a clean checkout.", "Review secrets, access control, dependency vulnerabilities, backups, and deployment configuration.", "Verify each finding against its file and context; pattern matches can be false positives.", "Retest after fixes and compare the new findings before release."));
    }

    private void readZip(byte[] bytes, Map<String,String> documents) throws IOException {
        long total = 0; int entries = 0;
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bytes))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (++entries > MAX_ENTRIES) throw new IllegalArgumentException("ZIP contains too many entries (limit 1,500).");
                String path = entry.getName().replace('\\', '/');
                if (entry.isDirectory() || path.startsWith("/") || path.contains("../") || path.equals("..")) continue;
                String lower = path.toLowerCase(Locale.ROOT);
                if (lower.contains("/.git/") || lower.contains("/target/") || lower.contains("/node_modules/") || lower.contains("/.idea/") || lower.contains("/build/")) continue;
                if (!isTextFile(lower) && !isInterestingFilename(lower)) continue;
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int read;
                while ((read = zin.read(chunk)) != -1) {
                    total += read;
                    if (total > MAX_UNCOMPRESSED_BYTES) throw new IllegalArgumentException("Uncompressed project text exceeds 12 MB. Remove build outputs and try again.");
                    buffer.write(chunk, 0, read);
                }
                byte[] data = buffer.toByteArray();
                String text = new String(data, StandardCharsets.UTF_8);
                if (!looksBinary(text)) documents.put(path, text.length() > 120_000 ? text.substring(0, 120_000) : text);
                zin.closeEntry();
            }
        }
    }
    private boolean isTextFile(String n) { return TEXT_EXTENSIONS.stream().anyMatch(n::endsWith) || isInterestingFilename(n); }
    private boolean isInterestingFilename(String n) { return n.endsWith("readme") || n.endsWith("dockerfile") || n.endsWith("makefile") || n.endsWith("license") || n.endsWith(".lock"); }
    private boolean looksBinary(String s) { return s.indexOf('\0') >= 0; }

    private boolean hasLikelyHardCodedSecret(String corpus) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "(?im)^\\s*(?:[\\w.-]*password|[\\w.-]*secret(?:_key)?|api[_-]?key|access[_-]?token)\\s*[:=]\\s*([^\\s#]+)"
        );
        java.util.regex.Matcher matcher = pattern.matcher(corpus);
        while (matcher.find()) {
            String value = matcher.group(1).replaceAll("^[\"']|[\"',;]$", "").toLowerCase(Locale.ROOT);
            if (value.isBlank() || value.startsWith("${") || value.contains("change_me") || value.contains("changeme")
                || value.contains("your_") || value.contains("example") || value.contains("placeholder")
                || value.contains("replace_me") || value.equals("null")) continue;
            if (value.length() >= 8) return true;
        }
        return false;
    }

    private boolean containsAny(Collection<String> values, String... needles) { return values.stream().map(v -> v.toLowerCase(Locale.ROOT)).anyMatch(v -> Arrays.stream(needles).anyMatch(v::endsWith)); }
    private boolean containsPath(Collection<String> values, String... needles) { return values.stream().map(v -> "/" + v.toLowerCase(Locale.ROOT)).anyMatch(v -> Arrays.stream(needles).anyMatch(v::contains)); }
    private boolean containsAnyText(String text, String... needles) { return Arrays.stream(needles).anyMatch(text::contains); }
    private String clip(String s, int max) { return s.length() <= max ? s : s.substring(0, max) + "…"; }
    private Finding f(String title, String severity, int points, String impact, String prevention, String evidence) { return new Finding(title, severity, points, impact, prevention, evidence); }

    public record Finding(String title, String severity, int points, String impact, String prevention, String evidence) {}
    public record AnalysisResult(String uploadedFile, int filesScanned, int riskScore, String verdict, String summary,
                                 List<Finding> findings, List<String> strengths, int healthScore, String maturity,
                                 List<String> preventiveChecklist) {}
}
