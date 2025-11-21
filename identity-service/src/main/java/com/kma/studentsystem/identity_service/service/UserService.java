package com.kma.studentsystem.identity_service.service;

import com.kma.studentsystem.identity_service.dto.UserDTO;
import com.kma.studentsystem.identity_service.event.IdentityEvent;
import com.kma.studentsystem.identity_service.model.User;
import com.kma.studentsystem.identity_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final KafkaTemplate<String, IdentityEvent> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;
    
    private static final String IDENTITY_EVENTS_TOPIC = "identity-events";
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Creating new user: {}", userDTO.getUsername());
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDTO.getUsername());
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }
        
        // Convert DTO to Entity
        User user = convertToEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setStatus(User.UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        
        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserCreatedEvent(savedUser);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, savedUser.getUsername(), event);
        log.info("Published UserCreated event for user: {}", savedUser.getUsername());
        
        return convertToDTO(savedUser);
    }
    
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        // Check if username or email already exists (excluding current user)
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
            userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDTO.getUsername());
        }
        
        if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDTO.getEmail());
        }
        
        // Update fields
        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setRole(userDTO.getRole());
        existingUser.setStatus(userDTO.getStatus());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setAddress(userDTO.getAddress());
        existingUser.setDateOfBirth(userDTO.getDateOfBirth());
        existingUser.setProfilePictureUrl(userDTO.getProfilePictureUrl());
        existingUser.setPreferredLanguage(userDTO.getPreferredLanguage());
        existingUser.setTimezone(userDTO.getTimezone());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserUpdatedEvent(updatedUser);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, updatedUser.getUsername(), event);
        log.info("Published UserUpdated event for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    public UserDTO changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        
        User updatedUser = userRepository.save(user);
        log.info("Password changed successfully for user: {}", updatedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createPasswordChangedEvent(updatedUser);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, updatedUser.getUsername(), event);
        log.info("Published PasswordChanged event for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    public UserDTO resetPassword(String email) {
        log.info("Resetting password for user with email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
        
        userRepository.save(user);
        log.info("Password reset token generated for user: {}", user.getId());
        
        // TODO: Send reset email with token
        
        return convertToDTO(user);
    }
    
    public UserDTO confirmPasswordReset(String token, String newPassword) {
        log.info("Confirming password reset with token: {}", token);
        
        User user = userRepository.findByPasswordResetToken(token, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);
        
        User updatedUser = userRepository.save(user);
        log.info("Password reset confirmed for user: {}", updatedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createPasswordChangedEvent(updatedUser);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, updatedUser.getUsername(), event);
        log.info("Published PasswordChanged event for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    public UserDTO verifyEmail(String token) {
        log.info("Verifying email with token: {}", token);
        
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
        
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setStatus(User.UserStatus.ACTIVE);
        
        User updatedUser = userRepository.save(user);
        log.info("Email verified successfully for user: {}", updatedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createEmailVerifiedEvent(updatedUser);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, updatedUser.getUsername(), event);
        log.info("Published EmailVerified event for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    public UserDTO lockUser(Long id, String reason) {
        log.info("Locking user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        user.setStatus(User.UserStatus.LOCKED);
        user.setLockedUntil(LocalDateTime.now().plusHours(24));
        
        User lockedUser = userRepository.save(user);
        log.info("User locked successfully: {}", lockedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserLockedEvent(lockedUser, reason);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, lockedUser.getUsername(), event);
        log.info("Published UserLocked event for user: {}", lockedUser.getUsername());
        
        return convertToDTO(lockedUser);
    }
    
    public UserDTO unlockUser(Long id) {
        log.info("Unlocking user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        user.setStatus(User.UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        
        User unlockedUser = userRepository.save(user);
        log.info("User unlocked successfully: {}", unlockedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserUnlockedEvent(unlockedUser);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, unlockedUser.getUsername(), event);
        log.info("Published UserUnlocked event for user: {}", unlockedUser.getUsername());
        
        return convertToDTO(unlockedUser);
    }
    
    public UserDTO changeRole(Long id, User.UserRole newRole) {
        log.info("Changing role for user with ID: {} to {}", id, newRole);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        User.UserRole oldRole = user.getRole();
        user.setRole(newRole);
        
        User updatedUser = userRepository.save(user);
        log.info("Role changed successfully for user: {}", updatedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createRoleChangedEvent(updatedUser, oldRole, newRole);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, updatedUser.getUsername(), event);
        log.info("Published RoleChanged event for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        
        String username = user.getUsername();
        userRepository.delete(user);
        log.info("User deleted successfully: {}", id);
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserDeletedEvent(user);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, username, event);
        log.info("Published UserDeleted event for user: {}", username);
    }
    
    public UserDTO login(String usernameOrEmail, String password, String ipAddress, String userAgent) {
        log.info("User login attempt: {}", usernameOrEmail);
        
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("Invalid username or email"));
        
        // Check if user is locked
        if (user.isLocked()) {
            throw new RuntimeException("Account is locked");
        }
        
        // Check if user is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Increment failed login attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            // Lock account if max attempts reached
            if (user.getFailedLoginAttempts() >= MAX_FAILED_LOGIN_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
                user.setStatus(User.UserStatus.LOCKED);
                
                IdentityEvent event = IdentityEvent.createUserLockedEvent(user, "Too many failed login attempts");
                kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, user.getUsername(), event);
            }
            
            userRepository.save(user);
            throw new RuntimeException("Invalid password");
        }
        
        // Reset failed login attempts and update last login
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        
        User updatedUser = userRepository.save(user);
        log.info("User logged in successfully: {}", updatedUser.getId());
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserLoginEvent(updatedUser, ipAddress, userAgent);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, updatedUser.getUsername(), event);
        log.info("Published UserLogin event for user: {}", updatedUser.getUsername());
        
        return convertToDTO(updatedUser);
    }
    
    public void logout(String username, String ipAddress, String userAgent) {
        log.info("User logout: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Publish event
        IdentityEvent event = IdentityEvent.createUserLogoutEvent(user, ipAddress, userAgent);
        kafkaTemplate.send(IDENTITY_EVENTS_TOPIC, user.getUsername(), event);
        log.info("Published UserLogout event for user: {}", user.getUsername());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Retrieving all users");
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        log.info("Retrieving user with ID: {}", id);
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        log.info("Retrieving user with username: {}", username);
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        log.info("Retrieving user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(User.UserRole role) {
        log.info("Retrieving users with role: {}", role);
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByStatus(User.UserStatus status) {
        log.info("Retrieving users with status: {}", status);
        return userRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long countUsersByRole(User.UserRole role) {
        log.info("Counting users with role: {}", role);
        return userRepository.countByRole(role);
    }
    
    @Transactional(readOnly = true)
    public Long countUsersByStatus(User.UserStatus status) {
        log.info("Counting users with status: {}", status);
        return userRepository.countByStatus(status);
    }
    
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user.setLastLoginAt(dto.getLastLoginAt());
        user.setLastLoginIp(dto.getLastLoginIp());
        user.setFailedLoginAttempts(dto.getFailedLoginAttempts());
        user.setLockedUntil(dto.getLockedUntil());
        user.setPasswordChangedAt(dto.getPasswordChangedAt());
        user.setEmailVerified(dto.getEmailVerified());
        user.setEmailVerificationToken(dto.getEmailVerificationToken());
        user.setPasswordResetToken(dto.getPasswordResetToken());
        user.setPasswordResetExpiresAt(dto.getPasswordResetExpiresAt());
        user.setTwoFactorEnabled(dto.getTwoFactorEnabled());
        user.setTwoFactorSecret(dto.getTwoFactorSecret());
        user.setPreferredLanguage(dto.getPreferredLanguage());
        user.setTimezone(dto.getTimezone());
        return user;
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(null); // Never return password
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLockedUntil(user.getLockedUntil());
        dto.setPasswordChangedAt(user.getPasswordChangedAt());
        dto.setEmailVerified(user.getEmailVerified());
        dto.setEmailVerificationToken(user.getEmailVerificationToken());
        dto.setPasswordResetToken(user.getPasswordResetToken());
        dto.setPasswordResetExpiresAt(user.getPasswordResetExpiresAt());
        dto.setTwoFactorEnabled(user.getTwoFactorEnabled());
        dto.setTwoFactorSecret(user.getTwoFactorSecret());
        dto.setPreferredLanguage(user.getPreferredLanguage());
        dto.setTimezone(user.getTimezone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
