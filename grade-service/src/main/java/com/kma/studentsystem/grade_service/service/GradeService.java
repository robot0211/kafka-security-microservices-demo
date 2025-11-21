package com.kma.studentsystem.grade_service.service;

import com.kma.studentsystem.grade_service.dto.GradeDTO;
import com.kma.studentsystem.grade_service.event.GradeEvent;
import com.kma.studentsystem.grade_service.model.Grade;
import com.kma.studentsystem.grade_service.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GradeService {
    
    private final GradeRepository gradeRepository;
    private final KafkaTemplate<String, GradeEvent> kafkaTemplate;
    
    private static final String GRADE_EVENTS_TOPIC = "grade-events";
    
    public GradeDTO createGrade(GradeDTO gradeDTO) {
        log.info("Creating new grade for student: {} in course: {}", gradeDTO.getStudentId(), gradeDTO.getCourseCode());
        
        // Check if grade already exists for the same student, course, and type
        if (gradeRepository.existsByStudentIdAndCourseCodeAndGradeType(
                gradeDTO.getStudentId(), gradeDTO.getCourseCode(), gradeDTO.getGradeType())) {
            throw new RuntimeException("Grade already exists for student " + gradeDTO.getStudentId() + 
                    " in course " + gradeDTO.getCourseCode() + " of type " + gradeDTO.getGradeType());
        }
        
        // Convert DTO to Entity
        Grade grade = convertToEntity(gradeDTO);
        grade.setStatus(Grade.GradeStatus.PENDING);
        grade.setGradedAt(LocalDateTime.now());
        
        // Save grade
        Grade savedGrade = gradeRepository.save(grade);
        log.info("Grade created successfully: {}", savedGrade.getId());
        
        // Publish event
        GradeEvent event = GradeEvent.createGradeAssignedEvent(savedGrade);
        kafkaTemplate.send(GRADE_EVENTS_TOPIC, savedGrade.getStudentId(), event);
        log.info("Published GradeAssigned event for student: {}", savedGrade.getStudentId());
        
        return convertToDTO(savedGrade);
    }
    
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        log.info("Updating grade with ID: {}", id);
        
        Grade existingGrade = gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found with ID: " + id));
        
        // Update fields
        existingGrade.setGradeValue(gradeDTO.getGradeValue());
        existingGrade.setGradeType(gradeDTO.getGradeType());
        existingGrade.setDescription(gradeDTO.getDescription());
        existingGrade.setAcademicYear(gradeDTO.getAcademicYear());
        existingGrade.setSemester(gradeDTO.getSemester());
        existingGrade.setStatus(gradeDTO.getStatus());
        existingGrade.setInstructorName(gradeDTO.getInstructorName());
        existingGrade.setInstructorId(gradeDTO.getInstructorId());
        existingGrade.setDueDate(gradeDTO.getDueDate());
        existingGrade.setSubmittedAt(gradeDTO.getSubmittedAt());
        existingGrade.setComments(gradeDTO.getComments());
        existingGrade.setIsFinalGrade(gradeDTO.getIsFinalGrade());
        existingGrade.setWeight(gradeDTO.getWeight());
        
        // Update graded timestamp if status changed to GRADED
        if (gradeDTO.getStatus() == Grade.GradeStatus.GRADED && existingGrade.getGradedAt() == null) {
            existingGrade.setGradedAt(LocalDateTime.now());
        }
        
        Grade updatedGrade = gradeRepository.save(existingGrade);
        log.info("Grade updated successfully: {}", updatedGrade.getId());
        
        // Publish event
        GradeEvent event = GradeEvent.createGradeUpdatedEvent(updatedGrade);
        kafkaTemplate.send(GRADE_EVENTS_TOPIC, updatedGrade.getStudentId(), event);
        log.info("Published GradeUpdated event for student: {}", updatedGrade.getStudentId());
        
        return convertToDTO(updatedGrade);
    }
    
    public GradeDTO finalizeGrade(Long id) {
        log.info("Finalizing grade with ID: {}", id);
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found with ID: " + id));
        
        if (grade.getStatus() != Grade.GradeStatus.GRADED) {
            throw new RuntimeException("Grade must be graded before finalization");
        }
        
        grade.setIsFinalGrade(true);
        grade.setStatus(Grade.GradeStatus.APPROVED);
        grade.setGradedAt(LocalDateTime.now());
        
        Grade finalizedGrade = gradeRepository.save(grade);
        log.info("Grade finalized successfully: {}", finalizedGrade.getId());
        
        // Publish event
        GradeEvent event = GradeEvent.createGradeFinalizedEvent(finalizedGrade);
        kafkaTemplate.send(GRADE_EVENTS_TOPIC, finalizedGrade.getStudentId(), event);
        log.info("Published GradeFinalized event for student: {}", finalizedGrade.getStudentId());
        
        return convertToDTO(finalizedGrade);
    }
    
    public void deleteGrade(Long id) {
        log.info("Deleting grade with ID: {}", id);
        
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Grade not found with ID: " + id));
        
        String studentId = grade.getStudentId();
        String courseCode = grade.getCourseCode();
        gradeRepository.delete(grade);
        log.info("Grade deleted successfully: {}", id);
        
        // Publish event
        GradeEvent event = GradeEvent.createGradeDeletedEvent(studentId, courseCode);
        kafkaTemplate.send(GRADE_EVENTS_TOPIC, studentId, event);
        log.info("Published GradeDeleted event for student: {}", studentId);
    }
    
    @Transactional(readOnly = true)
    public List<GradeDTO> getAllGrades() {
        log.info("Retrieving all grades");
        return gradeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<GradeDTO> getGradeById(Long id) {
        log.info("Retrieving grade with ID: {}", id);
        return gradeRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByStudent(String studentId) {
        log.info("Retrieving grades for student: {}", studentId);
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByCourse(String courseCode) {
        log.info("Retrieving grades for course: {}", courseCode);
        return gradeRepository.findByCourseCode(courseCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GradeDTO> getGradesByStudentAndCourse(String studentId, String courseCode) {
        log.info("Retrieving grades for student: {} in course: {}", studentId, courseCode);
        return gradeRepository.findByStudentIdAndCourseCode(studentId, courseCode).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<GradeDTO> getFinalGradeByStudentAndCourse(String studentId, String courseCode) {
        log.info("Retrieving final grade for student: {} in course: {}", studentId, courseCode);
        return gradeRepository.findFinalGradeByStudentAndCourse(studentId, courseCode)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAverageGradeByStudentAndCourse(String studentId, String courseCode) {
        log.info("Calculating average grade for student: {} in course: {}", studentId, courseCode);
        return gradeRepository.getAverageGradeByStudentAndCourse(studentId, courseCode);
    }
    
    @Transactional(readOnly = true)
    public BigDecimal getAverageGradeByStudentAndSemester(String studentId, Integer academicYear, String semester) {
        log.info("Calculating average grade for student: {} in semester: {} {}", studentId, academicYear, semester);
        return gradeRepository.getAverageGradeByStudentAndSemester(studentId, academicYear, semester);
    }
    
    @Transactional(readOnly = true)
    public List<GradeDTO> getOverdueGrades() {
        log.info("Retrieving overdue grades");
        return gradeRepository.findOverdueGrades().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long countGradedStudentsByCourse(String courseCode) {
        log.info("Counting graded students for course: {}", courseCode);
        return gradeRepository.countGradedStudentsByCourse(courseCode);
    }
    
    private Grade convertToEntity(GradeDTO dto) {
        Grade grade = new Grade();
        grade.setId(dto.getId());
        grade.setStudentId(dto.getStudentId());
        grade.setCourseCode(dto.getCourseCode());
        grade.setGradeValue(dto.getGradeValue());
        grade.setGradeType(dto.getGradeType());
        grade.setDescription(dto.getDescription());
        grade.setAcademicYear(dto.getAcademicYear());
        grade.setSemester(dto.getSemester());
        grade.setStatus(dto.getStatus());
        grade.setInstructorName(dto.getInstructorName());
        grade.setInstructorId(dto.getInstructorId());
        grade.setGradedAt(dto.getGradedAt());
        grade.setDueDate(dto.getDueDate());
        grade.setSubmittedAt(dto.getSubmittedAt());
        grade.setComments(dto.getComments());
        grade.setIsFinalGrade(dto.getIsFinalGrade());
        grade.setWeight(dto.getWeight());
        return grade;
    }
    
    private GradeDTO convertToDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudentId());
        dto.setCourseCode(grade.getCourseCode());
        dto.setGradeValue(grade.getGradeValue());
        dto.setGradeType(grade.getGradeType());
        dto.setDescription(grade.getDescription());
        dto.setAcademicYear(grade.getAcademicYear());
        dto.setSemester(grade.getSemester());
        dto.setStatus(grade.getStatus());
        dto.setInstructorName(grade.getInstructorName());
        dto.setInstructorId(grade.getInstructorId());
        dto.setGradedAt(grade.getGradedAt());
        dto.setDueDate(grade.getDueDate());
        dto.setSubmittedAt(grade.getSubmittedAt());
        dto.setComments(grade.getComments());
        dto.setIsFinalGrade(grade.getIsFinalGrade());
        dto.setWeight(grade.getWeight());
        dto.setCreatedAt(grade.getCreatedAt());
        dto.setUpdatedAt(grade.getUpdatedAt());
        return dto;
    }
}
