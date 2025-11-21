package com.kma.studentsystem.identity_service.controller;

import com.kma.studentsystem.identity_service.dto.UserDTO;
import com.kma.studentsystem.identity_service.model.User;
import com.kma.studentsystem.identity_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.info("Creating new user: {}", userDTO.getUsername());
        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, 
                                            @Valid @RequestBody UserDTO userDTO) {
        log.info("Updating user with ID: {}", id);
        try {
            UserDTO updatedUser = userService.updateUser(id, userDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/change-password")
    public ResponseEntity<UserDTO> changePassword(@PathVariable Long id,
                                                @RequestParam String oldPassword,
                                                @RequestParam String newPassword) {
        log.info("Changing password for user with ID: {}", id);
        try {
            UserDTO updatedUser = userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error changing password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<UserDTO> resetPassword(@RequestParam String email) {
        log.info("Resetting password for user with email: {}", email);
        try {
            UserDTO user = userService.resetPassword(email);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/confirm-password-reset")
    public ResponseEntity<UserDTO> confirmPasswordReset(@RequestParam String token,
                                                      @RequestParam String newPassword) {
        log.info("Confirming password reset with token: {}", token);
        try {
            UserDTO updatedUser = userService.confirmPasswordReset(token, newPassword);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error confirming password reset: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<UserDTO> verifyEmail(@RequestParam String token) {
        log.info("Verifying email with token: {}", token);
        try {
            UserDTO updatedUser = userService.verifyEmail(token);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error verifying email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/lock")
    public ResponseEntity<UserDTO> lockUser(@PathVariable Long id, 
                                          @RequestParam String reason) {
        log.info("Locking user with ID: {}", id);
        try {
            UserDTO lockedUser = userService.lockUser(id, reason);
            return ResponseEntity.ok(lockedUser);
        } catch (Exception e) {
            log.error("Error locking user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/unlock")
    public ResponseEntity<UserDTO> unlockUser(@PathVariable Long id) {
        log.info("Unlocking user with ID: {}", id);
        try {
            UserDTO unlockedUser = userService.unlockUser(id);
            return ResponseEntity.ok(unlockedUser);
        } catch (Exception e) {
            log.error("Error unlocking user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/change-role")
    public ResponseEntity<UserDTO> changeRole(@PathVariable Long id, 
                                            @RequestParam User.UserRole newRole) {
        log.info("Changing role for user with ID: {} to {}", id, newRole);
        try {
            UserDTO updatedUser = userService.changeRole(id, newRole);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error changing role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestParam String usernameOrEmail,
                                       @RequestParam String password,
                                       @RequestParam(required = false) String ipAddress,
                                       @RequestParam(required = false) String userAgent) {
        log.info("User login attempt: {}", usernameOrEmail);
        try {
            UserDTO user = userService.login(usernameOrEmail, password, ipAddress, userAgent);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String username,
                                     @RequestParam(required = false) String ipAddress,
                                     @RequestParam(required = false) String userAgent) {
        log.info("User logout: {}", username);
        try {
            userService.logout(username, ipAddress, userAgent);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Retrieving all users");
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Retrieving user with ID: {}", id);
        try {
            Optional<UserDTO> user = userService.getUserById(id);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        log.info("Retrieving user with username: {}", username);
        try {
            Optional<UserDTO> user = userService.getUserByUsername(username);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving user by username: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("Retrieving user with email: {}", email);
        try {
            Optional<UserDTO> user = userService.getUserByEmail(email);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving user by email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable User.UserRole role) {
        log.info("Retrieving users with role: {}", role);
        try {
            List<UserDTO> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users by role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserDTO>> getUsersByStatus(@PathVariable User.UserStatus status) {
        log.info("Retrieving users with status: {}", status);
        try {
            List<UserDTO> users = userService.getUsersByStatus(status);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/role/{role}/count")
    public ResponseEntity<Long> countUsersByRole(@PathVariable User.UserRole role) {
        log.info("Counting users with role: {}", role);
        try {
            Long count = userService.countUsersByRole(role);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting users by role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> countUsersByStatus(@PathVariable User.UserStatus status) {
        log.info("Counting users with status: {}", status);
        try {
            Long count = userService.countUsersByStatus(status);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting users by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Identity Service is running");
    }
}
