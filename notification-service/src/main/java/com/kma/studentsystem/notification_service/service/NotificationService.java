package com.kma.studentsystem.notification_service.service;

import com.kma.studentsystem.notification_service.dto.NotificationDTO;
import com.kma.studentsystem.notification_service.event.NotificationEvent;
import com.kma.studentsystem.notification_service.model.Notification;
import com.kma.studentsystem.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;
    
    private static final String NOTIFICATION_EVENTS_TOPIC = "notification-events";
    
    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        log.info("Creating new notification for recipient: {}", notificationDTO.getRecipientId());
        
        // Convert DTO to Entity
        Notification notification = convertToEntity(notificationDTO);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setDeliveryAttempts(0);
        notification.setNextRetryAt(LocalDateTime.now().plusMinutes(1));
        
        // Set expiration time if not provided
        if (notification.getExpiresAt() == null) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
        }
        
        // Save notification
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully: {}", savedNotification.getId());
        
        // Publish event
        NotificationEvent event = NotificationEvent.createNotificationCreatedEvent(savedNotification);
        kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, savedNotification.getRecipientId(), event);
        log.info("Published NotificationCreated event for recipient: {}", savedNotification.getRecipientId());
        
        // Send notification asynchronously
        sendNotificationAsync(savedNotification);
        
        return convertToDTO(savedNotification);
    }
    
    @Async
    public void sendNotificationAsync(Notification notification) {
        try {
            sendNotification(notification);
        } catch (Exception e) {
            log.error("Error sending notification asynchronously: {}", e.getMessage(), e);
        }
    }
    
    public void sendNotification(Notification notification) {
        log.info("Sending notification: {} to recipient: {}", notification.getId(), notification.getRecipientId());
        
        try {
            // Check if notification is expired
            if (notification.isExpired()) {
                notification.setStatus(Notification.NotificationStatus.EXPIRED);
                notificationRepository.save(notification);
                
                NotificationEvent event = NotificationEvent.createNotificationExpiredEvent(notification);
                kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, notification.getRecipientId(), event);
                return;
            }
            
            // Send based on channel
            boolean sent = false;
            switch (notification.getChannel()) {
                case EMAIL:
                    sent = emailService.sendEmail(notification);
                    break;
                case SMS:
                    sent = smsService.sendSms(notification);
                    break;
                case PUSH:
                    sent = pushNotificationService.sendPushNotification(notification);
                    break;
                case IN_APP:
                    sent = sendInAppNotification(notification);
                    break;
                case WEBHOOK:
                    sent = sendWebhookNotification(notification);
                    break;
                default:
                    log.warn("Unknown notification channel: {}", notification.getChannel());
            }
            
            if (sent) {
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setDeliveryAttempts(notification.getDeliveryAttempts() + 1);
                notification.setErrorMessage(null);
                
                notificationRepository.save(notification);
                
                NotificationEvent event = NotificationEvent.createNotificationSentEvent(notification);
                kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, notification.getRecipientId(), event);
                log.info("Notification sent successfully: {}", notification.getId());
            } else {
                handleNotificationFailure(notification, "Failed to send notification");
            }
            
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            handleNotificationFailure(notification, e.getMessage());
        }
    }
    
    private void handleNotificationFailure(Notification notification, String errorMessage) {
        notification.setDeliveryAttempts(notification.getDeliveryAttempts() + 1);
        notification.setErrorMessage(errorMessage);
        
        if (notification.getDeliveryAttempts() >= notification.getMaxDeliveryAttempts()) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setNextRetryAt(null);
            
            NotificationEvent event = NotificationEvent.createNotificationFailedEvent(notification);
            kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, notification.getRecipientId(), event);
            log.error("Notification failed permanently: {}", notification.getId());
        } else {
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(5 * notification.getDeliveryAttempts()));
            log.warn("Notification failed, will retry: {}", notification.getId());
        }
        
        notificationRepository.save(notification);
    }
    
    private boolean sendInAppNotification(Notification notification) {
        // Implement in-app notification logic
        log.info("Sending in-app notification: {}", notification.getId());
        return true;
    }
    
    private boolean sendWebhookNotification(Notification notification) {
        // Implement webhook notification logic
        log.info("Sending webhook notification: {}", notification.getId());
        return true;
    }
    
    public NotificationDTO markAsRead(Long id) {
        log.info("Marking notification as read: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        if (notification.getStatus() != Notification.NotificationStatus.DELIVERED) {
            throw new RuntimeException("Only delivered notifications can be marked as read");
        }
        
        notification.setStatus(Notification.NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification marked as read: {}", updatedNotification.getId());
        
        // Publish event
        NotificationEvent event = NotificationEvent.createNotificationReadEvent(updatedNotification);
        kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, updatedNotification.getRecipientId(), event);
        log.info("Published NotificationRead event for recipient: {}", updatedNotification.getRecipientId());
        
        return convertToDTO(updatedNotification);
    }
    
    public NotificationDTO markAsDelivered(Long id) {
        log.info("Marking notification as delivered: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        if (notification.getStatus() != Notification.NotificationStatus.SENT) {
            throw new RuntimeException("Only sent notifications can be marked as delivered");
        }
        
        notification.setStatus(Notification.NotificationStatus.DELIVERED);
        
        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification marked as delivered: {}", updatedNotification.getId());
        
        // Publish event
        NotificationEvent event = NotificationEvent.createNotificationDeliveredEvent(updatedNotification);
        kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, updatedNotification.getRecipientId(), event);
        log.info("Published NotificationDelivered event for recipient: {}", updatedNotification.getRecipientId());
        
        return convertToDTO(updatedNotification);
    }
    
    public void cancelNotification(Long id) {
        log.info("Cancelling notification: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        if (notification.getStatus() == Notification.NotificationStatus.SENT || 
            notification.getStatus() == Notification.NotificationStatus.DELIVERED ||
            notification.getStatus() == Notification.NotificationStatus.READ) {
            throw new RuntimeException("Cannot cancel sent, delivered, or read notifications");
        }
        
        notification.setStatus(Notification.NotificationStatus.CANCELLED);
        notification.setNextRetryAt(null);
        
        notificationRepository.save(notification);
        log.info("Notification cancelled: {}", notification.getId());
        
        // Publish event
        NotificationEvent event = NotificationEvent.createNotificationCancelledEvent(notification);
        kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, notification.getRecipientId(), event);
        log.info("Published NotificationCancelled event for recipient: {}", notification.getRecipientId());
    }
    
    public void deleteNotification(Long id) {
        log.info("Deleting notification: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        notificationRepository.delete(notification);
        log.info("Notification deleted: {}", id);
    }
    
    @Scheduled(fixedDelay = 60000) // Run every minute
    public void processPendingNotifications() {
        log.debug("Processing pending notifications");
        
        List<Notification> pendingNotifications = notificationRepository.findNotificationsReadyForRetry(LocalDateTime.now());
        
        for (Notification notification : pendingNotifications) {
            if (notification.canRetry()) {
                sendNotification(notification);
            }
        }
        
        // Process expired notifications
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        for (Notification notification : expiredNotifications) {
            notification.setStatus(Notification.NotificationStatus.EXPIRED);
            notificationRepository.save(notification);
            
            NotificationEvent event = NotificationEvent.createNotificationExpiredEvent(notification);
            kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, notification.getRecipientId(), event);
        }
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        log.info("Retrieving all notifications");
        return notificationRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<NotificationDTO> getNotificationById(Long id) {
        log.info("Retrieving notification with ID: {}", id);
        return notificationRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByRecipient(String recipientId) {
        log.info("Retrieving notifications for recipient: {}", recipientId);
        return notificationRepository.findByRecipientId(recipientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getPendingNotificationsByRecipient(String recipientId) {
        log.info("Retrieving pending notifications for recipient: {}", recipientId);
        return notificationRepository.findPendingNotificationsByRecipient(recipientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsByRecipient(String recipientId) {
        log.info("Retrieving unread notifications for recipient: {}", recipientId);
        return notificationRepository.findByRecipientId(recipientId).stream()
                .filter(n -> n.getStatus() == Notification.NotificationStatus.DELIVERED)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long countPendingNotificationsByRecipient(String recipientId) {
        log.info("Counting pending notifications for recipient: {}", recipientId);
        return notificationRepository.countPendingNotificationsByRecipient(recipientId);
    }
    
    @Transactional(readOnly = true)
    public Long countUnreadNotificationsByRecipient(String recipientId) {
        log.info("Counting unread notifications for recipient: {}", recipientId);
        return notificationRepository.countUnreadNotificationsByRecipient(recipientId);
    }
    
    private Notification convertToEntity(NotificationDTO dto) {
        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setRecipientId(dto.getRecipientId());
        notification.setRecipientType(dto.getRecipientType());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setType(dto.getType());
        notification.setPriority(dto.getPriority());
        notification.setStatus(dto.getStatus());
        notification.setSentAt(dto.getSentAt());
        notification.setReadAt(dto.getReadAt());
        notification.setDeliveryAttempts(dto.getDeliveryAttempts());
        notification.setMaxDeliveryAttempts(dto.getMaxDeliveryAttempts());
        notification.setNextRetryAt(dto.getNextRetryAt());
        notification.setExpiresAt(dto.getExpiresAt());
        notification.setMetadata(dto.getMetadata());
        notification.setSourceService(dto.getSourceService());
        notification.setCorrelationId(dto.getCorrelationId());
        notification.setTemplateId(dto.getTemplateId());
        notification.setTemplateVariables(dto.getTemplateVariables());
        notification.setChannel(dto.getChannel());
        notification.setExternalId(dto.getExternalId());
        notification.setErrorMessage(dto.getErrorMessage());
        return notification;
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipientId());
        dto.setRecipientType(notification.getRecipientType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setPriority(notification.getPriority());
        dto.setStatus(notification.getStatus());
        dto.setSentAt(notification.getSentAt());
        dto.setReadAt(notification.getReadAt());
        dto.setDeliveryAttempts(notification.getDeliveryAttempts());
        dto.setMaxDeliveryAttempts(notification.getMaxDeliveryAttempts());
        dto.setNextRetryAt(notification.getNextRetryAt());
        dto.setExpiresAt(notification.getExpiresAt());
        dto.setMetadata(notification.getMetadata());
        dto.setSourceService(notification.getSourceService());
        dto.setCorrelationId(notification.getCorrelationId());
        dto.setTemplateId(notification.getTemplateId());
        dto.setTemplateVariables(notification.getTemplateVariables());
        dto.setChannel(notification.getChannel());
        dto.setExternalId(notification.getExternalId());
        dto.setErrorMessage(notification.getErrorMessage());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        return dto;
    }
}
