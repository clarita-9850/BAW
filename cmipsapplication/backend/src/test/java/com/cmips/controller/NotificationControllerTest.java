package com.cmips.controller;

import com.cmips.entity.Notification;
import com.cmips.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationController
 * 
 * Tests cover:
 * - Create notification
 * - Get user notifications
 * - Get unread count
 * - Mark as read
 * - Mark all as read
 * - Delete notification
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private static final String TEST_USER_ID = "testuser";
    private static final Long TEST_NOTIFICATION_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get user notifications successfully")
    void testGetUserNotifications_Success() {
        // Arrange
        List<Notification> mockNotifications = createMockNotifications(3);
        when(notificationService.getUserNotifications(TEST_USER_ID)).thenReturn(mockNotifications);

        // Act
        ResponseEntity<List<Notification>> response = notificationController.getUserNotifications(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(notificationService, times(1)).getUserNotifications(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return empty list when user has no notifications")
    void testGetUserNotifications_Empty() {
        // Arrange
        when(notificationService.getUserNotifications(TEST_USER_ID)).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<Notification>> response = notificationController.getUserNotifications(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(notificationService, times(1)).getUserNotifications(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should get unread count successfully")
    void testGetUnreadCount_Success() {
        // Arrange
        Long unreadCount = 5L;
        when(notificationService.getUnreadNotificationCount(TEST_USER_ID)).thenReturn(unreadCount);

        // Act
        ResponseEntity<Long> response = notificationController.getUnreadCount(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(unreadCount, response.getBody());
        verify(notificationService, times(1)).getUnreadNotificationCount(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return zero when no unread notifications")
    void testGetUnreadCount_Zero() {
        // Arrange
        when(notificationService.getUnreadNotificationCount(TEST_USER_ID)).thenReturn(0L);

        // Act
        ResponseEntity<Long> response = notificationController.getUnreadCount(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0L, response.getBody());
        verify(notificationService, times(1)).getUnreadNotificationCount(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead_Success() {
        // Arrange
        doNothing().when(notificationService).markAsRead(TEST_NOTIFICATION_ID);

        // Act
        ResponseEntity<Void> response = notificationController.markAsRead(TEST_NOTIFICATION_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService, times(1)).markAsRead(TEST_NOTIFICATION_ID);
    }

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void testMarkAllAsRead_Success() {
        // Arrange
        doNothing().when(notificationService).markAllAsRead(TEST_USER_ID);

        // Act
        ResponseEntity<Void> response = notificationController.markAllAsRead(TEST_USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService, times(1)).markAllAsRead(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should delete notification successfully")
    void testDeleteNotification_Success() {
        // Arrange
        doNothing().when(notificationService).deleteNotification(TEST_NOTIFICATION_ID);

        // Act
        ResponseEntity<Void> response = notificationController.deleteNotification(TEST_NOTIFICATION_ID);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(notificationService, times(1)).deleteNotification(TEST_NOTIFICATION_ID);
    }

    @Test
    @DisplayName("Should create notification successfully")
    void testCreateNotification_Success() {
        // Arrange
        Notification notificationToCreate = createMockNotification(null);
        Notification createdNotification = createMockNotification(TEST_NOTIFICATION_ID);
        when(notificationService.createNotification(any(Notification.class))).thenReturn(createdNotification);

        // Act
        ResponseEntity<Notification> response = notificationController.createNotification(notificationToCreate);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_NOTIFICATION_ID, response.getBody().getId());
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }

    // Helper methods
    private Notification createMockNotification(Long id) {
        Notification notification = new Notification();
        if (id != null) {
            notification.setId(id);
        }
        notification.setUserId(TEST_USER_ID);
        notification.setMessage("Test notification message");
        notification.setNotificationType(Notification.NotificationType.INFO);
        notification.setReadStatus(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private List<Notification> createMockNotifications(int count) {
        List<Notification> notifications = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            notifications.add(createMockNotification((long) i));
        }
        return notifications;
    }
}

