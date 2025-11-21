package com.kma.studentsystem.notification_service.event;

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
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
}


