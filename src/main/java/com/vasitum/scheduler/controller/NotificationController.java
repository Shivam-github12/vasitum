package com.vasitum.scheduler.controller;

import com.vasitum.scheduler.entity.Notification;
import com.vasitum.scheduler.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Notification>> getNotificationsByEmail(@PathVariable String email) {
        List<Notification> notifications = notificationService.getNotificationsByEmail(email);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/slot/{slotId}")
    public ResponseEntity<List<Notification>> getNotificationsBySlot(@PathVariable Long slotId) {
        List<Notification> notifications = notificationService.getNotificationsBySlot(slotId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/process-pending")
    public ResponseEntity<String> processPendingNotifications() {
        notificationService.processPendingNotifications();
        return ResponseEntity.ok("Pending notifications processed");
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok("Failed notifications retried");
    }
}