package com.cmips.service;

import com.cmips.entity.WorkQueueSubscription;
import com.cmips.repository.WorkQueueSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkQueueSubscriptionService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("WorkQueueSubscriptionService Tests")
class WorkQueueSubscriptionServiceTest {

    @Mock
    private WorkQueueSubscriptionRepository subscriptionRepository;

    @Mock
    private WorkQueueCatalogService catalogService;

    @InjectMocks
    private WorkQueueSubscriptionService subscriptionService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_QUEUE = "address-changes";
    private static final String TEST_SUPERVISOR = "supervisor1";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should subscribe user to queue successfully")
    void testSubscribeUserToQueue_Success() {
        // Arrange
        when(catalogService.isSupervisorOnly(TEST_QUEUE)).thenReturn(false);
        when(subscriptionRepository.findByUsernameAndWorkQueue(TEST_USERNAME, TEST_QUEUE))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(WorkQueueSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WorkQueueSubscription result = subscriptionService.subscribeUserToQueue(
                TEST_USERNAME, TEST_QUEUE, TEST_SUPERVISOR);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USERNAME, result.getUsername());
        assertEquals(TEST_QUEUE, result.getWorkQueue());
        verify(catalogService, times(1)).isSupervisorOnly(TEST_QUEUE);
        verify(subscriptionRepository, times(1)).save(any(WorkQueueSubscription.class));
    }

    @Test
    @DisplayName("Should return existing subscription when already subscribed")
    void testSubscribeUserToQueue_AlreadySubscribed() {
        // Arrange
        WorkQueueSubscription existing = createMockSubscription(1L);
        when(catalogService.isSupervisorOnly(TEST_QUEUE)).thenReturn(false);
        when(subscriptionRepository.findByUsernameAndWorkQueue(TEST_USERNAME, TEST_QUEUE))
                .thenReturn(Optional.of(existing));

        // Act
        WorkQueueSubscription result = subscriptionService.subscribeUserToQueue(
                TEST_USERNAME, TEST_QUEUE, TEST_SUPERVISOR);

        // Assert
        assertNotNull(result);
        assertEquals(existing, result);
        verify(subscriptionRepository, never()).save(any(WorkQueueSubscription.class));
    }

    @Test
    @DisplayName("Should throw exception for supervisor-only queue")
    void testSubscribeUserToQueue_SupervisorOnly() {
        // Arrange
        when(catalogService.isSupervisorOnly(TEST_QUEUE)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
                subscriptionService.subscribeUserToQueue(TEST_USERNAME, TEST_QUEUE, TEST_SUPERVISOR));
        verify(subscriptionRepository, never()).save(any(WorkQueueSubscription.class));
    }

    @Test
    @DisplayName("Should unsubscribe user from queue successfully")
    void testUnsubscribeUserFromQueue_Success() {
        // Arrange
        doNothing().when(subscriptionRepository).deleteByUsernameAndWorkQueue(TEST_USERNAME, TEST_QUEUE);

        // Act
        subscriptionService.unsubscribeUserFromQueue(TEST_USERNAME, TEST_QUEUE);

        // Assert
        verify(subscriptionRepository, times(1)).deleteByUsernameAndWorkQueue(TEST_USERNAME, TEST_QUEUE);
    }

    @Test
    @DisplayName("Should get user queues successfully")
    void testGetUserQueues_Success() {
        // Arrange
        List<WorkQueueSubscription> subscriptions = Arrays.asList(
                createMockSubscription(1L),
                createMockSubscription(2L)
        );
        when(subscriptionRepository.findByUsername(TEST_USERNAME)).thenReturn(subscriptions);

        // Act
        List<String> result = subscriptionService.getUserQueues(TEST_USERNAME);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subscriptionRepository, times(1)).findByUsername(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should get queue subscribers successfully")
    void testGetQueueSubscribers_Success() {
        // Arrange
        List<WorkQueueSubscription> subscriptions = Arrays.asList(
                createMockSubscription(1L),
                createMockSubscription(2L)
        );
        when(subscriptionRepository.findByWorkQueue(TEST_QUEUE)).thenReturn(subscriptions);

        // Act
        List<String> result = subscriptionService.getQueueSubscribers(TEST_QUEUE);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(subscriptionRepository, times(1)).findByWorkQueue(TEST_QUEUE);
    }

    @Test
    @DisplayName("Should check if user is subscribed")
    void testIsUserSubscribed_True() {
        // Arrange
        when(subscriptionRepository.findByUsernameAndWorkQueue(TEST_USERNAME, TEST_QUEUE))
                .thenReturn(Optional.of(createMockSubscription(1L)));

        // Act
        boolean result = subscriptionService.isUserSubscribed(TEST_USERNAME, TEST_QUEUE);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when user is not subscribed")
    void testIsUserSubscribed_False() {
        // Arrange
        when(subscriptionRepository.findByUsernameAndWorkQueue(TEST_USERNAME, TEST_QUEUE))
                .thenReturn(Optional.empty());

        // Act
        boolean result = subscriptionService.isUserSubscribed(TEST_USERNAME, TEST_QUEUE);

        // Assert
        assertFalse(result);
    }

    // Helper methods
    private WorkQueueSubscription createMockSubscription(Long id) {
        return WorkQueueSubscription.builder()
                .id(id)
                .username(TEST_USERNAME)
                .workQueue(TEST_QUEUE)
                .subscribedBy(TEST_SUPERVISOR)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}







