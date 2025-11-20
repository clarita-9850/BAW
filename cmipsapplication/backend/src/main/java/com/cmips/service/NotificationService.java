package com.cmips.service;

import com.cmips.entity.Notification;
import com.cmips.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public Notification createNotification(Notification notification) {
        log.info("Creating notification for user: {}", notification.getUserId());
        return notificationRepository.save(notification);
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Long getUnreadNotificationCount(String userId) {
        return notificationRepository.countByUserIdAndReadStatus(userId, false);
    }
    
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
            .ifPresent(notification -> {
                notification.setReadStatus(true);
                notificationRepository.save(notification);
            });
    }
    
    public void markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        notifications.forEach(notification -> notification.setReadStatus(true));
        notificationRepository.saveAll(notifications);
    }
    
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    public void clearReadNotifications(String userId) {
        notificationRepository.deleteByUserIdAndReadStatus(userId, true);
    }
}




