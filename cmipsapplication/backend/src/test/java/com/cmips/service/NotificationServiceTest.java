package com.cmips.service;

import com.cmips.entity.Notification;
import com.cmips.repository.NotificationRepository;
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
 * Unit tests for NotificationService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private static final String TEST_USER_ID = "user123";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should create notification successfully")
    void testCreateNotification_Success() {
        // Arrange
        Notification notification = createMockNotification(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        Notification result = notificationService.createNotification(notification);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("Should get user notifications successfully")
    void testGetUserNotifications_Success() {
        // Arrange
        List<Notification> mockNotifications = Arrays.asList(
                createMockNotification(1L),
                createMockNotification(2L)
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(TEST_USER_ID))
                .thenReturn(mockNotifications);

        // Act
        List<Notification> result = notificationService.getUserNotifications(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should get unread notification count successfully")
    void testGetUnreadNotificationCount_Success() {
        // Arrange
        Long expectedCount = 3L;
        when(notificationRepository.countByUserIdAndReadStatus(TEST_USER_ID, false))
                .thenReturn(expectedCount);

        // Act
        Long result = notificationService.getUnreadNotificationCount(TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(expectedCount, result);
        verify(notificationRepository, times(1)).countByUserIdAndReadStatus(TEST_USER_ID, false);
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void testMarkAsRead_Success() {
        // Arrange
        Long notificationId = 1L;
        Notification notification = createMockNotification(notificationId);
        notification.setReadStatus(false);
        
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        assertTrue(notification.getReadStatus());
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("Should handle mark as read when notification not found")
    void testMarkAsRead_NotFound() {
        // Arrange
        Long notificationId = 999L;
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act
        notificationService.markAsRead(notificationId);

        // Assert
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should mark all notifications as read successfully")
    void testMarkAllAsRead_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(
                createMockNotification(1L),
                createMockNotification(2L)
        );
        when(notificationRepository.findByUserId(TEST_USER_ID)).thenReturn(notifications);
        when(notificationRepository.saveAll(anyList())).thenReturn(notifications);

        // Act
        notificationService.markAllAsRead(TEST_USER_ID);

        // Assert
        assertTrue(notifications.get(0).getReadStatus());
        assertTrue(notifications.get(1).getReadStatus());
        verify(notificationRepository, times(1)).findByUserId(TEST_USER_ID);
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Should delete notification successfully")
    void testDeleteNotification_Success() {
        // Arrange
        Long notificationId = 1L;
        doNothing().when(notificationRepository).deleteById(notificationId);

        // Act
        notificationService.deleteNotification(notificationId);

        // Assert
        verify(notificationRepository, times(1)).deleteById(notificationId);
    }

    @Test
    @DisplayName("Should clear read notifications successfully")
    void testClearReadNotifications_Success() {
        // Arrange
        doNothing().when(notificationRepository).deleteByUserIdAndReadStatus(TEST_USER_ID, true);

        // Act
        notificationService.clearReadNotifications(TEST_USER_ID);

        // Assert
        verify(notificationRepository, times(1)).deleteByUserIdAndReadStatus(TEST_USER_ID, true);
    }

    // Helper methods
    private Notification createMockNotification(Long id) {
        return Notification.builder()
                .id(id)
                .userId(TEST_USER_ID)
                .message("Test notification " + id)
                .notificationType(Notification.NotificationType.INFO)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}







