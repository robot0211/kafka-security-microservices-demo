package com.kma.studentsystem.identity_service.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IdentityEventListener {
    
    @KafkaListener(topics = "student-events", groupId = "identity-service-group")
    public void handleStudentEvents(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Received student event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Student event message: {}", message);
            
            // Process student events (e.g., student created, student updated, student deleted)
            // This could trigger user account creation, role updates, etc.
            
            acknowledgment.acknowledge();
            log.info("Successfully processed student event");
        } catch (Exception e) {
            log.error("Error processing student event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "course-events", groupId = "identity-service-group")
    public void handleCourseEvents(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        try {
            log.info("Received course event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Course event message: {}", message);
            
            // Process course events (e.g., course created, course updated, course deleted)
            // This could trigger instructor account creation, role updates, etc.
            
            acknowledgment.acknowledge();
            log.info("Successfully processed course event");
        } catch (Exception e) {
            log.error("Error processing course event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "grade-events", groupId = "identity-service-group")
    public void handleGradeEvents(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        try {
            log.info("Received grade event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Grade event message: {}", message);
            
            // Process grade events (e.g., grade updated, grade published)
            // This could trigger access control updates, notifications, etc.
            
            acknowledgment.acknowledge();
            log.info("Successfully processed grade event");
        } catch (Exception e) {
            log.error("Error processing grade event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "enrollment-events", groupId = "identity-service-group")
    public void handleEnrollmentEvents(@Payload String message,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                      @Header(KafkaHeaders.OFFSET) long offset,
                                      Acknowledgment acknowledgment) {
        try {
            log.info("Received enrollment event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Enrollment event message: {}", message);
            
            // Process enrollment events (e.g., enrollment created, enrollment approved, enrollment completed)
            // This could trigger access control updates, role changes, etc.
            
            acknowledgment.acknowledge();
            log.info("Successfully processed enrollment event");
        } catch (Exception e) {
            log.error("Error processing enrollment event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "notification-events", groupId = "identity-service-group")
    public void handleNotificationEvents(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment acknowledgment) {
        try {
            log.info("Received notification event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Notification event message: {}", message);
            
            // Process notification events (e.g., notification sent, notification failed)
            // This could trigger user preference updates, security alerts, etc.
            
            acknowledgment.acknowledge();
            log.info("Successfully processed notification event");
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
}
