package com.cmips.repository;

import com.cmips.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndReadStatus(String userId, Boolean readStatus);
    Long countByUserIdAndReadStatus(String userId, Boolean readStatus);
    void deleteByUserIdAndReadStatus(String userId, Boolean readStatus);
}

