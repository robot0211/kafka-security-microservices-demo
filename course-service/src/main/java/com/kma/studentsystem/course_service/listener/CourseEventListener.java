package com.kma.studentsystem.course_service.listener;

import com.kma.studentsystem.course_service.event.CourseEvent;
import com.kma.studentsystem.course_service.service.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CourseEventListener {

    private static final Logger logger = LoggerFactory.getLogger(CourseEventListener.class);
    private final CourseService courseService;

    public CourseEventListener(CourseService courseService) {
        this.courseService = courseService;
    }

    @KafkaListener(topics = "course-events", groupId = "course-service-group")
    public void handleCourseEvent(CourseEvent event) {
        logger.info("Received course event: {}", event);
        
        try {
            switch (event.getEventType()) {
                case "CourseCreated":
                    logger.info("Course created: {}", event.getCourseCode());
                    break;
                case "CourseUpdated":
                    logger.info("Course updated: {}", event.getCourseCode());
                    break;
                case "CourseDeleted":
                    logger.info("Course deleted: {}", event.getCourseCode());
                    break;
                case "CourseCapacityUpdated":
                    logger.info("Course capacity changed: {}", event.getCourseCode());
                    break;
                default:
                    logger.warn("Unknown course event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing course event: {}", event, e);
        }
    }
}
