package com.kma.studentsystem.notification_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Recipient ID is required")
    @Column(name = "recipient_id", nullable = false)
    private String recipientId;
    
    @NotBlank(message = "Recipient type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(name = "title", nullable = false)
    private String title;
    
    @NotBlank(message = "Message is required")
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "delivery_attempts")
    private Integer deliveryAttempts = 0;
    
    @Column(name = "max_delivery_attempts")
    private Integer maxDeliveryAttempts = 3;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "source_service")
    @Size(max = 100, message = "Source service must not exceed 100 characters")
    private String sourceService;
    
    @Column(name = "correlation_id")
    @Size(max = 100, message = "Correlation ID must not exceed 100 characters")
    private String correlationId;
    
    @Column(name = "template_id")
    @Size(max = 100, message = "Template ID must not exceed 100 characters")
    private String templateId;
    
    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables;
    
    @Column(name = "channel")
    @Enumerated(EnumType.STRING)
    private Channel channel = Channel.EMAIL;
    
    @Column(name = "external_id")
    @Size(max = 200, message = "External ID must not exceed 200 characters")
    private String externalId;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RecipientType {
        STUDENT, INSTRUCTOR, ADMIN, SYSTEM
    }
    
    public enum NotificationType {
        ENROLLMENT_CONFIRMATION,
        ENROLLMENT_CANCELLATION,
        GRADE_UPDATE,
        COURSE_UPDATE,
        DEADLINE_REMINDER,
        SYSTEM_MAINTENANCE,
        SECURITY_ALERT,
        WELCOME,
        PASSWORD_RESET,
        ACCOUNT_ACTIVATION,
        GENERAL
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, READ, FAILED, EXPIRED, CANCELLED
    }
    
    public enum Channel {
        EMAIL, SMS, PUSH, IN_APP, WEBHOOK
    }
    
    // Helper methods
    public boolean isPending() {
        return status == NotificationStatus.PENDING;
    }
    
    public boolean isSent() {
        return status == NotificationStatus.SENT;
    }
    
    public boolean isDelivered() {
        return status == NotificationStatus.DELIVERED;
    }
    
    public boolean isRead() {
        return status == NotificationStatus.READ;
    }
    
    public boolean isFailed() {
        return status == NotificationStatus.FAILED;
    }
    
    public boolean canRetry() {
        return deliveryAttempts < maxDeliveryAttempts && 
               (status == NotificationStatus.FAILED || status == NotificationStatus.PENDING);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public String getStatusDescription() {
        switch (status) {
            case PENDING: return "Chờ gửi";
            case SENT: return "Đã gửi";
            case DELIVERED: return "Đã giao";
            case READ: return "Đã đọc";
            case FAILED: return "Gửi thất bại";
            case EXPIRED: return "Hết hạn";
            case CANCELLED: return "Đã hủy";
            default: return "Không xác định";
        }
    }
    
    public String getPriorityDescription() {
        switch (priority) {
            case LOW: return "Thấp";
            case MEDIUM: return "Trung bình";
            case HIGH: return "Cao";
            case URGENT: return "Khẩn cấp";
            default: return "Không xác định";
        }
    }
}
