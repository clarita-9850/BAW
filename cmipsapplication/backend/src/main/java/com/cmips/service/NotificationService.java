package com.cmips.service;

import com.cmips.entity.Notification;
import com.cmips.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

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

    /**
     * Send batch completion notification for scheduled reports
     */
    public void sendBatchCompletionNotification(int totalReports, int successfulReports, int failedReports) {
        log.info("Batch report generation completed: total={}, success={}, failed={}",
            totalReports, successfulReports, failedReports);
        // Could send to admin users or a system notification queue
    }

    /**
     * Send delivery notification for generated reports
     */
    public void sendDeliveryNotification(String userId, String reportId, String reportType, String deliveryMethod) {
        log.info("Report delivered: userId={}, reportId={}, type={}, method={}",
            userId, reportId, reportType, deliveryMethod);

        Notification notification = Notification.builder()
            .userId(userId)
            .message("Report " + reportType + " has been delivered via " + deliveryMethod)
            .notificationType(Notification.NotificationType.INFO)
            .actionLink("/reports/" + reportId)
            .readStatus(false)
            .build();

        createNotification(notification);
    }

    /**
     * Send error notification for failed operations
     */
    public void sendErrorNotification(String userId, String errorMessage) {
        log.error("Error notification for user {}: {}", userId, errorMessage);

        Notification notification = Notification.builder()
            .userId(userId)
            .message(errorMessage)
            .notificationType(Notification.NotificationType.ALERT)
            .readStatus(false)
            .build();

        createNotification(notification);
    }
}
