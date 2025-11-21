package com.kma.studentsystem.student_service.service;

import com.kma.studentsystem.student_service.dto.StudentDTO;
import com.kma.studentsystem.student_service.event.StudentEvent;
import com.kma.studentsystem.student_service.model.Student;
import com.kma.studentsystem.student_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final KafkaTemplate<String, StudentEvent> kafkaTemplate;
    
    private static final String STUDENT_EVENTS_TOPIC = "student-events";
    
    public StudentDTO createStudent(StudentDTO studentDTO) {
        log.info("Creating new student: {}", studentDTO.getStudentId());
        
        // Check if student already exists
        if (studentRepository.existsByStudentId(studentDTO.getStudentId())) {
            throw new RuntimeException("Student with ID " + studentDTO.getStudentId() + " already exists");
        }
        
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new RuntimeException("Student with email " + studentDTO.getEmail() + " already exists");
        }
        
        // Convert DTO to Entity
        Student student = convertToEntity(studentDTO);
        student.setStatus(Student.StudentStatus.ACTIVE);
        
        // Save student
        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully: {}", savedStudent.getStudentId());
        
        // Publish event
        StudentEvent event = StudentEvent.createStudentCreatedEvent(savedStudent);
        kafkaTemplate.send(STUDENT_EVENTS_TOPIC, savedStudent.getStudentId(), event);
        log.info("Published StudentCreated event for student: {}", savedStudent.getStudentId());
        
        return convertToDTO(savedStudent);
    }
    
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        log.info("Updating student with ID: {}", id);
        
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));
        
        // Check if email is being changed and if new email already exists
        if (!existingStudent.getEmail().equals(studentDTO.getEmail()) && 
            studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new RuntimeException("Student with email " + studentDTO.getEmail() + " already exists");
        }
        
        // Update fields
        existingStudent.setFirstName(studentDTO.getFirstName());
        existingStudent.setLastName(studentDTO.getLastName());
        existingStudent.setEmail(studentDTO.getEmail());
        existingStudent.setPhoneNumber(studentDTO.getPhoneNumber());
        existingStudent.setDateOfBirth(studentDTO.getDateOfBirth());
        existingStudent.setGender(studentDTO.getGender());
        existingStudent.setAddress(studentDTO.getAddress());
        existingStudent.setMajor(studentDTO.getMajor());
        existingStudent.setStatus(studentDTO.getStatus());
        existingStudent.setGpa(studentDTO.getGpa());
        existingStudent.setEnrollmentYear(studentDTO.getEnrollmentYear());
        
        Student updatedStudent = studentRepository.save(existingStudent);
        log.info("Student updated successfully: {}", updatedStudent.getStudentId());
        
        // Publish event
        StudentEvent event = StudentEvent.createStudentUpdatedEvent(updatedStudent);
        kafkaTemplate.send(STUDENT_EVENTS_TOPIC, updatedStudent.getStudentId(), event);
        log.info("Published StudentUpdated event for student: {}", updatedStudent.getStudentId());
        
        return convertToDTO(updatedStudent);
    }
    
    public void deleteStudent(Long id) {
        log.info("Deleting student with ID: {}", id);
        
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));
        
        String studentId = student.getStudentId();
        studentRepository.delete(student);
        log.info("Student deleted successfully: {}", studentId);
        
        // Publish event
        StudentEvent event = StudentEvent.createStudentDeletedEvent(studentId);
        kafkaTemplate.send(STUDENT_EVENTS_TOPIC, studentId, event);
        log.info("Published StudentDeleted event for student: {}", studentId);
    }
    
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        log.info("Retrieving all students");
        return studentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentById(Long id) {
        log.info("Retrieving student with ID: {}", id);
        return studentRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<StudentDTO> getStudentByStudentId(String studentId) {
        log.info("Retrieving student with student ID: {}", studentId);
        return studentRepository.findByStudentId(studentId)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<StudentDTO> getStudentsByStatus(Student.StudentStatus status) {
        log.info("Retrieving students with status: {}", status);
        return studentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StudentDTO> getStudentsByMajor(String major) {
        log.info("Retrieving students with major: {}", major);
        return studentRepository.findByMajor(major).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<StudentDTO> searchStudentsByName(String name) {
        log.info("Searching students by name: {}", name);
        return studentRepository.findByNameContaining(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long getActiveStudentCount() {
        log.info("Getting active student count");
        return studentRepository.countActiveStudents();
    }
    
    @Transactional(readOnly = true)
    public Double getAverageGpa() {
        log.info("Getting average GPA");
        return studentRepository.getAverageGpa();
    }
    
    private Student convertToEntity(StudentDTO dto) {
        Student student = new Student();
        student.setId(dto.getId());
        student.setStudentId(dto.getStudentId());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setPhoneNumber(dto.getPhoneNumber());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setGender(dto.getGender());
        student.setAddress(dto.getAddress());
        student.setMajor(dto.getMajor());
        student.setStatus(dto.getStatus());
        student.setGpa(dto.getGpa());
        student.setEnrollmentYear(dto.getEnrollmentYear());
        return student;
    }
    
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setStudentId(student.getStudentId());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setEmail(student.getEmail());
        dto.setPhoneNumber(student.getPhoneNumber());
        dto.setDateOfBirth(student.getDateOfBirth());
        dto.setGender(student.getGender());
        dto.setAddress(student.getAddress());
        dto.setMajor(student.getMajor());
        dto.setStatus(student.getStatus());
        dto.setGpa(student.getGpa());
        dto.setEnrollmentYear(student.getEnrollmentYear());
        dto.setCreatedAt(student.getCreatedAt());
        dto.setUpdatedAt(student.getUpdatedAt());
        return dto;
    }
}
