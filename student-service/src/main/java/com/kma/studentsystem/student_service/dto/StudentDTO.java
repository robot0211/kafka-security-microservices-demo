package com.kma.studentsystem.student_service.dto;

import com.kma.studentsystem.student_service.model.Student;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    @Email(message = "Email should be valid")
    private String email;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number should be 10-11 digits")
    private String phoneNumber;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;
    
    @NotNull(message = "Gender is required")
    private Student.Gender gender;
    
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    @NotBlank(message = "Major is required")
    @Size(max = 100, message = "Major must not exceed 100 characters")
    private String major;
    
    private Student.StudentStatus status;
    
    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "4.0", message = "GPA must be at most 4.0")
    private Double gpa;
    
    @NotNull(message = "Enrollment year is required")
    @Min(value = 2000, message = "Enrollment year must be at least 2000")
    @Max(value = 2030, message = "Enrollment year must be at most 2030")
    private Integer enrollmentYear;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public Integer getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
    
    public boolean isActive() {
        return status == Student.StudentStatus.ACTIVE;
    }
}
