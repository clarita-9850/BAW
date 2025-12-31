package com.cmips.controller;

import com.cmips.entity.Task;
import com.cmips.entity.WorkQueueSubscription;
import com.cmips.service.KeycloakAdminService;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueCatalogService;
import com.cmips.service.WorkQueueSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkQueueController
 * 
 * Tests cover:
 * - Queue catalog retrieval
 * - Subscription management
 * - Queue summary operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkQueueController Tests")
class WorkQueueControllerTest {

    @Mock
    private WorkQueueCatalogService catalogService;

    @Mock
    private TaskService taskService;

    @Mock
    private WorkQueueSubscriptionService subscriptionService;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private WorkQueueController workQueueController;

    private static final String TEST_QUEUE = "address-changes";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get queue catalog successfully")
    void testGetQueueCatalog_Success() {
        // Arrange
        List<WorkQueueCatalogService.WorkQueueInfo> mockQueues = createMockQueueInfos();
        when(catalogService.getAllQueues()).thenReturn(mockQueues);

        // Act
        ResponseEntity<List<WorkQueueCatalogService.WorkQueueInfo>> response = workQueueController.getQueueCatalog();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(catalogService, times(1)).getAllQueues();
    }

    @Test
    @DisplayName("Should get queue tasks successfully")
    void testGetQueueTasks_Success() {
        // Arrange
        List<Task> mockTasks = createMockTasks(3);
        when(taskService.getQueueTasks(TEST_QUEUE)).thenReturn(mockTasks);

        // Act
        ResponseEntity<List<Task>> response = workQueueController.getQueueTasks(TEST_QUEUE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(taskService, times(1)).getQueueTasks(TEST_QUEUE);
    }

    @Test
    @DisplayName("Should get queues summary successfully")
    void testGetQueuesSummary_Success() {
        // Arrange
        List<WorkQueueCatalogService.WorkQueueInfo> mockQueues = createMockQueueInfos();
        List<Task> mockTasks1 = createMockTasks(2);
        List<Task> mockTasks2 = createMockTasks(1);

        when(catalogService.getAllQueues()).thenReturn(mockQueues);
        when(taskService.getQueueTasks(mockQueues.get(0).getName())).thenReturn(mockTasks1);
        when(taskService.getQueueTasks(mockQueues.get(1).getName())).thenReturn(mockTasks2);

        // Act
        ResponseEntity<Map<String, Object>> response = workQueueController.getQueuesSummary();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(catalogService, times(1)).getAllQueues();
    }

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsers_Success() {
        // Arrange
        List<Map<String, Object>> mockUsers = createMockUsers();
        when(keycloakAdminService.getAllUsers()).thenReturn(mockUsers);

        // Act
        ResponseEntity<List<Map<String, Object>>> response = workQueueController.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(keycloakAdminService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should subscribe user to queue successfully")
    void testSubscribeUserToQueue_Success() {
        // Arrange
        Map<String, String> request = Map.of(
                "username", TEST_USERNAME,
                "workQueue", TEST_QUEUE,
                "subscribedBy", "supervisor1"
        );
        WorkQueueSubscription mockSubscription = createMockSubscription();
        when(subscriptionService.subscribeUserToQueue(eq(TEST_USERNAME), eq(TEST_QUEUE), anyString()))
                .thenReturn(mockSubscription);

        // Act
        ResponseEntity<Map<String, Object>> response = workQueueController.subscribeUserToQueue(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(subscriptionService, times(1)).subscribeUserToQueue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return BadRequest when missing username")
    void testSubscribeUserToQueue_MissingUsername() {
        // Arrange
        Map<String, String> request = Map.of("workQueue", TEST_QUEUE);

        // Act
        ResponseEntity<Map<String, Object>> response = workQueueController.subscribeUserToQueue(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        verify(subscriptionService, never()).subscribeUserToQueue(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should unsubscribe user from queue successfully")
    void testUnsubscribeUserFromQueue_Success() {
        // Arrange
        Map<String, String> request = Map.of(
                "username", TEST_USERNAME,
                "workQueue", TEST_QUEUE
        );
        doNothing().when(subscriptionService).unsubscribeUserFromQueue(TEST_USERNAME, TEST_QUEUE);

        // Act
        ResponseEntity<Map<String, Object>> response = workQueueController.unsubscribeUserFromQueue(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        verify(subscriptionService, times(1)).unsubscribeUserFromQueue(TEST_USERNAME, TEST_QUEUE);
    }

    @Test
    @DisplayName("Should get queue subscribers successfully")
    void testGetQueueSubscribers_Success() {
        // Arrange
        List<WorkQueueSubscription> mockSubscriptions = createMockSubscriptions();
        when(subscriptionService.getQueueSubscriptions(TEST_QUEUE)).thenReturn(mockSubscriptions);

        // Act
        ResponseEntity<List<WorkQueueSubscription>> response = workQueueController.getQueueSubscribers(TEST_QUEUE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(subscriptionService, times(1)).getQueueSubscriptions(TEST_QUEUE);
    }

    @Test
    @DisplayName("Should get queue subscription details successfully")
    void testGetQueueSubscriptionDetails_Success() {
        // Arrange
        List<WorkQueueSubscription> mockSubscriptions = createMockSubscriptions();
        when(subscriptionService.getQueueSubscriptions(TEST_QUEUE)).thenReturn(mockSubscriptions);

        // Act
        ResponseEntity<List<WorkQueueSubscription>> response = workQueueController.getQueueSubscriptionDetails(TEST_QUEUE);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(subscriptionService, times(1)).getQueueSubscriptions(TEST_QUEUE);
    }

    // Helper methods
    private List<WorkQueueCatalogService.WorkQueueInfo> createMockQueueInfos() {
        return Arrays.asList(
                new WorkQueueCatalogService.WorkQueueInfo("address-changes", "Address Changes", "Tasks for address changes", false),
                new WorkQueueCatalogService.WorkQueueInfo("status-updates", "Status Updates", "Tasks for status updates", false)
        );
    }

    private List<Task> createMockTasks(int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Task task = new Task();
            task.setId((long) i);
            task.setTitle("Test Task " + i);
            task.setWorkQueue(TEST_QUEUE);
            tasks.add(task);
        }
        return tasks;
    }

    private List<Map<String, Object>> createMockUsers() {
        return Arrays.asList(
                Map.of("id", "user1", "username", "user1", "email", "user1@example.com"),
                Map.of("id", "user2", "username", "user2", "email", "user2@example.com")
        );
    }

    private WorkQueueSubscription createMockSubscription() {
        WorkQueueSubscription subscription = new WorkQueueSubscription();
        subscription.setId(1L);
        subscription.setUsername(TEST_USERNAME);
        subscription.setWorkQueue(TEST_QUEUE);
        return subscription;
    }

    private List<WorkQueueSubscription> createMockSubscriptions() {
        List<WorkQueueSubscription> subscriptions = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            WorkQueueSubscription sub = new WorkQueueSubscription();
            sub.setId((long) i);
            sub.setUsername("user" + i);
            sub.setWorkQueue(TEST_QUEUE);
            subscriptions.add(sub);
        }
        return subscriptions;
    }
}

