package com.kma.studentsystem.grade_service.listener;

import com.kma.studentsystem.grade_service.event.GradeEvent;
import com.kma.studentsystem.grade_service.service.GradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GradeEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GradeEventListener.class);
    private final GradeService gradeService;

    public GradeEventListener(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @KafkaListener(topics = "grade-events", groupId = "grade-service-group")
    public void handleGradeEvent(GradeEvent event) {
        logger.info("Received grade event: {}", event);
        
        try {
            switch (event.getEventType()) {
                case "GradeAssigned":
                    logger.info("Grade assigned for student: {} in course: {}", event.getStudentId(), event.getCourseCode());
                    break;
                case "GradeUpdated":
                    logger.info("Grade updated for student: {} in course: {}", event.getStudentId(), event.getCourseCode());
                    break;
                case "GradeDeleted":
                    logger.info("Grade deleted for student: {} in course: {}", event.getStudentId(), event.getCourseCode());
                    break;
                case "GradeFinalized":
                    logger.info("Grade finalized for student: {} in course: {}", event.getStudentId(), event.getCourseCode());
                    break;
                default:
                    logger.warn("Unknown grade event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            logger.error("Error processing grade event: {}", event, e);
        }
    }
}
