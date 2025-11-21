package com.kma.studentsystem.identity_service.event;

import com.kma.studentsystem.identity_service.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityEvent {
    
    private String eventId;
    private String eventType;
    private String userId;
    private String username;
    private User user;
    private LocalDateTime timestamp;
    private String source;
    private String correlationId;
    private String ipAddress;
    private String userAgent;
    
    public static IdentityEvent createUserCreatedEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserCreated");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createUserUpdatedEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserUpdated");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createUserDeletedEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserDeleted");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createUserLoginEvent(User user, String ipAddress, String userAgent) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserLogin");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        return event;
    }
    
    public static IdentityEvent createUserLogoutEvent(User user, String ipAddress, String userAgent) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserLogout");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        return event;
    }
    
    public static IdentityEvent createUserLockedEvent(User user, String reason) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserLocked");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createUserUnlockedEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("UserUnlocked");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createPasswordChangedEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("PasswordChanged");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createEmailVerifiedEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("EmailVerified");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createRoleChangedEvent(User user, User.UserRole oldRole, User.UserRole newRole) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("RoleChanged");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createTwoFactorEnabledEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("TwoFactorEnabled");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
    
    public static IdentityEvent createTwoFactorDisabledEvent(User user) {
        IdentityEvent event = new IdentityEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType("TwoFactorDisabled");
        event.setUserId(user.getId().toString());
        event.setUsername(user.getUsername());
        event.setUser(user);
        event.setTimestamp(LocalDateTime.now());
        event.setSource("identity-service");
        event.setCorrelationId(java.util.UUID.randomUUID().toString());
        return event;
    }
}
