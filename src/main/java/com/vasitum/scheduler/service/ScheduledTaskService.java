package com.vasitum.scheduler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);
    
    @Autowired
    private NotificationService notificationService;
    
    // Process pending notifications every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void processPendingNotifications() {
        logger.debug("Processing pending notifications...");
        notificationService.processPendingNotifications();
    }
    
    // Retry failed notifications every 30 minutes
    @Scheduled(fixedRate = 1800000) // 30 minutes in milliseconds
    public void retryFailedNotifications() {
        logger.debug("Retrying failed notifications...");
        notificationService.retryFailedNotifications();
    }
}