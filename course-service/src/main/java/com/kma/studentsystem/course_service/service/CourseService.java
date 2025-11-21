package com.kma.studentsystem.course_service.service;

import com.kma.studentsystem.course_service.dto.CourseDTO;
import com.kma.studentsystem.course_service.event.CourseEvent;
import com.kma.studentsystem.course_service.model.Course;
import com.kma.studentsystem.course_service.repository.CourseRepository;
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
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final KafkaTemplate<String, CourseEvent> kafkaTemplate;
    
    private static final String COURSE_EVENTS_TOPIC = "course-events";
    
    public CourseDTO createCourse(CourseDTO courseDTO) {
        log.info("Creating new course: {}", courseDTO.getCourseCode());
        
        // Check if course already exists
        if (courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
            throw new RuntimeException("Course with code " + courseDTO.getCourseCode() + " already exists");
        }
        
        // Convert DTO to Entity
        Course course = convertToEntity(courseDTO);
        course.setStatus(Course.CourseStatus.ACTIVE);
        course.setEnrolledCount(0);
        
        // Save course
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully: {}", savedCourse.getCourseCode());
        
        // Publish event
        CourseEvent event = CourseEvent.createCourseCreatedEvent(savedCourse);
        kafkaTemplate.send(COURSE_EVENTS_TOPIC, savedCourse.getCourseCode(), event);
        log.info("Published CourseCreated event for course: {}", savedCourse.getCourseCode());
        
        return convertToDTO(savedCourse);
    }
    
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        log.info("Updating course with ID: {}", id);
        
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        
        // Check if course code is being changed and if new code already exists
        if (!existingCourse.getCourseCode().equals(courseDTO.getCourseCode()) && 
            courseRepository.existsByCourseCode(courseDTO.getCourseCode())) {
            throw new RuntimeException("Course with code " + courseDTO.getCourseCode() + " already exists");
        }
        
        // Update fields
        existingCourse.setCourseCode(courseDTO.getCourseCode());
        existingCourse.setCourseName(courseDTO.getCourseName());
        existingCourse.setDescription(courseDTO.getDescription());
        existingCourse.setCredits(courseDTO.getCredits());
        existingCourse.setDepartment(courseDTO.getDepartment());
        existingCourse.setLevel(courseDTO.getLevel());
        existingCourse.setStatus(courseDTO.getStatus());
        existingCourse.setCapacity(courseDTO.getCapacity());
        existingCourse.setAcademicYear(courseDTO.getAcademicYear());
        existingCourse.setSemester(courseDTO.getSemester());
        existingCourse.setPrerequisites(courseDTO.getPrerequisites());
        existingCourse.setInstructorName(courseDTO.getInstructorName());
        existingCourse.setInstructorEmail(courseDTO.getInstructorEmail());
        existingCourse.setSchedule(courseDTO.getSchedule());
        existingCourse.setLocation(courseDTO.getLocation());
        
        Course updatedCourse = courseRepository.save(existingCourse);
        log.info("Course updated successfully: {}", updatedCourse.getCourseCode());
        
        // Publish event
        CourseEvent event = CourseEvent.createCourseUpdatedEvent(updatedCourse);
        kafkaTemplate.send(COURSE_EVENTS_TOPIC, updatedCourse.getCourseCode(), event);
        log.info("Published CourseUpdated event for course: {}", updatedCourse.getCourseCode());
        
        return convertToDTO(updatedCourse);
    }
    
    public void deleteCourse(Long id) {
        log.info("Deleting course with ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        
        String courseCode = course.getCourseCode();
        courseRepository.delete(course);
        log.info("Course deleted successfully: {}", courseCode);
        
        // Publish event
        CourseEvent event = CourseEvent.createCourseDeletedEvent(courseCode);
        kafkaTemplate.send(COURSE_EVENTS_TOPIC, courseCode, event);
        log.info("Published CourseDeleted event for course: {}", courseCode);
    }
    
    public CourseDTO updateEnrollmentCount(String courseCode, int delta) {
        log.info("Updating enrollment count for course: {} by {}", courseCode, delta);
        
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found with code: " + courseCode));
        
        int newEnrolledCount = course.getEnrolledCount() + delta;
        if (newEnrolledCount < 0) {
            throw new RuntimeException("Enrollment count cannot be negative");
        }
        if (newEnrolledCount > course.getCapacity()) {
            throw new RuntimeException("Enrollment count cannot exceed capacity");
        }
        
        course.setEnrolledCount(newEnrolledCount);
        Course updatedCourse = courseRepository.save(course);
        log.info("Enrollment count updated for course: {} to {}", courseCode, newEnrolledCount);
        
        // Publish event
        CourseEvent event = CourseEvent.createCourseCapacityUpdatedEvent(updatedCourse);
        kafkaTemplate.send(COURSE_EVENTS_TOPIC, courseCode, event);
        log.info("Published CourseCapacityUpdated event for course: {}", courseCode);
        
        return convertToDTO(updatedCourse);
    }
    
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        log.info("Retrieving all courses");
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Optional<CourseDTO> getCourseById(Long id) {
        log.info("Retrieving course with ID: {}", id);
        return courseRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public Optional<CourseDTO> getCourseByCode(String courseCode) {
        log.info("Retrieving course with code: {}", courseCode);
        return courseRepository.findByCourseCode(courseCode)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByStatus(Course.CourseStatus status) {
        log.info("Retrieving courses with status: {}", status);
        return courseRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByDepartment(String department) {
        log.info("Retrieving courses with department: {}", department);
        return courseRepository.findByDepartment(department).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CourseDTO> getAvailableCourses() {
        log.info("Retrieving available courses");
        return courseRepository.findAvailableCourses().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CourseDTO> searchCourses(String searchTerm) {
        log.info("Searching courses with term: {}", searchTerm);
        return courseRepository.findByNameOrCodeContaining(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Long getActiveCourseCount() {
        log.info("Getting active course count");
        return courseRepository.countActiveCourses();
    }
    
    @Transactional(readOnly = true)
    public Long getTotalEnrolledStudents() {
        log.info("Getting total enrolled students");
        return courseRepository.getTotalEnrolledStudents();
    }
    
    private Course convertToEntity(CourseDTO dto) {
        Course course = new Course();
        course.setId(dto.getId());
        course.setCourseCode(dto.getCourseCode());
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setCredits(dto.getCredits());
        course.setDepartment(dto.getDepartment());
        course.setLevel(dto.getLevel());
        course.setStatus(dto.getStatus());
        course.setCapacity(dto.getCapacity());
        course.setEnrolledCount(dto.getEnrolledCount());
        course.setAcademicYear(dto.getAcademicYear());
        course.setSemester(dto.getSemester());
        course.setPrerequisites(dto.getPrerequisites());
        course.setInstructorName(dto.getInstructorName());
        course.setInstructorEmail(dto.getInstructorEmail());
        course.setSchedule(dto.getSchedule());
        course.setLocation(dto.getLocation());
        return course;
    }
    
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setCredits(course.getCredits());
        dto.setDepartment(course.getDepartment());
        dto.setLevel(course.getLevel());
        dto.setStatus(course.getStatus());
        dto.setCapacity(course.getCapacity());
        dto.setEnrolledCount(course.getEnrolledCount());
        dto.setAcademicYear(course.getAcademicYear());
        dto.setSemester(course.getSemester());
        dto.setPrerequisites(course.getPrerequisites());
        dto.setInstructorName(course.getInstructorName());
        dto.setInstructorEmail(course.getInstructorEmail());
        dto.setSchedule(course.getSchedule());
        dto.setLocation(course.getLocation());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        return dto;
    }
}
