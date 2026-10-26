package com.premortem.engine.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTest {
    private final RiskScoringService scoring = new RiskScoringService();

    @Test void multipliesProbabilityAndImpact() {
        assertEquals(20, scoring.score(4, 5));
    }
    @Test void appliesConfiguredLevels() {
        assertEquals("LOW", scoring.level(5));
        assertEquals("MEDIUM", scoring.level(6));
        assertEquals("HIGH", scoring.level(15));
    }
    @Test void rejectsInvalidScale() {
        assertThrows(IllegalArgumentException.class, () -> scoring.score(0, 3));
    }
}
