package com.kma.studentsystem.student_service.controller;

import com.kma.studentsystem.student_service.dto.StudentDTO;
import com.kma.studentsystem.student_service.model.Student;
import com.kma.studentsystem.student_service.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class StudentController {
    
    private final StudentService studentService;
    
    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        log.info("POST /api/students - Creating new student: {}", studentDTO.getStudentId());
        try {
            StudentDTO createdStudent = studentService.createStudent(studentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (Exception e) {
            log.error("Error creating student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        log.info("GET /api/students - Retrieving all students");
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        log.info("GET /api/students/{} - Retrieving student by ID", id);
        Optional<StudentDTO> student = studentService.getStudentById(id);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/student-id/{studentId}")
    public ResponseEntity<StudentDTO> getStudentByStudentId(@PathVariable String studentId) {
        log.info("GET /api/students/student-id/{} - Retrieving student by student ID", studentId);
        Optional<StudentDTO> student = studentService.getStudentByStudentId(studentId);
        return student.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, 
                                                   @Valid @RequestBody StudentDTO studentDTO) {
        log.info("PUT /api/students/{} - Updating student", id);
        try {
            StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            log.error("Error updating student: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        log.info("DELETE /api/students/{} - Deleting student", id);
        try {
            studentService.deleteStudent(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting student: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudentDTO>> getStudentsByStatus(@PathVariable Student.StudentStatus status) {
        log.info("GET /api/students/status/{} - Retrieving students by status", status);
        List<StudentDTO> students = studentService.getStudentsByStatus(status);
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/major/{major}")
    public ResponseEntity<List<StudentDTO>> getStudentsByMajor(@PathVariable String major) {
        log.info("GET /api/students/major/{} - Retrieving students by major", major);
        List<StudentDTO> students = studentService.getStudentsByMajor(major);
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<StudentDTO>> searchStudentsByName(@RequestParam String name) {
        log.info("GET /api/students/search?name={} - Searching students by name", name);
        List<StudentDTO> students = studentService.searchStudentsByName(name);
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/stats/active-count")
    public ResponseEntity<Long> getActiveStudentCount() {
        log.info("GET /api/students/stats/active-count - Getting active student count");
        Long count = studentService.getActiveStudentCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/average-gpa")
    public ResponseEntity<Double> getAverageGpa() {
        log.info("GET /api/students/stats/average-gpa - Getting average GPA");
        Double averageGpa = studentService.getAverageGpa();
        return ResponseEntity.ok(averageGpa);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("GET /api/students/health - Health check");
        return ResponseEntity.ok("Student Service is running!");
    }
}
