package com.kma.studentsystem.notification_service.service;

import com.kma.studentsystem.notification_service.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    
    public boolean sendSms(Notification notification) {
        try {
            log.info("Sending SMS notification: {} to recipient: {}", 
                    notification.getId(), notification.getRecipientId());
            
            // Simulate SMS sending
            // In a real implementation, you would integrate with an SMS service like:
            // - Twilio
            // - Amazon SNS
            // - Vonage (Nexmo)
            // - Local SMS gateway
            
            // For demo purposes, we'll simulate success
            Thread.sleep(50); // Simulate network delay
            
            log.info("SMS sent successfully: {}", notification.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public boolean sendBulkSms(java.util.List<Notification> notifications) {
        try {
            log.info("Sending bulk SMS notifications: {} SMS", notifications.size());
            
            int successCount = 0;
            for (Notification notification : notifications) {
                if (sendSms(notification)) {
                    successCount++;
                }
            }
            
            log.info("Bulk SMS sending completed: {}/{} successful", successCount, notifications.size());
            return successCount == notifications.size();
            
        } catch (Exception e) {
            log.error("Failed to send bulk SMS notifications: {}", e.getMessage(), e);
            return false;
        }
    }
}
