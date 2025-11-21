package com.kma.studentsystem.course_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Course code is required")
    @Column(name = "course_code", unique = true, nullable = false)
    private String courseCode;
    
    @NotBlank(message = "Course name is required")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    @Column(name = "course_name", nullable = false)
    private String courseName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits must be at most 10")
    @Column(name = "credits", nullable = false)
    private Integer credits;
    
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Column(name = "department", nullable = false)
    private String department;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private CourseLevel level;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CourseStatus status = CourseStatus.ACTIVE;
    
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Column(name = "enrolled_count")
    private Integer enrolledCount = 0;
    
    @NotNull(message = "Academic year is required")
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    @Column(name = "semester", nullable = false)
    private String semester;
    
    @Column(name = "prerequisites", columnDefinition = "TEXT")
    private String prerequisites;
    
    @Column(name = "instructor_name")
    @Size(max = 100, message = "Instructor name must not exceed 100 characters")
    private String instructorName;
    
    @Column(name = "instructor_email")
    @Email(message = "Instructor email should be valid")
    private String instructorEmail;
    
    @Column(name = "schedule")
    @Size(max = 200, message = "Schedule must not exceed 200 characters")
    private String schedule;
    
    @Column(name = "location")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum CourseLevel {
        UNDERGRADUATE, GRADUATE, DOCTORATE
    }
    
    public enum CourseStatus {
        ACTIVE, INACTIVE, CANCELLED, COMPLETED
    }
    
    // Helper methods
    public boolean isAvailable() {
        return status == CourseStatus.ACTIVE && enrolledCount < capacity;
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
