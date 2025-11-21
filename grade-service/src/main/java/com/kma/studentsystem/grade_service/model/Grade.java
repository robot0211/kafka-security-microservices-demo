package com.kma.studentsystem.grade_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    @Column(name = "student_id", nullable = false)
    private String studentId;
    
    @NotBlank(message = "Course code is required")
    @Column(name = "course_code", nullable = false)
    private String courseCode;
    
    @NotNull(message = "Grade value is required")
    @DecimalMin(value = "0.0", message = "Grade must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Grade must be at most 10.0")
    @Column(name = "grade_value", nullable = false, precision = 3, scale = 2)
    private BigDecimal gradeValue;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false)
    private GradeType gradeType;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Academic year is required")
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    @Column(name = "semester", nullable = false)
    private String semester;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GradeStatus status = GradeStatus.PENDING;
    
    @Column(name = "instructor_name")
    @Size(max = 100, message = "Instructor name must not exceed 100 characters")
    private String instructorName;
    
    @Column(name = "instructor_id")
    private String instructorId;
    
    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;
    
    @Column(name = "is_final_grade")
    private Boolean isFinalGrade = false;
    
    @Column(name = "weight")
    @DecimalMin(value = "0.0", message = "Weight must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Weight must be at most 1.0")
    private BigDecimal weight;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum GradeType {
        ASSIGNMENT, QUIZ, MIDTERM, FINAL, PROJECT, PARTICIPATION, HOMEWORK, LAB
    }
    
    public enum GradeStatus {
        PENDING, GRADED, APPROVED, REJECTED, APPEALED
    }
    
    // Helper methods
    public String getLetterGrade() {
        if (gradeValue == null) return "N/A";
        
        double value = gradeValue.doubleValue();
        if (value >= 9.0) return "A+";
        else if (value >= 8.5) return "A";
        else if (value >= 8.0) return "B+";
        else if (value >= 7.0) return "B";
        else if (value >= 6.5) return "C+";
        else if (value >= 5.5) return "C";
        else if (value >= 5.0) return "D+";
        else if (value >= 4.0) return "D";
        else return "F";
    }
    
    public boolean isPassing() {
        return gradeValue != null && gradeValue.doubleValue() >= 5.0;
    }
    
    public boolean isExcellent() {
        return gradeValue != null && gradeValue.doubleValue() >= 8.0;
    }
    
    public boolean isGraded() {
        return status == GradeStatus.GRADED || status == GradeStatus.APPROVED;
    }
    
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !isGraded();
    }
}
