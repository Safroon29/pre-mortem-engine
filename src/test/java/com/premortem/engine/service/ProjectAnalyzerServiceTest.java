package com.premortem.engine.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class ProjectAnalyzerServiceTest {
    private final ProjectAnalyzerService analyzer = new ProjectAnalyzerService();

    @Test void scansReadmeAndReturnsBoundedScore() throws Exception {
        String readme = "# Demo\nRun with java. No tests are included yet.";
        var file = new MockMultipartFile("projectFile", "README.md", "text/markdown", readme.getBytes(StandardCharsets.UTF_8));
        var result = analyzer.analyze(file);
        assertTrue(result.riskScore() >= 1 && result.riskScore() <= 10);
        assertFalse(result.findings().isEmpty());
        assertNotNull(result.verdict());
    }

    @Test void rejectsUnsupportedFileType() {
        var file = new MockMultipartFile("projectFile", "program.exe", "application/octet-stream", new byte[]{1, 2, 3});
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze(file));
    }
}
