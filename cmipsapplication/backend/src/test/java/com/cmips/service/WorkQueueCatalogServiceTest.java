package com.cmips.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WorkQueueCatalogService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkQueueCatalogService Tests")
class WorkQueueCatalogServiceTest {

    @InjectMocks
    private WorkQueueCatalogService catalogService;

    @BeforeEach
    void setUp() {
        // No setup needed - service has no dependencies
    }

    @Test
    @DisplayName("Should get all queues successfully")
    void testGetAllQueues_Success() {
        // Act
        List<WorkQueueCatalogService.WorkQueueInfo> queues = catalogService.getAllQueues();

        // Assert
        assertNotNull(queues);
        assertEquals(5, queues.size());
        assertTrue(queues.stream().anyMatch(q -> q.getName().equals("PROVIDER_MANAGEMENT")));
        assertTrue(queues.stream().anyMatch(q -> q.getName().equals("ESCALATED")));
    }

    @Test
    @DisplayName("Should get queue info by name successfully")
    void testGetQueueInfo_Success() {
        // Act
        WorkQueueCatalogService.WorkQueueInfo info = catalogService.getQueueInfo("PROVIDER_MANAGEMENT");

        // Assert
        assertNotNull(info);
        assertEquals("PROVIDER_MANAGEMENT", info.getName());
        assertEquals("Provider Management", info.getDisplayName());
    }

    @Test
    @DisplayName("Should return null when queue not found")
    void testGetQueueInfo_NotFound() {
        // Act
        WorkQueueCatalogService.WorkQueueInfo info = catalogService.getQueueInfo("NON_EXISTENT");

        // Assert
        assertNull(info);
    }

    @Test
    @DisplayName("Should identify supervisor-only queue correctly")
    void testIsSupervisorOnly_Success() {
        // Act & Assert
        assertTrue(catalogService.isSupervisorOnly("ESCALATED"));
        assertFalse(catalogService.isSupervisorOnly("PROVIDER_MANAGEMENT"));
    }

    @Test
    @DisplayName("Should return false for non-existent queue")
    void testIsSupervisorOnly_NotFound() {
        // Act
        boolean result = catalogService.isSupervisorOnly("NON_EXISTENT");

        // Assert
        assertFalse(result);
    }
}







