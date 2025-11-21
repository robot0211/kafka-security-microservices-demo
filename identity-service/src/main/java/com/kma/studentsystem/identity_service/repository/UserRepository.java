package com.kma.studentsystem.identity_service.repository;

import com.kma.studentsystem.identity_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    List<User> findByRole(User.UserRole role);
    
    List<User> findByStatus(User.UserStatus status);
    
    List<User> findByRoleAndStatus(User.UserRole role, User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = true")
    List<User> findVerifiedUsers();
    
    @Query("SELECT u FROM User u WHERE u.emailVerified = false")
    List<User> findUnverifiedUsers();
    
    @Query("SELECT u FROM User u WHERE u.twoFactorEnabled = true")
    List<User> findUsersWithTwoFactorEnabled();
    
    @Query("SELECT u FROM User u WHERE u.lockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :maxAttempts")
    List<User> findUsersWithFailedLoginAttempts(@Param("maxAttempts") Integer maxAttempts);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since")
    List<User> findUsersLoggedInSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :before")
    List<User> findUsersNotLoggedInSince(@Param("before") LocalDateTime before);
    
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since")
    List<User> findUsersCreatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.passwordChangedAt < :before")
    List<User> findUsersWithOldPasswords(@Param("before") LocalDateTime before);
    
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    Optional<User> findByEmailVerificationToken(@Param("token") String token);
    
    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetExpiresAt > :now")
    Optional<User> findByPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> findByEmailContaining(@Param("email") String email);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") User.UserRole role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    Long countByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    Long countVerifiedUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.twoFactorEnabled = true")
    Long countUsersWithTwoFactorEnabled();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLoginAt >= :since")
    Long countActiveUsers(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC")
    List<User> findByRoleOrderByCreatedAtDesc(@Param("role") User.UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC")
    List<User> findByStatusOrderByCreatedAtDesc(@Param("status") User.UserStatus status);
    
    @Query("SELECT u FROM User u ORDER BY u.lastLoginAt DESC")
    List<User> findAllOrderByLastLoginAtDesc();
    
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllOrderByCreatedAtDesc();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status ORDER BY u.createdAt DESC")
    List<User> findByRoleAndStatusOrderByCreatedAtDesc(@Param("role") User.UserRole role, @Param("status") User.UserStatus status);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsernameOrEmail(String username, String email);
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoleIn(@Param("roles") List<User.UserRole> roles);
    
    @Query("SELECT u FROM User u WHERE u.status IN :statuses")
    List<User> findByStatusIn(@Param("statuses") List<User.UserStatus> statuses);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.emailVerified = :verified")
    List<User> findByRoleAndEmailVerified(@Param("role") User.UserRole role, @Param("verified") Boolean verified);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.twoFactorEnabled = :enabled")
    List<User> findByRoleAndTwoFactorEnabled(@Param("role") User.UserRole role, @Param("enabled") Boolean enabled);
}
