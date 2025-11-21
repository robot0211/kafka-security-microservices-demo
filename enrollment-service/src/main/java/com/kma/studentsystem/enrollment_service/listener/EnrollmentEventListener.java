package com.kma.studentsystem.enrollment_service.listener;

import com.kma.studentsystem.enrollment_service.event.EnrollmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EnrollmentEventListener {
    
    @KafkaListener(topics = "student-events", groupId = "enrollment-service-group")
    public void handleStudentEvents(@Payload String message,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Received student event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Student event message: {}", message);
            
            // Process student events (e.g., student deleted, student updated)
            // This could trigger enrollment cleanup or updates
            
            acknowledgment.acknowledge();
            log.info("Successfully processed student event");
        } catch (Exception e) {
            log.error("Error processing student event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "course-events", groupId = "enrollment-service-group")
    public void handleCourseEvents(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        try {
            log.info("Received course event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Course event message: {}", message);
            
            // Process course events (e.g., course deleted, course updated)
            // This could trigger enrollment updates or notifications
            
            acknowledgment.acknowledge();
            log.info("Successfully processed course event");
        } catch (Exception e) {
            log.error("Error processing course event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "grade-events", groupId = "enrollment-service-group")
    public void handleGradeEvents(@Payload String message,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        try {
            log.info("Received grade event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Grade event message: {}", message);
            
            // Process grade events (e.g., grade updated, grade deleted)
            // This could trigger enrollment completion or updates
            
            acknowledgment.acknowledge();
            log.info("Successfully processed grade event");
        } catch (Exception e) {
            log.error("Error processing grade event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "notification-events", groupId = "enrollment-service-group")
    public void handleNotificationEvents(@Payload String message,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment acknowledgment) {
        try {
            log.info("Received notification event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Notification event message: {}", message);
            
            // Process notification events (e.g., notification sent, notification failed)
            // This could trigger enrollment status updates
            
            acknowledgment.acknowledge();
            log.info("Successfully processed notification event");
        } catch (Exception e) {
            log.error("Error processing notification event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "identity-events", groupId = "enrollment-service-group")
    public void handleIdentityEvents(@Payload String message,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {
        try {
            log.info("Received identity event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Identity event message: {}", message);
            
            // Process identity events (e.g., user role changed, user deleted)
            // This could trigger enrollment access control updates
            
            acknowledgment.acknowledge();
            log.info("Successfully processed identity event");
        } catch (Exception e) {
            log.error("Error processing identity event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
}
