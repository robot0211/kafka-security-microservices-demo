package com.kma.studentsystem.enrollment_service.service;

import com.kma.studentsystem.enrollment_service.dto.EnrollmentDTO;
import com.kma.studentsystem.enrollment_service.event.EnrollmentEvent;
import com.kma.studentsystem.enrollment_service.model.Enrollment;
import com.kma.studentsystem.enrollment_service.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final KafkaTemplate<String, EnrollmentEvent> kafkaTemplate;
    
    private static final String ENROLLMENT_EVENTS_TOPIC = "enrollment-events";
    
    public EnrollmentDTO createEnrollment(EnrollmentDTO enrollmentDTO) {
        log.info("Creating new enrollment for student: {} in course: {}", 
                enrollmentDTO.getStudentId(), enrollmentDTO.getCourseCode());
        
        // Check if enrollment already exists
        if (enrollmentRepository.existsByStudentIdAndCourseCodeAndAcademicYearAndSemester(
                enrollmentDTO.getStudentId(), enrollmentDTO.getCourseCode(), 
                enrollmentDTO.getAcademicYear(), enrollmentDTO.getSemester())) {
            throw new RuntimeException("Enrollment already exists for student " + enrollmentDTO.getStudentId() + 
                    " in course " + enrollmentDTO.getCourseCode() + " for " + 
                    enrollmentDTO.getAcademicYear() + " " + enrollmentDTO.getSemester());
        }
        
        // Convert DTO to Entity
        Enrollment enrollment = convertToEntity(enrollmentDTO);
        enrollment.setStatus(Enrollment.EnrollmentStatus.PENDING);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        
        // Save enrollment
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment created successfully: {}", savedEnrollment.getId());
        
        // Publish event
        EnrollmentEvent event = EnrollmentEvent.createEnrollmentCreatedEvent(savedEnrollment);
        kafkaTemplate.send(ENROLLMENT_EVENTS_TOPIC, savedEnrollment.getStudentId(), event);
        log.info("Published EnrollmentCreated event for student: {}", savedEnrollment.getStudentId());
        
        return convertToDTO(savedEnrollment);
    }
    
    public EnrollmentDTO updateEnrollment(Long id, EnrollmentDTO enrollmentDTO) {
        log.info("Updating enrollment with ID: {}", id);
        
        Enrollment existingEnrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
        
        // Update fields
        existingEnrollment.setStudentId(enrollmentDTO.getStudentId());
        existingEnrollment.setCourseCode(enrollmentDTO.getCourseCode());
        existingEnrollment.setAcademicYear(enrollmentDTO.getAcademicYear());
        existingEnrollment.setSemester(enrollmentDTO.getSemester());
        existingEnrollment.setStatus(enrollmentDTO.getStatus());
        existingEnrollment.setEnrollmentDate(enrollmentDTO.getEnrollmentDate());
        existingEnrollment.setCompletionDate(enrollmentDTO.getCompletionDate());
        existingEnrollment.setWithdrawalDate(enrollmentDTO.getWithdrawalDate());
        existingEnrollment.setFinalGrade(enrollmentDTO.getFinalGrade());
        existingEnrollment.setLetterGrade(enrollmentDTO.getLetterGrade());
        existingEnrollment.setCreditsEarned(enrollmentDTO.getCreditsEarned());
        existingEnrollment.setGpaPoints(enrollmentDTO.getGpaPoints());
        existingEnrollment.setInstructorName(enrollmentDTO.getInstructorName());
        existingEnrollment.setInstructorId(enrollmentDTO.getInstructorId());
        existingEnrollment.setNotes(enrollmentDTO.getNotes());
        existingEnrollment.setIsAudit(enrollmentDTO.getIsAudit());
        existingEnrollment.setIsPassFail(enrollmentDTO.getIsPassFail());
        existingEnrollment.setPrerequisiteMet(enrollmentDTO.getPrerequisiteMet());
        
        Enrollment updatedEnrollment = enrollmentRepository.save(existingEnrollment);
        log.info("Enrollment updated successfully: {}", updatedEnrollment.getId());
        
        // Publish event
        EnrollmentEvent event = EnrollmentEvent.createEnrollmentUpdatedEvent(updatedEnrollment);
        kafkaTemplate.send(ENROLLMENT_EVENTS_TOPIC, updatedEnrollment.getStudentId(), event);
        log.info("Published EnrollmentUpdated event for student: {}", updatedEnrollment.getStudentId());
        
        return convertToDTO(updatedEnrollment);
    }
    
    public EnrollmentDTO approveEnrollment(Long id) {
        log.info("Approving enrollment with ID: {}", id);
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
        
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.PENDING) {
            throw new RuntimeException("Only pending enrollments can be approved");
        }
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        
        Enrollment approvedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment approved successfully: {}", approvedEnrollment.getId());
        
        // Publish event
        EnrollmentEvent event = EnrollmentEvent.createEnrollmentUpdatedEvent(approvedEnrollment);
        kafkaTemplate.send(ENROLLMENT_EVENTS_TOPIC, approvedEnrollment.getStudentId(), event);
        log.info("Published EnrollmentUpdated event for student: {}", approvedEnrollment.getStudentId());
        
        return convertToDTO(approvedEnrollment);
    }
    
    public EnrollmentDTO completeEnrollment(Long id, Double finalGrade) {
        log.info("Completing enrollment with ID: {} with grade: {}", id, finalGrade);
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
        
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ENROLLED) {
            throw new RuntimeException("Only enrolled courses can be completed");
        }
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        enrollment.setFinalGrade(finalGrade);
        enrollment.setCompletionDate(LocalDateTime.now());
        
        // Set letter grade based on final grade
        if (finalGrade != null) {
            enrollment.setLetterGrade(getLetterGrade(finalGrade));
        }
        
        Enrollment completedEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment completed successfully: {}", completedEnrollment.getId());
        
        // Publish event
        EnrollmentEvent event = EnrollmentEvent.createEnrollmentCompletedEvent(completedEnrollment);
        kafkaTemplate.send(ENROLLMENT_EVENTS_TOPIC, completedEnrollment.getStudentId(), event);
        log.info("Published EnrollmentCompleted event for student: {}", completedEnrollment.getStudentId());
        
        return convertToDTO(completedEnrollment);
    }
    
    public EnrollmentDTO cancelEnrollment(Long id) {
        log.info("Cancelling enrollment with ID: {}", id);
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
        
        if (enrollment.getStatus() == Enrollment.EnrollmentStatus.COMPLETED) {
            throw new RuntimeException("Completed enrollments cannot be cancelled");
        }
        
        enrollment.setStatus(Enrollment.EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawalDate(LocalDateTime.now());
        
        Enrollment cancelledEnrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment cancelled successfully: {}", cancelledEnrollment.getId());
        
        // Publish event
        EnrollmentEvent event = EnrollmentEvent.createEnrollmentCancelledEvent(cancelledEnrollment);
        kafkaTemplate.send(ENROLLMENT_EVENTS_TOPIC, cancelledEnrollment.getStudentId(), event);
        log.info("Published EnrollmentCancelled event for student: {}", cancelledEnrollment.getStudentId());
        
        return convertToDTO(cancelledEnrollment);
    }
    
    public void deleteEnrollment(Long id) {
        log.info("Deleting enrollment with ID: {}", id);
        
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
        
        String studentId = enrollment.getStudentId();
        String courseCode = enrollment.getCourseCode();
        enrollmentRepository.delete(enrollment);
        log.info("Enrollment deleted successfully: {}", id);
        
        // Publish event
        EnrollmentEvent event = EnrollmentEvent.createEnrollmentDeletedEvent(studentId, courseCode);
        kafkaTemplate.send(ENROLLMENT_EVENTS_TOPIC, studentId, event);
        log.info("Published EnrollmentDeleted event for student: {}", studentId);
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments() {
        log.info("Retrieving all enrollments");
        return enrollmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<EnrollmentDTO> getEnrollmentById(Long id) {
        log.info("Retrieving enrollment with ID: {}", id);
        return enrollmentRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByStudent(String studentId) {
        log.info("Retrieving enrollments for student: {}", studentId);
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getEnrollmentsByCourse(String courseCode) {
        log.info("Retrieving enrollments for course: {}", courseCode);
        return enrollmentRepository.findByCourseCode(courseCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getActiveEnrollmentsByStudent(String studentId) {
        log.info("Retrieving active enrollments for student: {}", studentId);
        return enrollmentRepository.findActiveEnrollmentsByStudent(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getCompletedEnrollmentsByStudent(String studentId) {
        log.info("Retrieving completed enrollments for student: {}", studentId);
        return enrollmentRepository.findCompletedEnrollmentsByStudent(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long countActiveEnrollmentsByCourse(String courseCode) {
        log.info("Counting active enrollments for course: {}", courseCode);
        return enrollmentRepository.countActiveEnrollmentsByCourse(courseCode);
    }
    
    @Transactional(readOnly = true)
    public Double getAverageGradeByStudent(String studentId) {
        log.info("Calculating average grade for student: {}", studentId);
        return enrollmentRepository.getAverageGradeByStudent(studentId);
    }
    
    @Transactional(readOnly = true)
    public Integer getTotalCreditsByStudent(String studentId) {
        log.info("Calculating total credits for student: {}", studentId);
        return enrollmentRepository.getTotalCreditsByStudent(studentId);
    }
    
    private String getLetterGrade(Double grade) {
        if (grade >= 9.0) return "A+";
        else if (grade >= 8.5) return "A";
        else if (grade >= 8.0) return "B+";
        else if (grade >= 7.0) return "B";
        else if (grade >= 6.5) return "C+";
        else if (grade >= 5.5) return "C";
        else if (grade >= 5.0) return "D+";
        else if (grade >= 4.0) return "D";
        else return "F";
    }
    
    private Enrollment convertToEntity(EnrollmentDTO dto) {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(dto.getId());
        enrollment.setStudentId(dto.getStudentId());
        enrollment.setCourseCode(dto.getCourseCode());
        enrollment.setAcademicYear(dto.getAcademicYear());
        enrollment.setSemester(dto.getSemester());
        enrollment.setStatus(dto.getStatus());
        enrollment.setEnrollmentDate(dto.getEnrollmentDate());
        enrollment.setCompletionDate(dto.getCompletionDate());
        enrollment.setWithdrawalDate(dto.getWithdrawalDate());
        enrollment.setFinalGrade(dto.getFinalGrade());
        enrollment.setLetterGrade(dto.getLetterGrade());
        enrollment.setCreditsEarned(dto.getCreditsEarned());
        enrollment.setGpaPoints(dto.getGpaPoints());
        enrollment.setInstructorName(dto.getInstructorName());
        enrollment.setInstructorId(dto.getInstructorId());
        enrollment.setNotes(dto.getNotes());
        enrollment.setIsAudit(dto.getIsAudit());
        enrollment.setIsPassFail(dto.getIsPassFail());
        enrollment.setPrerequisiteMet(dto.getPrerequisiteMet());
        return enrollment;
    }
    
    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setStudentId(enrollment.getStudentId());
        dto.setCourseCode(enrollment.getCourseCode());
        dto.setAcademicYear(enrollment.getAcademicYear());
        dto.setSemester(enrollment.getSemester());
        dto.setStatus(enrollment.getStatus());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setCompletionDate(enrollment.getCompletionDate());
        dto.setWithdrawalDate(enrollment.getWithdrawalDate());
        dto.setFinalGrade(enrollment.getFinalGrade());
        dto.setLetterGrade(enrollment.getLetterGrade());
        dto.setCreditsEarned(enrollment.getCreditsEarned());
        dto.setGpaPoints(enrollment.getGpaPoints());
        dto.setInstructorName(enrollment.getInstructorName());
        dto.setInstructorId(enrollment.getInstructorId());
        dto.setNotes(enrollment.getNotes());
        dto.setIsAudit(enrollment.getIsAudit());
        dto.setIsPassFail(enrollment.getIsPassFail());
        dto.setPrerequisiteMet(enrollment.getPrerequisiteMet());
        dto.setCreatedAt(enrollment.getCreatedAt());
        dto.setUpdatedAt(enrollment.getUpdatedAt());
        return dto;
    }
}
