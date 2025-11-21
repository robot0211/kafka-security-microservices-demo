package com.kma.studentsystem.enrollment_service.controller;

import com.kma.studentsystem.enrollment_service.dto.EnrollmentDTO;
import com.kma.studentsystem.enrollment_service.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @PostMapping
    public ResponseEntity<EnrollmentDTO> createEnrollment(@Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        log.info("Creating new enrollment for student: {} in course: {}", 
                enrollmentDTO.getStudentId(), enrollmentDTO.getCourseCode());
        try {
            EnrollmentDTO createdEnrollment = enrollmentService.createEnrollment(enrollmentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEnrollment);
        } catch (Exception e) {
            log.error("Error creating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> updateEnrollment(@PathVariable Long id, 
                                                         @Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        log.info("Updating enrollment with ID: {}", id);
        try {
            EnrollmentDTO updatedEnrollment = enrollmentService.updateEnrollment(id, enrollmentDTO);
            return ResponseEntity.ok(updatedEnrollment);
        } catch (Exception e) {
            log.error("Error updating enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<EnrollmentDTO> approveEnrollment(@PathVariable Long id) {
        log.info("Approving enrollment with ID: {}", id);
        try {
            EnrollmentDTO approvedEnrollment = enrollmentService.approveEnrollment(id);
            return ResponseEntity.ok(approvedEnrollment);
        } catch (Exception e) {
            log.error("Error approving enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/complete")
    public ResponseEntity<EnrollmentDTO> completeEnrollment(@PathVariable Long id, 
                                                           @RequestParam Double finalGrade) {
        log.info("Completing enrollment with ID: {} with grade: {}", id, finalGrade);
        try {
            EnrollmentDTO completedEnrollment = enrollmentService.completeEnrollment(id, finalGrade);
            return ResponseEntity.ok(completedEnrollment);
        } catch (Exception e) {
            log.error("Error completing enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<EnrollmentDTO> cancelEnrollment(@PathVariable Long id) {
        log.info("Cancelling enrollment with ID: {}", id);
        try {
            EnrollmentDTO cancelledEnrollment = enrollmentService.cancelEnrollment(id);
            return ResponseEntity.ok(cancelledEnrollment);
        } catch (Exception e) {
            log.error("Error cancelling enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
        log.info("Deleting enrollment with ID: {}", id);
        try {
            enrollmentService.deleteEnrollment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments() {
        log.info("Retrieving all enrollments");
        try {
            List<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments();
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            log.error("Error retrieving enrollments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        log.info("Retrieving enrollment with ID: {}", id);
        try {
            Optional<EnrollmentDTO> enrollment = enrollmentService.getEnrollmentById(id);
            return enrollment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving enrollment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByStudent(@PathVariable String studentId) {
        log.info("Retrieving enrollments for student: {}", studentId);
        try {
            List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            log.error("Error retrieving enrollments for student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/course/{courseCode}")
    public ResponseEntity<List<EnrollmentDTO>> getEnrollmentsByCourse(@PathVariable String courseCode) {
        log.info("Retrieving enrollments for course: {}", courseCode);
        try {
            List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourse(courseCode);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            log.error("Error retrieving enrollments for course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<EnrollmentDTO>> getActiveEnrollmentsByStudent(@PathVariable String studentId) {
        log.info("Retrieving active enrollments for student: {}", studentId);
        try {
            List<EnrollmentDTO> enrollments = enrollmentService.getActiveEnrollmentsByStudent(studentId);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            log.error("Error retrieving active enrollments for student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/student/{studentId}/completed")
    public ResponseEntity<List<EnrollmentDTO>> getCompletedEnrollmentsByStudent(@PathVariable String studentId) {
        log.info("Retrieving completed enrollments for student: {}", studentId);
        try {
            List<EnrollmentDTO> enrollments = enrollmentService.getCompletedEnrollmentsByStudent(studentId);
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            log.error("Error retrieving completed enrollments for student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/course/{courseCode}/count")
    public ResponseEntity<Long> countActiveEnrollmentsByCourse(@PathVariable String courseCode) {
        log.info("Counting active enrollments for course: {}", courseCode);
        try {
            Long count = enrollmentService.countActiveEnrollmentsByCourse(courseCode);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("Error counting active enrollments for course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/student/{studentId}/average-grade")
    public ResponseEntity<Double> getAverageGradeByStudent(@PathVariable String studentId) {
        log.info("Calculating average grade for student: {}", studentId);
        try {
            Double averageGrade = enrollmentService.getAverageGradeByStudent(studentId);
            return ResponseEntity.ok(averageGrade);
        } catch (Exception e) {
            log.error("Error calculating average grade for student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/student/{studentId}/total-credits")
    public ResponseEntity<Integer> getTotalCreditsByStudent(@PathVariable String studentId) {
        log.info("Calculating total credits for student: {}", studentId);
        try {
            Integer totalCredits = enrollmentService.getTotalCreditsByStudent(studentId);
            return ResponseEntity.ok(totalCredits);
        } catch (Exception e) {
            log.error("Error calculating total credits for student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Enrollment Service is running");
    }
}
