package com.kma.studentsystem.course_service.repository;

import com.kma.studentsystem.course_service.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findByStatus(Course.CourseStatus status);
    
    List<Course> findByDepartment(String department);
    
    List<Course> findByLevel(Course.CourseLevel level);
    
    List<Course> findByAcademicYear(Integer academicYear);
    
    List<Course> findBySemester(String semester);
    
    List<Course> findByAcademicYearAndSemester(Integer academicYear, String semester);
    
    @Query("SELECT c FROM Course c WHERE c.courseName LIKE %:name% OR c.courseCode LIKE %:name%")
    List<Course> findByNameOrCodeContaining(@Param("name") String name);
    
    @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE' AND c.enrolledCount < c.capacity")
    List<Course> findAvailableCourses();
    
    @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE' AND c.department = :department AND c.enrolledCount < c.capacity")
    List<Course> findAvailableCoursesByDepartment(@Param("department") String department);
    
    @Query("SELECT c FROM Course c WHERE c.status = 'ACTIVE' AND c.level = :level AND c.enrolledCount < c.capacity")
    List<Course> findAvailableCoursesByLevel(@Param("level") Course.CourseLevel level);
    
    @Query("SELECT c FROM Course c WHERE c.instructorName LIKE %:instructor%")
    List<Course> findByInstructorNameContaining(@Param("instructor") String instructor);
    
    @Query("SELECT c FROM Course c WHERE c.credits = :credits")
    List<Course> findByCredits(@Param("credits") Integer credits);
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.status = 'ACTIVE'")
    Long countActiveCourses();
    
    @Query("SELECT COUNT(c) FROM Course c WHERE c.department = :department AND c.status = 'ACTIVE'")
    Long countActiveCoursesByDepartment(@Param("department") String department);
    
    @Query("SELECT SUM(c.enrolledCount) FROM Course c WHERE c.status = 'ACTIVE'")
    Long getTotalEnrolledStudents();
    
    @Query("SELECT AVG(c.enrolledCount) FROM Course c WHERE c.status = 'ACTIVE'")
    Double getAverageEnrollment();
    
    boolean existsByCourseCode(String courseCode);
    
    @Query("SELECT c FROM Course c WHERE c.courseCode IN :courseCodes")
    List<Course> findByCourseCodeIn(@Param("courseCodes") List<String> courseCodes);
}
