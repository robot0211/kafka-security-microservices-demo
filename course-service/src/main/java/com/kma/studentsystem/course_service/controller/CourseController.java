package com.kma.studentsystem.course_service.controller;

import com.kma.studentsystem.course_service.dto.CourseDTO;
import com.kma.studentsystem.course_service.model.Course;
import com.kma.studentsystem.course_service.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        log.info("POST /api/courses - Creating new course: {}", courseDTO.getCourseCode());
        try {
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        log.info("GET /api/courses - Retrieving all courses");
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        log.info("GET /api/courses/{} - Retrieving course by ID", id);
        Optional<CourseDTO> course = courseService.getCourseById(id);
        return course.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{courseCode}")
    public ResponseEntity<CourseDTO> getCourseByCode(@PathVariable String courseCode) {
        log.info("GET /api/courses/code/{} - Retrieving course by code", courseCode);
        Optional<CourseDTO> course = courseService.getCourseByCode(courseCode);
        return course.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, 
                                                @Valid @RequestBody CourseDTO courseDTO) {
        log.info("PUT /api/courses/{} - Updating course", id);
        try {
            CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            log.error("Error updating course: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("DELETE /api/courses/{} - Deleting course", id);
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting course: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseDTO>> getCoursesByStatus(@PathVariable Course.CourseStatus status) {
        log.info("GET /api/courses/status/{} - Retrieving courses by status", status);
        List<CourseDTO> courses = courseService.getCoursesByStatus(status);
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/department/{department}")
    public ResponseEntity<List<CourseDTO>> getCoursesByDepartment(@PathVariable String department) {
        log.info("GET /api/courses/department/{} - Retrieving courses by department", department);
        List<CourseDTO> courses = courseService.getCoursesByDepartment(department);
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<CourseDTO>> getAvailableCourses() {
        log.info("GET /api/courses/available - Retrieving available courses");
        List<CourseDTO> courses = courseService.getAvailableCourses();
        return ResponseEntity.ok(courses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String q) {
        log.info("GET /api/courses/search?q={} - Searching courses", q);
        List<CourseDTO> courses = courseService.searchCourses(q);
        return ResponseEntity.ok(courses);
    }
    
    @PutMapping("/{courseCode}/enrollment")
    public ResponseEntity<CourseDTO> updateEnrollmentCount(@PathVariable String courseCode, 
                                                          @RequestParam int delta) {
        log.info("PUT /api/courses/{}/enrollment?delta={} - Updating enrollment count", courseCode, delta);
        try {
            CourseDTO updatedCourse = courseService.updateEnrollmentCount(courseCode, delta);
            return ResponseEntity.ok(updatedCourse);
        } catch (Exception e) {
            log.error("Error updating enrollment count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/stats/active-count")
    public ResponseEntity<Long> getActiveCourseCount() {
        log.info("GET /api/courses/stats/active-count - Getting active course count");
        Long count = courseService.getActiveCourseCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/total-enrolled")
    public ResponseEntity<Long> getTotalEnrolledStudents() {
        log.info("GET /api/courses/stats/total-enrolled - Getting total enrolled students");
        Long total = courseService.getTotalEnrolledStudents();
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("GET /api/courses/health - Health check");
        return ResponseEntity.ok("Course Service is running!");
    }
}
