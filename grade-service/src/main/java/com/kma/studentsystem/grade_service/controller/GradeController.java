package com.kma.studentsystem.grade_service.controller;

import com.kma.studentsystem.grade_service.dto.GradeDTO;
import com.kma.studentsystem.grade_service.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GradeController {
    
    private final GradeService gradeService;
    
    @PostMapping
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        log.info("POST /api/grades - Creating new grade for student: {} in course: {}", 
                gradeDTO.getStudentId(), gradeDTO.getCourseCode());
        try {
            GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (Exception e) {
            log.error("Error creating grade: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGrades() {
        log.info("GET /api/grades - Retrieving all grades");
        List<GradeDTO> grades = gradeService.getAllGrades();
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        log.info("GET /api/grades/{} - Retrieving grade by ID", id);
        Optional<GradeDTO> grade = gradeService.getGradeById(id);
        return grade.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudent(@PathVariable String studentId) {
        log.info("GET /api/grades/student/{} - Retrieving grades for student", studentId);
        List<GradeDTO> grades = gradeService.getGradesByStudent(studentId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/course/{courseCode}")
    public ResponseEntity<List<GradeDTO>> getGradesByCourse(@PathVariable String courseCode) {
        log.info("GET /api/grades/course/{} - Retrieving grades for course", courseCode);
        List<GradeDTO> grades = gradeService.getGradesByCourse(courseCode);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/student/{studentId}/course/{courseCode}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentAndCourse(@PathVariable String studentId, 
                                                                     @PathVariable String courseCode) {
        log.info("GET /api/grades/student/{}/course/{} - Retrieving grades for student in course", studentId, courseCode);
        List<GradeDTO> grades = gradeService.getGradesByStudentAndCourse(studentId, courseCode);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/student/{studentId}/course/{courseCode}/final")
    public ResponseEntity<GradeDTO> getFinalGradeByStudentAndCourse(@PathVariable String studentId, 
                                                                   @PathVariable String courseCode) {
        log.info("GET /api/grades/student/{}/course/{}/final - Retrieving final grade", studentId, courseCode);
        Optional<GradeDTO> grade = gradeService.getFinalGradeByStudentAndCourse(studentId, courseCode);
        return grade.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, 
                                              @Valid @RequestBody GradeDTO gradeDTO) {
        log.info("PUT /api/grades/{} - Updating grade", id);
        try {
            GradeDTO updatedGrade = gradeService.updateGrade(id, gradeDTO);
            return ResponseEntity.ok(updatedGrade);
        } catch (Exception e) {
            log.error("Error updating grade: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/{id}/finalize")
    public ResponseEntity<GradeDTO> finalizeGrade(@PathVariable Long id) {
        log.info("PUT /api/grades/{}/finalize - Finalizing grade", id);
        try {
            GradeDTO finalizedGrade = gradeService.finalizeGrade(id);
            return ResponseEntity.ok(finalizedGrade);
        } catch (Exception e) {
            log.error("Error finalizing grade: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        log.info("DELETE /api/grades/{} - Deleting grade", id);
        try {
            gradeService.deleteGrade(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting grade: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/student/{studentId}/course/{courseCode}/average")
    public ResponseEntity<BigDecimal> getAverageGradeByStudentAndCourse(@PathVariable String studentId, 
                                                                       @PathVariable String courseCode) {
        log.info("GET /api/grades/student/{}/course/{}/average - Getting average grade", studentId, courseCode);
        BigDecimal average = gradeService.getAverageGradeByStudentAndCourse(studentId, courseCode);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/student/{studentId}/semester/{academicYear}/{semester}/average")
    public ResponseEntity<BigDecimal> getAverageGradeByStudentAndSemester(@PathVariable String studentId, 
                                                                         @PathVariable Integer academicYear, 
                                                                         @PathVariable String semester) {
        log.info("GET /api/grades/student/{}/semester/{}/{}/average - Getting semester average", studentId, academicYear, semester);
        BigDecimal average = gradeService.getAverageGradeByStudentAndSemester(studentId, academicYear, semester);
        return ResponseEntity.ok(average);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<GradeDTO>> getOverdueGrades() {
        log.info("GET /api/grades/overdue - Retrieving overdue grades");
        List<GradeDTO> grades = gradeService.getOverdueGrades();
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/course/{courseCode}/graded-count")
    public ResponseEntity<Long> countGradedStudentsByCourse(@PathVariable String courseCode) {
        log.info("GET /api/grades/course/{}/graded-count - Counting graded students", courseCode);
        Long count = gradeService.countGradedStudentsByCourse(courseCode);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("GET /api/grades/health - Health check");
        return ResponseEntity.ok("Grade Service is running!");
    }
}
