package com.vasitum.scheduler.service;

import com.vasitum.scheduler.entity.InterviewSlot;
import com.vasitum.scheduler.entity.Interviewer;
import com.vasitum.scheduler.entity.Notification;
import com.vasitum.scheduler.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Async
    public void sendBookingConfirmation(InterviewSlot slot) {
        try {
            String subject = "Interview Booking Confirmation";
            String content = emailService.generateBookingConfirmationEmail(
                slot.getCandidateName(),
                slot.getInterviewer().getName(),
                slot.getStartTime().format(DATE_FORMATTER),
                slot.getStartTime().format(TIME_FORMATTER)
            );
            
            Notification notification = new Notification(
                slot.getCandidateEmail(),
                subject,
                content,
                Notification.NotificationType.BOOKING_CONFIRMATION
            );
            notification.setInterviewSlotId(slot.getId());
            notification.setInterviewerId(slot.getInterviewer().getId());
            
            saveAndSendNotification(notification);
            
        } catch (Exception e) {
            logger.error("Error sending booking confirmation for slot {}: {}", slot.getId(), e.getMessage());
        }
    }
    
    @Async
    public void sendCancellationNotice(InterviewSlot slot) {
        try {
            String subject = "Interview Cancellation Notice";
            String content = emailService.generateCancellationEmail(
                slot.getCandidateName(),
                slot.getInterviewer().getName(),
                slot.getStartTime().format(DATE_FORMATTER),
                slot.getStartTime().format(TIME_FORMATTER)
            );
            
            Notification notification = new Notification(
                slot.getCandidateEmail(),
                subject,
                content,
                Notification.NotificationType.CANCELLATION_NOTICE
            );
            notification.setInterviewSlotId(slot.getId());
            notification.setInterviewerId(slot.getInterviewer().getId());
            
            saveAndSendNotification(notification);
            
        } catch (Exception e) {
            logger.error("Error sending cancellation notice for slot {}: {}", slot.getId(), e.getMessage());
        }
    }
    
    @Async
    public void scheduleInterviewReminder(InterviewSlot slot) {
        try {
            // Schedule reminder 24 hours before the interview
            LocalDateTime reminderTime = slot.getStartTime().minusHours(24);
            
            // Only schedule if the reminder time is in the future
            if (reminderTime.isAfter(LocalDateTime.now())) {
                String subject = "Interview Reminder - Tomorrow";
                String content = emailService.generateInterviewReminderEmail(
                    slot.getCandidateName(),
                    slot.getInterviewer().getName(),
                    slot.getStartTime().format(DATE_FORMATTER),
                    slot.getStartTime().format(TIME_FORMATTER)
                );
                
                Notification notification = new Notification(
                    slot.getCandidateEmail(),
                    subject,
                    content,
                    Notification.NotificationType.INTERVIEW_REMINDER
                );
                notification.setInterviewSlotId(slot.getId());
                notification.setInterviewerId(slot.getInterviewer().getId());
                notification.setScheduledFor(reminderTime);
                
                notificationRepository.save(notification);
                logger.info("Interview reminder scheduled for slot {} at {}", slot.getId(), reminderTime);
            }
            
        } catch (Exception e) {
            logger.error("Error scheduling interview reminder for slot {}: {}", slot.getId(), e.getMessage());
        }
    }
    
    @Async
    public void sendSlotGenerationAlert(Interviewer interviewer, int slotsGenerated) {
        try {
            String subject = "New Interview Slots Generated";
            String content = emailService.generateSlotGenerationAlert(
                interviewer.getName(),
                slotsGenerated
            );
            
            Notification notification = new Notification(
                interviewer.getEmail(),
                subject,
                content,
                Notification.NotificationType.SLOT_GENERATION_ALERT
            );
            notification.setInterviewerId(interviewer.getId());
            
            saveAndSendNotification(notification);
            
        } catch (Exception e) {
            logger.error("Error sending slot generation alert for interviewer {}: {}", interviewer.getId(), e.getMessage());
        }
    }
    
    private void saveAndSendNotification(Notification notification) {
        try {
            // Save notification to database
            notification = notificationRepository.save(notification);
            
            // Send email
            boolean sent = emailService.sendEmail(
                notification.getRecipientEmail(),
                notification.getSubject(),
                notification.getContent()
            );
            
            // Update notification status
            if (sent) {
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } else {
                notification.setStatus(Notification.NotificationStatus.FAILED);
                notification.setErrorMessage("Failed to send email");
            }
            
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            logger.error("Error saving/sending notification: {}", e.getMessage());
        }
    }
    
    public void processPendingNotifications() {
        try {
            List<Notification> pendingNotifications = notificationRepository
                .findByStatusAndScheduledForBefore(
                    Notification.NotificationStatus.PENDING,
                    LocalDateTime.now()
                );
            
            for (Notification notification : pendingNotifications) {
                boolean sent = emailService.sendEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getContent()
                );
                
                if (sent) {
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                } else {
                    notification.setStatus(Notification.NotificationStatus.FAILED);
                    notification.setRetryCount(notification.getRetryCount() + 1);
                    notification.setErrorMessage("Failed to send email");
                }
                
                notificationRepository.save(notification);
            }
            
            logger.info("Processed {} pending notifications", pendingNotifications.size());
            
        } catch (Exception e) {
            logger.error("Error processing pending notifications: {}", e.getMessage());
        }
    }
    
    public void retryFailedNotifications() {
        try {
            List<Notification> failedNotifications = notificationRepository
                .findFailedNotificationsForRetry(Notification.NotificationStatus.FAILED, 3);
            
            for (Notification notification : failedNotifications) {
                boolean sent = emailService.sendEmail(
                    notification.getRecipientEmail(),
                    notification.getSubject(),
                    notification.getContent()
                );
                
                if (sent) {
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                } else {
                    notification.setRetryCount(notification.getRetryCount() + 1);
                }
                
                notificationRepository.save(notification);
            }
            
            logger.info("Retried {} failed notifications", failedNotifications.size());
            
        } catch (Exception e) {
            logger.error("Error retrying failed notifications: {}", e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByEmail(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }
    
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsBySlot(Long slotId) {
        return notificationRepository.findByInterviewSlotId(slotId);
    }
}