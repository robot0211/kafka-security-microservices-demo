package com.kma.studentsystem.enrollment_service.event;

import com.kma.studentsystem.enrollment_service.model.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentEvent {
    
    private String eventId;
    private String eventType;
    private String studentId;
    private String courseCode;
    private Enrollment enrollment;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
    
    public static EnrollmentEvent createEnrollmentCreatedEvent(Enrollment enrollment) {
        EnrollmentEvent event = new EnrollmentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EnrollmentCreated");
        event.setStudentId(enrollment.getStudentId());
        event.setCourseCode(enrollment.getCourseCode());
        event.setEnrollment(enrollment);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("enrollment-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static EnrollmentEvent createEnrollmentUpdatedEvent(Enrollment enrollment) {
        EnrollmentEvent event = new EnrollmentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EnrollmentUpdated");
        event.setStudentId(enrollment.getStudentId());
        event.setCourseCode(enrollment.getCourseCode());
        event.setEnrollment(enrollment);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("enrollment-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static EnrollmentEvent createEnrollmentCancelledEvent(Enrollment enrollment) {
        EnrollmentEvent event = new EnrollmentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EnrollmentCancelled");
        event.setStudentId(enrollment.getStudentId());
        event.setCourseCode(enrollment.getCourseCode());
        event.setEnrollment(enrollment);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("enrollment-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static EnrollmentEvent createEnrollmentCompletedEvent(Enrollment enrollment) {
        EnrollmentEvent event = new EnrollmentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EnrollmentCompleted");
        event.setStudentId(enrollment.getStudentId());
        event.setCourseCode(enrollment.getCourseCode());
        event.setEnrollment(enrollment);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("enrollment-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static EnrollmentEvent createEnrollmentDeletedEvent(String studentId, String courseCode) {
        EnrollmentEvent event = new EnrollmentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EnrollmentDeleted");
        event.setStudentId(studentId);
        event.setCourseCode(courseCode);
        event.setEnrollment(null);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("enrollment-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
}
