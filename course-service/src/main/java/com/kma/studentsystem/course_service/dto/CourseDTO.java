package com.kma.studentsystem.course_service.dto;

import com.kma.studentsystem.course_service.model.Course;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    
    private Long id;
    
    @NotBlank(message = "Course code is required")
    private String courseCode;
    
    @NotBlank(message = "Course name is required")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    private String courseName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits must be at most 10")
    private Integer credits;
    
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @NotNull(message = "Level is required")
    private Course.CourseLevel level;
    
    private Course.CourseStatus status;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    
    private Integer enrolledCount;
    
    @NotNull(message = "Academic year is required")
    private Integer academicYear;
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;
    
    private String prerequisites;
    
    @Size(max = 100, message = "Instructor name must not exceed 100 characters")
    private String instructorName;
    
    @Email(message = "Instructor email should be valid")
    private String instructorEmail;
    
    @Size(max = 200, message = "Schedule must not exceed 200 characters")
    private String schedule;
    
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isAvailable() {
        return status == Course.CourseStatus.ACTIVE && enrolledCount < capacity;
    }
    
    public boolean isFull() {
        return enrolledCount >= capacity;
    }
    
    public Integer getAvailableSlots() {
        return Math.max(0, capacity - enrolledCount);
    }
    
    public boolean canEnroll() {
        return isAvailable() && !isFull();
    }
}
