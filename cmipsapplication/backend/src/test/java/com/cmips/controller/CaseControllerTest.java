package com.cmips.controller;

import com.cmips.entity.Notification;
import com.cmips.event.BaseEvent;
import com.cmips.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CaseController
 * 
 * Tests cover:
 * - Address change submission
 * - Kafka event publishing
 * - Notification creation
 * - Task creation for outside CA
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CaseController Tests")
class CaseControllerTest {

    @Mock
    private KafkaTemplate<String, BaseEvent> kafkaTemplate;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CaseController caseController;

    private static final String TEST_USER_ID = "testuser";

    @BeforeEach
    void setUp() {
        setupMockSecurityContext();
    }

    private void setupMockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn(TEST_USER_ID);
        when(authentication.getPrincipal()).thenReturn(TEST_USER_ID); // Set principal to pass null check
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should submit address change successfully for California")
    void testSubmitAddressChange_California_Success() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("CA");
        Notification mockNotification = createMockNotification();
        
        when(notificationService.createNotification(any(Notification.class))).thenReturn(mockNotification);
        CompletableFuture<SendResult<String, BaseEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(future);

        // Act
        ResponseEntity<Map<String, String>> response = caseController.submitAddressChange(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Address change submitted successfully", response.getBody().get("message"));
        assertEquals("false", response.getBody().get("willCreateTask")); // Inside CA, no task
        verify(kafkaTemplate, times(1)).send(anyString(), any(BaseEvent.class));
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }

    @Test
    @DisplayName("Should submit address change successfully for outside California")
    void testSubmitAddressChange_OutsideCA_Success() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("TX");
        Notification mockNotification = createMockNotification();
        
        when(notificationService.createNotification(any(Notification.class))).thenReturn(mockNotification);
        CompletableFuture<SendResult<String, BaseEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(future);

        // Act
        ResponseEntity<Map<String, String>> response = caseController.submitAddressChange(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("true", response.getBody().get("willCreateTask")); // Outside CA, creates task
        verify(kafkaTemplate, times(1)).send(anyString(), any(BaseEvent.class));
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle missing recipient name gracefully")
    void testSubmitAddressChange_MissingRecipientName() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("CA");
        request.remove("recipientName"); // Remove recipient name
        Notification mockNotification = createMockNotification();
        
        when(notificationService.createNotification(any(Notification.class))).thenReturn(mockNotification);
        CompletableFuture<SendResult<String, BaseEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(future);

        // Act
        ResponseEntity<Map<String, String>> response = caseController.submitAddressChange(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }

    @Test
    @DisplayName("Should create Kafka event with correct payload")
    void testSubmitAddressChange_KafkaEventPayload() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("CA");
        Notification mockNotification = createMockNotification();
        
        when(notificationService.createNotification(any(Notification.class))).thenReturn(mockNotification);
        CompletableFuture<SendResult<String, BaseEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(future);

        // Act
        caseController.submitAddressChange(request);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("cmips-case-events"), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Should handle Kafka send failure gracefully")
    void testSubmitAddressChange_KafkaFailure() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("CA");
        Notification mockNotification = createMockNotification();
        
        // Kafka sends are asynchronous - exceptions won't throw immediately
        // The controller uses fire-and-forget pattern, so it returns 200 OK
        // This test verifies that the request completes successfully even if Kafka fails later
        when(notificationService.createNotification(any(Notification.class))).thenReturn(mockNotification);
        CompletableFuture<SendResult<String, BaseEvent>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(failedFuture);

        // Act
        ResponseEntity<Map<String, String>> response = caseController.submitAddressChange(request);

        // Assert
        // Note: Kafka sends are async, so the controller returns 200 OK immediately
        // The failure happens asynchronously and doesn't affect the response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode()); // Async operation returns success
        assertNotNull(response.getBody());
        assertEquals("Address change submitted successfully", response.getBody().get("message"));
        verify(kafkaTemplate, times(1)).send(anyString(), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Should handle notification creation failure gracefully")
    void testSubmitAddressChange_NotificationFailure() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("CA");
        
        CompletableFuture<SendResult<String, BaseEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(future);
        when(notificationService.createNotification(any(Notification.class)))
                .thenThrow(new RuntimeException("Notification error"));

        // Act
        ResponseEntity<Map<String, String>> response = caseController.submitAddressChange(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    @DisplayName("Should extract current user ID from security context")
    void testSubmitAddressChange_CurrentUserId() {
        // Arrange
        Map<String, Object> request = createValidAddressChangeRequest("CA");
        Notification mockNotification = createMockNotification();
        
        when(notificationService.createNotification(any(Notification.class))).thenReturn(mockNotification);
        CompletableFuture<SendResult<String, BaseEvent>> future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), any(BaseEvent.class))).thenReturn(future);

        // Act
        ResponseEntity<Map<String, String>> response = caseController.submitAddressChange(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kafkaTemplate, times(1)).send(anyString(), argThat(event -> 
            event.getUserId().equals(TEST_USER_ID)
        ));
    }

    // Helper methods
    private Map<String, Object> createValidAddressChangeRequest(String state) {
        Map<String, Object> request = new HashMap<>();
        request.put("caseId", "CASE001");
        request.put("recipientId", "REC001");
        request.put("recipientName", "recipient1");
        request.put("providerId", "PROV001");
        
        Map<String, String> newAddress = new HashMap<>();
        newAddress.put("line1", "123 New Street");
        newAddress.put("city", "Los Angeles");
        newAddress.put("state", state);
        newAddress.put("zip", "90001");
        request.put("newAddress", newAddress);
        
        return request;
    }

    private Notification createMockNotification() {
        return Notification.builder()
                .id(1L)
                .userId("recipient1")
                .message("Test notification")
                .notificationType(Notification.NotificationType.INFO)
                .readStatus(false)
                .build();
    }
}

