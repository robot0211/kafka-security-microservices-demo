package com.kma.studentsystem.student_service.event;

import com.kma.studentsystem.student_service.model.Student;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentEvent {
    
    private String eventId;
    private String eventType;
    private String studentId;
    private Student student;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
    
    public static StudentEvent createStudentCreatedEvent(Student student) {
        StudentEvent event = new StudentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("StudentCreated");
        event.setStudentId(student.getStudentId());
        event.setStudent(student);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("student-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static StudentEvent createStudentUpdatedEvent(Student student) {
        StudentEvent event = new StudentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("StudentUpdated");
        event.setStudentId(student.getStudentId());
        event.setStudent(student);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("student-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static StudentEvent createStudentDeletedEvent(String studentId) {
        StudentEvent event = new StudentEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("StudentDeleted");
        event.setStudentId(studentId);
        event.setStudent(null);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("student-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
}
