package com.kma.studentsystem.student_service.listener;

import com.kma.studentsystem.student_service.event.StudentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StudentEventListener {
    
    @KafkaListener(topics = "student-events", groupId = "student-service-group")
    public void handleStudentEvent(@Payload StudentEvent event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        
        log.info("Received StudentEvent: {} from topic: {}, partition: {}, offset: {}", 
                event.getEventType(), topic, partition, offset);
        
        try {
            switch (event.getEventType()) {
                case "StudentCreated":
                    handleStudentCreated(event);
                    break;
                case "StudentUpdated":
                    handleStudentUpdated(event);
                    break;
                case "StudentDeleted":
                    handleStudentDeleted(event);
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
            
            // Acknowledge the message
            acknowledgment.acknowledge();
            log.info("Successfully processed StudentEvent: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing StudentEvent: {}", event.getEventId(), e);
            // In a real application, you might want to implement retry logic or dead letter queue
        }
    }
    
    private void handleStudentCreated(StudentEvent event) {
        log.info("Processing StudentCreated event for student: {}", event.getStudentId());
        // Here you can implement business logic for when a student is created
        // For example: send welcome email, create default records, etc.
    }
    
    private void handleStudentUpdated(StudentEvent event) {
        log.info("Processing StudentUpdated event for student: {}", event.getStudentId());
        // Here you can implement business logic for when a student is updated
        // For example: update related records, send notification, etc.
    }
    
    private void handleStudentDeleted(StudentEvent event) {
        log.info("Processing StudentDeleted event for student: {}", event.getStudentId());
        // Here you can implement business logic for when a student is deleted
        // For example: cleanup related records, send notification, etc.
    }
}
