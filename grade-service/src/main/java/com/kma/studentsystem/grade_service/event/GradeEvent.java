package com.kma.studentsystem.grade_service.event;

import com.kma.studentsystem.grade_service.model.Grade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeEvent {
    
    private String eventId;
    private String eventType;
    private String studentId;
    private String courseCode;
    private Grade grade;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
    
    public static GradeEvent createGradeAssignedEvent(Grade grade) {
        GradeEvent event = new GradeEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("GradeAssigned");
        event.setStudentId(grade.getStudentId());
        event.setCourseCode(grade.getCourseCode());
        event.setGrade(grade);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("grade-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static GradeEvent createGradeUpdatedEvent(Grade grade) {
        GradeEvent event = new GradeEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("GradeUpdated");
        event.setStudentId(grade.getStudentId());
        event.setCourseCode(grade.getCourseCode());
        event.setGrade(grade);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("grade-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static GradeEvent createGradeFinalizedEvent(Grade grade) {
        GradeEvent event = new GradeEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("GradeFinalized");
        event.setStudentId(grade.getStudentId());
        event.setCourseCode(grade.getCourseCode());
        event.setGrade(grade);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("grade-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static GradeEvent createGradeDeletedEvent(String studentId, String courseCode) {
        GradeEvent event = new GradeEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("GradeDeleted");
        event.setStudentId(studentId);
        event.setCourseCode(courseCode);
        event.setGrade(null);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("grade-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
}
