package com.kma.studentsystem.identity_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", unique = true, nullable = false)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "phone_number")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;
    
    @Column(name = "profile_picture_url")
    @Size(max = 500, message = "Profile picture URL must not exceed 500 characters")
    private String profilePictureUrl;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip")
    @Size(max = 45, message = "Last login IP must not exceed 45 characters")
    private String lastLoginIp;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "email_verified")
    private Boolean emailVerified = false;
    
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;
    
    @Column(name = "preferred_language")
    @Size(max = 10, message = "Preferred language must not exceed 10 characters")
    private String preferredLanguage = "vi";
    
    @Column(name = "timezone")
    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone = "Asia/Ho_Chi_Minh";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum UserRole {
        STUDENT, INSTRUCTOR, ADMIN, SUPER_ADMIN
    }
    
    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION, LOCKED
    }
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
    
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
    
    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }
    
    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole role : roles) {
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
