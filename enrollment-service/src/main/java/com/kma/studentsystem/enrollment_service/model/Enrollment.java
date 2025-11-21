package com.kma.studentsystem.enrollment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    @Column(name = "student_id", nullable = false)
    private String studentId;
    
    @NotBlank(message = "Course code is required")
    @Column(name = "course_code", nullable = false)
    private String courseCode;
    
    @NotNull(message = "Academic year is required")
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;
    
    @NotBlank(message = "Semester is required")
    @Size(max = 20, message = "Semester must not exceed 20 characters")
    @Column(name = "semester", nullable = false)
    private String semester;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;
    
    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;
    
    @Column(name = "completion_date")
    private LocalDateTime completionDate;
    
    @Column(name = "withdrawal_date")
    private LocalDateTime withdrawalDate;
    
    @Column(name = "final_grade")
    private Double finalGrade;
    
    @Column(name = "letter_grade")
    @Size(max = 5, message = "Letter grade must not exceed 5 characters")
    private String letterGrade;
    
    @Column(name = "credits_earned")
    private Integer creditsEarned;
    
    @Column(name = "gpa_points")
    private Double gpaPoints;
    
    @Column(name = "instructor_name")
    @Size(max = 100, message = "Instructor name must not exceed 100 characters")
    private String instructorName;
    
    @Column(name = "instructor_id")
    private String instructorId;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_audit")
    private Boolean isAudit = false;
    
    @Column(name = "is_pass_fail")
    private Boolean isPassFail = false;
    
    @Column(name = "prerequisite_met")
    private Boolean prerequisiteMet = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EnrollmentStatus {
        PENDING, ENROLLED, COMPLETED, WITHDRAWN, FAILED, AUDIT
    }
    
    // Helper methods
    public boolean isActive() {
        return status == EnrollmentStatus.ENROLLED;
    }
    
    public boolean isCompleted() {
        return status == EnrollmentStatus.COMPLETED;
    }
    
    public boolean isWithdrawn() {
        return status == EnrollmentStatus.WITHDRAWN;
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
