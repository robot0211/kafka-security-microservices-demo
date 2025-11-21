package com.kma.studentsystem.student_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    @Column(name = "student_id", unique = true, nullable = false)
    private String studentId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Email(message = "Email should be valid")
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number should be 10-11 digits")
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @NotNull(message = "Date of birth is required")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;
    
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", nullable = false)
    private String address;
    
    @NotBlank(message = "Major is required")
    @Size(max = 100, message = "Major must not exceed 100 characters")
    @Column(name = "major", nullable = false)
    private String major;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StudentStatus status = StudentStatus.ACTIVE;
    
    @Column(name = "gpa")
    private Double gpa;
    
    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    public enum StudentStatus {
        ACTIVE, INACTIVE, GRADUATED, SUSPENDED, DROPPED
    }
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public Integer getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }
}
