package com.kma.studentsystem.grade_service.dto;

import com.kma.studentsystem.grade_service.model.Grade;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {
    
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Course code is required")
    private String courseCode;
    
    @NotNull(message = "Grade value is required")
    @DecimalMin(value = "0.0", message = "Grade must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Grade must be at most 10.0")
    private BigDecimal gradeValue;
    
    @NotNull(message = "Grade type is required")
    private Grade.GradeType gradeType;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;
    
    @NotNull(message = "Academic year is required")
    private Integer academicYear;
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;
    
    private Grade.GradeStatus status;
    
    @Size(max = 100, message = "Instructor name must not exceed 100 characters")
    private String instructorName;
    
    private String instructorId;
    
    private LocalDateTime gradedAt;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime submittedAt;
    
    private String comments;
    
    private Boolean isFinalGrade;
    
    @DecimalMin(value = "0.0", message = "Weight must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Weight must be at most 1.0")
    private BigDecimal weight;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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
        return status == Grade.GradeStatus.GRADED || status == Grade.GradeStatus.APPROVED;
    }
    
    public boolean isOverdue() {
        return dueDate != null && LocalDateTime.now().isAfter(dueDate) && !isGraded();
    }
}
