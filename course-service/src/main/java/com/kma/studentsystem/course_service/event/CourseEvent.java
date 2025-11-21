package com.kma.studentsystem.course_service.event;

import com.kma.studentsystem.course_service.model.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseEvent {
    
    private String eventId;
    private String eventType;
    private String courseCode;
    private Course course;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
    
    public static CourseEvent createCourseCreatedEvent(Course course) {
        CourseEvent event = new CourseEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("CourseCreated");
        event.setCourseCode(course.getCourseCode());
        event.setCourse(course);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("course-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static CourseEvent createCourseUpdatedEvent(Course course) {
        CourseEvent event = new CourseEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("CourseUpdated");
        event.setCourseCode(course.getCourseCode());
        event.setCourse(course);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("course-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static CourseEvent createCourseDeletedEvent(String courseCode) {
        CourseEvent event = new CourseEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("CourseDeleted");
        event.setCourseCode(courseCode);
        event.setCourse(null);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("course-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static CourseEvent createCourseCapacityUpdatedEvent(Course course) {
        CourseEvent event = new CourseEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("CourseCapacityUpdated");
        event.setCourseCode(course.getCourseCode());
        event.setCourse(course);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("course-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
}
