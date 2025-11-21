package com.kma.studentsystem.notification_service.controller;

import com.kma.studentsystem.notification_service.dto.NotificationDTO;
import com.kma.studentsystem.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        log.info("Creating new notification for recipient: {}", notificationDTO.getRecipientId());
        try {
            NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNotification);
        } catch (Exception e) {
            log.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        log.info("Marking notification as read: {}", id);
        try {
            NotificationDTO updatedNotification = notificationService.markAsRead(id);
            return ResponseEntity.ok(updatedNotification);
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/delivered")
    public ResponseEntity<NotificationDTO> markAsDelivered(@PathVariable Long id) {
        log.info("Marking notification as delivered: {}", id);
        try {
            NotificationDTO updatedNotification = notificationService.markAsDelivered(id);
            return ResponseEntity.ok(updatedNotification);
        } catch (Exception e) {
            log.error("Error marking notification as delivered: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelNotification(@PathVariable Long id) {
        log.info("Cancelling notification: {}", id);
        try {
            notificationService.cancelNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error cancelling notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        log.info("Deleting notification: {}", id);
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        log.info("Retrieving all notifications");
        try {
            List<NotificationDTO> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error retrieving notifications: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        log.info("Retrieving notification with ID: {}", id);
        try {
            Optional<NotificationDTO> notification = notificationService.getNotificationById(id);
            return notification.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving notification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recipient/{recipientId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByRecipient(@PathVariable String recipientId) {
        log.info("Retrieving notifications for recipient: {}", recipientId);
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByRecipient(recipientId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error retrieving notifications for recipient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recipient/{recipientId}/pending")
    public ResponseEntity<List<NotificationDTO>> getPendingNotificationsByRecipient(@PathVariable String recipientId) {
        log.info("Retrieving pending notifications for recipient: {}", recipientId);
        try {
            List<NotificationDTO> notifications = notificationService.getPendingNotificationsByRecipient(recipientId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error retrieving pending notifications for recipient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recipient/{recipientId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotificationsByRecipient(@PathVariable String recipientId) {
        log.info("Retrieving unread notifications for recipient: {}", recipientId);
        try {
            List<NotificationDTO> notifications = notificationService.getUnreadNotificationsByRecipient(recipientId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error retrieving unread notifications for recipient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recipient/{recipientId}/pending/count")
    public ResponseEntity<Long> countPendingNotificationsByRecipient(@PathVariable String recipientId) {
        log.info("Counting pending notifications for recipient: {}", recipientId);
        try {
            Long count = notificationService.countPendingNotificationsByRecipient(recipientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting pending notifications for recipient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recipient/{recipientId}/unread/count")
    public ResponseEntity<Long> countUnreadNotificationsByRecipient(@PathVariable String recipientId) {
        log.info("Counting unread notifications for recipient: {}", recipientId);
        try {
            Long count = notificationService.countUnreadNotificationsByRecipient(recipientId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting unread notifications for recipient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
