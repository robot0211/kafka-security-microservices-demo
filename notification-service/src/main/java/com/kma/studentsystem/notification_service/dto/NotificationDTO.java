package com.kma.studentsystem.notification_service.dto;

import com.kma.studentsystem.notification_service.model.Notification;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long id;
    
    @NotNull(message = "Recipient ID is required")
    private String recipientId;
    
    @NotNull(message = "Recipient type is required")
    private Notification.RecipientType recipientType;
    
    @NotNull(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotNull(message = "Message is required")
    private String message;
    
    private Notification.NotificationType type;
    
    private Notification.Priority priority;
    
    private Notification.NotificationStatus status;
    
    private LocalDateTime sentAt;
    
    private LocalDateTime readAt;
    
    private Integer deliveryAttempts;
    
    private Integer maxDeliveryAttempts;
    
    private LocalDateTime nextRetryAt;
    
    private LocalDateTime expiresAt;
    
    private String metadata;
    
    @Size(max = 100, message = "Source service must not exceed 100 characters")
    private String sourceService;
    
    @Size(max = 100, message = "Correlation ID must not exceed 100 characters")
    private String correlationId;
    
    @Size(max = 100, message = "Template ID must not exceed 100 characters")
    private String templateId;
    
    private String templateVariables;
    
    private Notification.Channel channel;
    
    @Size(max = 200, message = "External ID must not exceed 200 characters")
    private String externalId;
    
    private String errorMessage;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isPending() {
        return status == Notification.NotificationStatus.PENDING;
    }
    
    public boolean isSent() {
        return status == Notification.NotificationStatus.SENT;
    }
    
    public boolean isDelivered() {
        return status == Notification.NotificationStatus.DELIVERED;
    }
    
    public boolean isRead() {
        return status == Notification.NotificationStatus.READ;
    }
    
    public boolean isFailed() {
        return status == Notification.NotificationStatus.FAILED;
    }
    
    public boolean isExpired() {
        return status == Notification.NotificationStatus.EXPIRED;
    }
    
    public boolean canRetry() {
        return deliveryAttempts < maxDeliveryAttempts && 
               (status == Notification.NotificationStatus.FAILED || status == Notification.NotificationStatus.PENDING);
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
