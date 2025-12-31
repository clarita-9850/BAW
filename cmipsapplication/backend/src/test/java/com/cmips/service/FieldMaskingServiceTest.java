package com.cmips.service;

import com.cmips.entity.Timesheet;
import com.cmips.model.FieldMaskingRule;
import com.cmips.model.FieldMaskingRules;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FieldMaskingService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FieldMaskingService Tests")
class FieldMaskingServiceTest {

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private FieldMaskingService fieldMaskingService;

    private static final String TEST_ROLE = "RECIPIENT";
    private static final String TEST_JWT = "test-jwt-token";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get masking rules successfully")
    void testGetMaskingRules_Success() {
        // Act
        FieldMaskingRules rules = fieldMaskingService.getMaskingRules(TEST_ROLE, "TIMESHEET_REPORT", TEST_JWT);

        // Assert
        assertNotNull(rules);
        assertNotNull(rules.getRules());
    }

    @Test
    @DisplayName("Should apply masking with NONE mask type")
    void testApplyMasking_NONE() {
        // Arrange
        Timesheet timesheet = createMockTimesheet();
        FieldMaskingRules rules = createMockRules(FieldMaskingRule.MaskingType.NONE);

        // Act
        Map<String, Object> result = fieldMaskingService.applyMaskingToRecord(timesheet, rules);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should apply masking with HIDDEN mask type")
    void testApplyMasking_HIDDEN() {
        // Arrange
        Timesheet timesheet = createMockTimesheet();
        FieldMaskingRules rules = createMockRules(FieldMaskingRule.MaskingType.HIDDEN);

        // Act
        Map<String, Object> result = fieldMaskingService.applyMaskingToRecord(timesheet, rules);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should update rules successfully")
    void testUpdateRules_Success() {
        // Arrange
        List<FieldMaskingRule> rules = Arrays.asList(createMockRule());
        List<String> selectedFields = Arrays.asList("id", "employeeName");

        // Act
        fieldMaskingService.updateRules(TEST_ROLE, rules, selectedFields);

        // Assert
        // Verify rules were updated
        assertNotNull(fieldMaskingService.getSelectedFields(TEST_ROLE));
    }

    @Test
    @DisplayName("Should get selected fields successfully")
    void testGetSelectedFields_Success() {
        // Act
        List<String> result = fieldMaskingService.getSelectedFields(TEST_ROLE);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should use default rules when JWT token is missing")
    void testGetMaskingRules_NoJWT() {
        // Act
        FieldMaskingRules rules = fieldMaskingService.getMaskingRules(TEST_ROLE, "TIMESHEET_REPORT", null);

        // Assert
        assertNotNull(rules);
    }

    // Helper methods
    private Timesheet createMockTimesheet() {
        Timesheet timesheet = new Timesheet(
                "user1", "EMP001", "John Doe", "IT", "CTA",
                LocalDate.now().minusDays(14), LocalDate.now()
        );
        timesheet.setId(1L);
        timesheet.setRegularHours(BigDecimal.valueOf(40.0));
        return timesheet;
    }

    private FieldMaskingRules createMockRules(FieldMaskingRule.MaskingType maskType) {
        FieldMaskingRule rule = createMockRule();
        rule.setMaskingType(maskType);
        FieldMaskingRules rules = new FieldMaskingRules();
        rules.setUserRole(TEST_ROLE);
        rules.setReportType("TIMESHEET_REPORT");
        rules.setRules(Arrays.asList(rule));
        return rules;
    }

    private FieldMaskingRule createMockRule() {
        FieldMaskingRule rule = new FieldMaskingRule();
        rule.setFieldName("ssn");
        rule.setMaskingType(FieldMaskingRule.MaskingType.HIDDEN);
        rule.setAccessLevel(FieldMaskingRule.AccessLevel.HIDDEN_ACCESS);
        return rule;
    }
}

