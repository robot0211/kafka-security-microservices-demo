package com.kma.studentsystem.notification_service.repository;

import com.kma.studentsystem.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientId(String recipientId);
    
    List<Notification> findByRecipientType(Notification.RecipientType recipientType);
    
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    List<Notification> findByType(Notification.NotificationType type);
    
    List<Notification> findByPriority(Notification.Priority priority);
    
    List<Notification> findByChannel(Notification.Channel channel);
    
    List<Notification> findBySourceService(String sourceService);
    
    List<Notification> findByCorrelationId(String correlationId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'PENDING'")
    List<Notification> findPendingNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'SENT'")
    List<Notification> findSentNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'DELIVERED'")
    List<Notification> findDeliveredNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'READ'")
    List<Notification> findReadNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'FAILED'")
    List<Notification> findFailedNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.nextRetryAt <= :now")
    List<Notification> findNotificationsReadyForRetry(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.expiresAt <= :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.createdAt >= :since")
    List<Notification> findNotificationsByRecipientSince(@Param("recipientId") String recipientId, @Param("since") LocalDateTime since);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.type = :type")
    List<Notification> findNotificationsByRecipientAndType(@Param("recipientId") String recipientId, @Param("type") Notification.NotificationType type);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.priority = :priority")
    List<Notification> findNotificationsByRecipientAndPriority(@Param("recipientId") String recipientId, @Param("priority") Notification.Priority priority);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'PENDING'")
    Long countPendingNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'UNREAD'")
    Long countUnreadNotificationsByRecipient(@Param("recipientId") String recipientId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'PENDING'")
    Long countPendingNotifications();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'FAILED'")
    Long countFailedNotifications();
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByRecipientOrderByCreatedAtDesc(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status IN ('SENT', 'DELIVERED') ORDER BY n.sentAt DESC")
    List<Notification> findSentNotificationsByRecipientOrderBySentAtDesc(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'READ' ORDER BY n.readAt DESC")
    List<Notification> findReadNotificationsByRecipientOrderByReadAtDesc(@Param("recipientId") String recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.deliveryAttempts >= n.maxDeliveryAttempts AND n.status = 'FAILED'")
    List<Notification> findNotificationsWithMaxDeliveryAttempts();
    
    @Query("SELECT n FROM Notification n WHERE n.externalId = :externalId")
    Optional<Notification> findByExternalId(@Param("externalId") String externalId);
    
    @Query("SELECT n FROM Notification n WHERE n.templateId = :templateId")
    List<Notification> findByTemplateId(@Param("templateId") String templateId);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.createdAt BETWEEN :startDate AND :endDate")
    List<Notification> findNotificationsByRecipientAndDateRange(@Param("recipientId") String recipientId, 
                                                               @Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.priority = :priority ORDER BY n.createdAt ASC")
    List<Notification> findPendingNotificationsByPriorityOrderByCreatedAt(@Param("priority") Notification.Priority priority);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'PENDING' AND n.priority = :priority")
    List<Notification> findPendingNotificationsByRecipientAndPriority(@Param("recipientId") String recipientId, 
                                                                     @Param("priority") Notification.Priority priority);
}
