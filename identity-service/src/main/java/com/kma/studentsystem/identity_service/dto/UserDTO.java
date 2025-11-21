package com.kma.studentsystem.identity_service.dto;

import com.kma.studentsystem.identity_service.model.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    private User.UserRole role;
    
    private User.UserStatus status;
    
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    private String address;
    
    private LocalDateTime dateOfBirth;
    
    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;
    
    private LocalDateTime lastLoginAt;
    
    @Size(max = 45, message = "Last login IP must not exceed 45 characters")
    private String lastLoginIp;
    
    private Integer failedLoginAttempts;
    
    private LocalDateTime lockedUntil;
    
    private LocalDateTime passwordChangedAt;
    
    private Boolean emailVerified;
    
    private String emailVerificationToken;
    
    private String passwordResetToken;
    
    private LocalDateTime passwordResetExpiresAt;
    
    private Boolean twoFactorEnabled;
    
    private String twoFactorSecret;
    
    @Size(max = 10, message = "Preferred language must not exceed 10 characters")
    private String preferredLanguage;
    
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
    
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }
    
    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled != null && twoFactorEnabled;
    }
    
    public boolean hasRole(User.UserRole requiredRole) {
        return this.role == requiredRole;
    }
    
    public boolean hasAnyRole(User.UserRole... roles) {
        for (User.UserRole role : roles) {
            if (this.role == role) {
                return true;
            }
        }
        return false;
    }
    
    public String getStatusDescription() {
        switch (status) {
            case ACTIVE: return "Hoạt động";
            case INACTIVE: return "Không hoạt động";
            case SUSPENDED: return "Tạm khóa";
            case PENDING_VERIFICATION: return "Chờ xác thực";
            case LOCKED: return "Bị khóa";
            default: return "Không xác định";
        }
    }
    
    public String getRoleDescription() {
        switch (role) {
            case STUDENT: return "Sinh viên";
            case INSTRUCTOR: return "Giảng viên";
            case ADMIN: return "Quản trị viên";
            case SUPER_ADMIN: return "Siêu quản trị viên";
            default: return "Không xác định";
        }
    }
}
