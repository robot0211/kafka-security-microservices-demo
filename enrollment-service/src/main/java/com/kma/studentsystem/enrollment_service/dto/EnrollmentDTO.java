package com.kma.studentsystem.enrollment_service.dto;

import com.kma.studentsystem.enrollment_service.model.Enrollment;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDTO {
    
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Course code is required")
    private String courseCode;
    
    @NotNull(message = "Academic year is required")
    private Integer academicYear;
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    private String semester;
    
    private Enrollment.EnrollmentStatus status;
    
    private LocalDateTime enrollmentDate;
    
    private LocalDateTime completionDate;
    
    private LocalDateTime withdrawalDate;
    
    @DecimalMin(value = "0.0", message = "Final grade must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Final grade must be at most 10.0")
    private Double finalGrade;
    
    @Size(max = 5, message = "Letter grade must not exceed 5 characters")
    private String letterGrade;
    
    private Integer creditsEarned;
    
    private Double gpaPoints;
    
    @Size(max = 100, message = "Instructor name must not exceed 100 characters")
    private String instructorName;
    
    private String instructorId;
    
    private String notes;
    
    private Boolean isAudit;
    
    private Boolean isPassFail;
    
    private Boolean prerequisiteMet;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isActive() {
        return status == Enrollment.EnrollmentStatus.ENROLLED;
    }
    
    public boolean isCompleted() {
        return status == Enrollment.EnrollmentStatus.COMPLETED;
    }
    
    public boolean isWithdrawn() {
        return status == Enrollment.EnrollmentStatus.WITHDRAWN;
    }
    
    public boolean isPassing() {
        return finalGrade != null && finalGrade >= 5.0;
    }
    
    public String getStatusDescription() {
        switch (status) {
            case PENDING: return "Chờ duyệt";
            case ENROLLED: return "Đã đăng ký";
            case COMPLETED: return "Hoàn thành";
            case WITHDRAWN: return "Đã rút";
            case FAILED: return "Không đạt";
            case AUDIT: return "Học thử";
            default: return "Không xác định";
        }
    }
}
