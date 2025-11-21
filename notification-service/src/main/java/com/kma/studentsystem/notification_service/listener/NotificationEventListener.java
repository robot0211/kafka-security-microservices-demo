package com.kma.studentsystem.notification_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kma.studentsystem.notification_service.dto.NotificationDTO;
import com.kma.studentsystem.notification_service.event.StudentEvent;
import com.kma.studentsystem.notification_service.model.Notification;
import com.kma.studentsystem.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @KafkaListener(topics = "student-events", groupId = "notification-service-group", containerFactory = "studentEventKafkaListenerContainerFactory")
    public void handleStudentEvents(@Payload StudentEvent event,
                                   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                   @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                   @Header(KafkaHeaders.OFFSET) long offset,
                                   Acknowledgment acknowledgment) {
        try {
            log.info("Received student event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            log.info("Student event type: {}, studentId: {}", event.getEventType(), event.getStudentId());

            NotificationDTO notificationDTO = null;
            
            switch (event.getEventType()) {
                case "StudentCreated":
                    notificationDTO = createWelcomeNotification(event.getStudentId(), event.getEmail(), 
                        "Chào mừng bạn đến với hệ thống!", 
                        String.format("Xin chào %s %s! Tài khoản của bạn đã được tạo thành công.", 
                            event.getFirstName(), event.getLastName()),
                        event.getCorrelationId());
                    break;
                case "StudentUpdated":
                    notificationDTO = createNotification(event.getStudentId(), event.getEmail(),
                        "Thông tin tài khoản đã được cập nhật",
                        "Thông tin tài khoản của bạn đã được cập nhật thành công.",
                        event.getCorrelationId());
                    break;
                case "StudentDeleted":
                    notificationDTO = createNotification(event.getStudentId(), event.getEmail(),
                        "Tài khoản đã bị xóa",
                        "Tài khoản của bạn đã bị xóa khỏi hệ thống.",
                        event.getCorrelationId());
                    break;
                default:
                    log.warn("Unknown student event type: {}", event.getEventType());
            }
            
            if (notificationDTO != null) {
                notificationService.createNotification(notificationDTO);
                log.info("Created notification for student event: {}", event.getEventType());
            }
            
            acknowledgment.acknowledge();
            log.info("Successfully processed student event");
        } catch (Exception e) {
            log.error("Error processing student event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "course-events", groupId = "notification-service-group", 
                   containerFactory = "genericEventKafkaListenerContainerFactory")
    public void handleCourseEvents(@Payload Object payload,
                                  @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment acknowledgment) {
        try {
            log.info("Received course event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            
            Map<String, Object> event = convertToMap(payload);
            String eventType = (String) event.getOrDefault("eventType", "Unknown");
            String courseId = (String) event.getOrDefault("courseId", "");
            String courseName = (String) event.getOrDefault("courseName", "Khóa học");
            String correlationId = (String) event.getOrDefault("correlationId", "");
            
            log.info("Course event type: {}, courseId: {}", eventType, courseId);
            
            // Extract student IDs if available (for course-related notifications)
            String recipientId = (String) event.getOrDefault("studentId", null);
            if (recipientId == null) {
                recipientId = (String) event.getOrDefault("userId", null);
            }
            
            if (recipientId != null) {
                NotificationDTO notificationDTO = null;
                
                switch (eventType) {
                    case "CourseCreated":
                        notificationDTO = createNotification(recipientId, null,
                            "Khóa học mới: " + courseName,
                            String.format("Khóa học '%s' đã được tạo và sẵn sàng để đăng ký.", courseName),
                            correlationId);
                        break;
                    case "CourseUpdated":
                        notificationDTO = createNotification(recipientId, null,
                            "Cập nhật khóa học: " + courseName,
                            String.format("Thông tin khóa học '%s' đã được cập nhật.", courseName),
                            correlationId);
                        break;
                    case "CourseDeleted":
                        notificationDTO = createNotification(recipientId, null,
                            "Khóa học đã bị xóa: " + courseName,
                            String.format("Khóa học '%s' đã bị xóa khỏi hệ thống.", courseName),
                            correlationId);
                        break;
                    default:
                        log.debug("Course event type {} - no notification needed", eventType);
                }
                
                if (notificationDTO != null) {
                    notificationService.createNotification(notificationDTO);
                    log.info("Created notification for course event: {}", eventType);
                }
            }
            
            acknowledgment.acknowledge();
            log.info("Successfully processed course event");
        } catch (Exception e) {
            log.error("Error processing course event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "grade-events", groupId = "notification-service-group",
                   containerFactory = "genericEventKafkaListenerContainerFactory")
    public void handleGradeEvents(@Payload Object payload,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset,
                                 Acknowledgment acknowledgment) {
        try {
            log.info("Received grade event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            
            Map<String, Object> event = convertToMap(payload);
            String eventType = (String) event.getOrDefault("eventType", "Unknown");
            String studentId = (String) event.getOrDefault("studentId", "");
            String courseId = (String) event.getOrDefault("courseId", "");
            String courseName = (String) event.getOrDefault("courseName", "khóa học");
            Object gradeValue = event.getOrDefault("grade", "");
            String correlationId = (String) event.getOrDefault("correlationId", "");
            
            log.info("Grade event type: {}, studentId: {}, grade: {}", eventType, studentId, gradeValue);
            
            if (studentId != null && !studentId.isEmpty()) {
                NotificationDTO notificationDTO = null;
                
                switch (eventType) {
                    case "GradePublished":
                        notificationDTO = createNotification(studentId, null,
                            "Điểm số đã được công bố",
                            String.format("Điểm số của bạn cho khóa học '%s' đã được công bố: %s", 
                                courseName, gradeValue),
                            correlationId, Notification.Priority.HIGH);
                        break;
                    case "GradeUpdated":
                        notificationDTO = createNotification(studentId, null,
                            "Điểm số đã được cập nhật",
                            String.format("Điểm số của bạn cho khóa học '%s' đã được cập nhật: %s", 
                                courseName, gradeValue),
                            correlationId, Notification.Priority.HIGH);
                        break;
                    default:
                        log.debug("Grade event type {} - no notification needed", eventType);
                }
                
                if (notificationDTO != null) {
                    notificationService.createNotification(notificationDTO);
                    log.info("Created notification for grade event: {}", eventType);
                }
            }
            
            acknowledgment.acknowledge();
            log.info("Successfully processed grade event");
        } catch (Exception e) {
            log.error("Error processing grade event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "enrollment-events", groupId = "notification-service-group",
                   containerFactory = "genericEventKafkaListenerContainerFactory")
    public void handleEnrollmentEvents(@Payload Object payload,
                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                      @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                      @Header(KafkaHeaders.OFFSET) long offset,
                                      Acknowledgment acknowledgment) {
        try {
            log.info("Received enrollment event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            
            Map<String, Object> event = convertToMap(payload);
            String eventType = (String) event.getOrDefault("eventType", "Unknown");
            String studentId = (String) event.getOrDefault("studentId", "");
            String courseId = (String) event.getOrDefault("courseId", "");
            String courseName = (String) event.getOrDefault("courseName", "khóa học");
            String correlationId = (String) event.getOrDefault("correlationId", "");
            
            log.info("Enrollment event type: {}, studentId: {}, courseId: {}", eventType, studentId, courseId);
            
            if (studentId != null && !studentId.isEmpty()) {
                NotificationDTO notificationDTO = null;
                
                switch (eventType) {
                    case "EnrollmentCreated":
                        notificationDTO = createNotification(studentId, null,
                            "Đăng ký khóa học mới",
                            String.format("Bạn đã đăng ký thành công khóa học '%s'. Vui lòng chờ xét duyệt.", courseName),
                            correlationId);
                        break;
                    case "EnrollmentApproved":
                        notificationDTO = createNotification(studentId, null,
                            "Đăng ký khóa học đã được duyệt",
                            String.format("Đăng ký khóa học '%s' của bạn đã được duyệt thành công!", courseName),
                            correlationId, Notification.Priority.HIGH);
                        break;
                    case "EnrollmentRejected":
                        notificationDTO = createNotification(studentId, null,
                            "Đăng ký khóa học bị từ chối",
                            String.format("Đăng ký khóa học '%s' của bạn đã bị từ chối.", courseName),
                            correlationId, Notification.Priority.HIGH);
                        break;
                    case "EnrollmentCompleted":
                        notificationDTO = createNotification(studentId, null,
                            "Hoàn thành khóa học",
                            String.format("Chúc mừng! Bạn đã hoàn thành khóa học '%s'.", courseName),
                            correlationId, Notification.Priority.HIGH);
                        break;
                    default:
                        log.debug("Enrollment event type {} - no notification needed", eventType);
                }
                
                if (notificationDTO != null) {
                    notificationService.createNotification(notificationDTO);
                    log.info("Created notification for enrollment event: {}", eventType);
                }
            }
            
            acknowledgment.acknowledge();
            log.info("Successfully processed enrollment event");
        } catch (Exception e) {
            log.error("Error processing enrollment event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    @KafkaListener(topics = "identity-events", groupId = "notification-service-group",
                   containerFactory = "genericEventKafkaListenerContainerFactory")
    public void handleIdentityEvents(@Payload Object payload,
                                    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                    @Header(KafkaHeaders.OFFSET) long offset,
                                    Acknowledgment acknowledgment) {
        try {
            log.info("Received identity event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
            
            Map<String, Object> event = convertToMap(payload);
            String eventType = (String) event.getOrDefault("eventType", "Unknown");
            String userId = (String) event.getOrDefault("userId", "");
            String email = (String) event.getOrDefault("email", "");
            String correlationId = (String) event.getOrDefault("correlationId", "");
            
            log.info("Identity event type: {}, userId: {}", eventType, userId);
            
            if (userId != null && !userId.isEmpty()) {
                NotificationDTO notificationDTO = null;
                
                switch (eventType) {
                    case "PasswordResetRequested":
                        notificationDTO = createNotification(userId, email,
                            "Yêu cầu đặt lại mật khẩu",
                            "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng kiểm tra email để xác nhận.",
                            correlationId, Notification.Priority.HIGH);
                        break;
                    case "PasswordResetCompleted":
                        notificationDTO = createNotification(userId, email,
                            "Mật khẩu đã được đặt lại",
                            "Mật khẩu của bạn đã được đặt lại thành công. Nếu không phải bạn, vui lòng liên hệ ngay.",
                            correlationId, Notification.Priority.HIGH);
                        break;
                    case "AccountLocked":
                        notificationDTO = createNotification(userId, email,
                            "Tài khoản đã bị khóa",
                            "Tài khoản của bạn đã bị khóa do nhiều lần đăng nhập sai. Vui lòng liên hệ quản trị viên.",
                            correlationId, Notification.Priority.URGENT);
                        break;
                    case "SuspiciousLogin":
                        notificationDTO = createNotification(userId, email,
                            "Cảnh báo đăng nhập đáng ngờ",
                            "Phát hiện đăng nhập đáng ngờ vào tài khoản của bạn. Nếu không phải bạn, vui lòng đổi mật khẩu ngay.",
                            correlationId, Notification.Priority.URGENT);
                        break;
                    default:
                        log.debug("Identity event type {} - no notification needed", eventType);
                }
                
                if (notificationDTO != null) {
                    notificationService.createNotification(notificationDTO);
                    log.info("Created notification for identity event: {}", eventType);
                }
            }
            
            acknowledgment.acknowledge();
            log.info("Successfully processed identity event");
        } catch (Exception e) {
            log.error("Error processing identity event: {}", e.getMessage(), e);
            // Don't acknowledge to retry
        }
    }
    
    // Helper methods
    private Map<String, Object> convertToMap(Object payload) {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        }
        try {
            return objectMapper.convertValue(payload, Map.class);
        } catch (Exception e) {
            log.error("Error converting payload to Map: {}", e.getMessage());
            return Map.of();
        }
    }
    
    private NotificationDTO createWelcomeNotification(String recipientId, String email, String title, String message, String correlationId) {
        return createNotification(recipientId, email, title, message, correlationId, Notification.Priority.MEDIUM);
    }
    
    private NotificationDTO createNotification(String recipientId, String email, String title, String message, String correlationId) {
        return createNotification(recipientId, email, title, message, correlationId, Notification.Priority.MEDIUM);
    }
    
    private NotificationDTO createNotification(String recipientId, String email, String title, String message, 
                                              String correlationId, Notification.Priority priority) {
        NotificationDTO dto = new NotificationDTO();
        dto.setRecipientId(recipientId);
        dto.setRecipientType(Notification.RecipientType.STUDENT);
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setType(Notification.NotificationType.GENERAL);
        dto.setPriority(priority);
        dto.setChannel(Notification.Channel.IN_APP);
        dto.setSourceService("notification-service");
        dto.setCorrelationId(correlationId);
        dto.setMaxDeliveryAttempts(3);
        return dto;
    }
}
