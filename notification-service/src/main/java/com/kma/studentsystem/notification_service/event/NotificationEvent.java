package com.kma.studentsystem.notification_service.event;

import com.kma.studentsystem.notification_service.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    
    private String eventId;
    private String eventType;
    private String recipientId;
    private Notification notification;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
    
    public static NotificationEvent createNotificationCreatedEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationCreated");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
    
    public static NotificationEvent createNotificationSentEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationSent");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
    
    public static NotificationEvent createNotificationDeliveredEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationDelivered");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
    
    public static NotificationEvent createNotificationReadEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationRead");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
    
    public static NotificationEvent createNotificationFailedEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationFailed");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
    
    public static NotificationEvent createNotificationExpiredEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationExpired");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
    
    public static NotificationEvent createNotificationCancelledEvent(Notification notification) {
        NotificationEvent event = new NotificationEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("NotificationCancelled");
        event.setRecipientId(notification.getRecipientId());
        event.setNotification(notification);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("notification-service");
        event.setCorrelationId(notification.getCorrelationId());
        return event;
    }
}
