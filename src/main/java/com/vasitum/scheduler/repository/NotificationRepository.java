package com.vasitum.scheduler.repository;

import com.vasitum.scheduler.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByStatusAndScheduledForBefore(
        Notification.NotificationStatus status, 
        LocalDateTime scheduledFor
    );
    
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    
    List<Notification> findByInterviewSlotId(Long interviewSlotId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < :maxRetries")
    List<Notification> findFailedNotificationsForRetry(
        @Param("status") Notification.NotificationStatus status,
        @Param("maxRetries") Integer maxRetries
    );
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    Long countByStatus(@Param("status") Notification.NotificationStatus status);
}