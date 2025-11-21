package com.kma.studentsystem.enrollment_service.repository;

import com.kma.studentsystem.enrollment_service.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudentId(String studentId);
    
    List<Enrollment> findByCourseCode(String courseCode);
    
    Optional<Enrollment> findByStudentIdAndCourseCodeAndAcademicYearAndSemester(
            String studentId, String courseCode, Integer academicYear, String semester);
    
    List<Enrollment> findByStatus(Enrollment.EnrollmentStatus status);
    
    List<Enrollment> findByAcademicYearAndSemester(Integer academicYear, String semester);
    
    List<Enrollment> findByStudentIdAndAcademicYearAndSemester(String studentId, Integer academicYear, String semester);
    
    List<Enrollment> findByCourseCodeAndAcademicYearAndSemester(String courseCode, Integer academicYear, String semester);
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'ENROLLED'")
    List<Enrollment> findActiveEnrollmentsByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.courseCode = :courseCode AND e.status = 'ENROLLED'")
    List<Enrollment> findActiveEnrollmentsByCourse(@Param("courseCode") String courseCode);
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollmentsByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.courseCode = :courseCode AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollmentsByCourse(@Param("courseCode") String courseCode);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.courseCode = :courseCode AND e.status = 'ENROLLED'")
    Long countActiveEnrollmentsByCourse(@Param("courseCode") String courseCode);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'ENROLLED'")
    Long countActiveEnrollmentsByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT AVG(e.finalGrade) FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'COMPLETED' AND e.finalGrade IS NOT NULL")
    Double getAverageGradeByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT AVG(e.finalGrade) FROM Enrollment e WHERE e.courseCode = :courseCode AND e.status = 'COMPLETED' AND e.finalGrade IS NOT NULL")
    Double getAverageGradeByCourse(@Param("courseCode") String courseCode);
    
    @Query("SELECT SUM(e.creditsEarned) FROM Enrollment e WHERE e.studentId = :studentId AND e.status = 'COMPLETED' AND e.creditsEarned IS NOT NULL")
    Integer getTotalCreditsByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.finalGrade >= :minGrade AND e.status = 'COMPLETED'")
    List<Enrollment> findByFinalGradeGreaterThanEqual(@Param("minGrade") Double minGrade);
    
    @Query("SELECT e FROM Enrollment e WHERE e.finalGrade < :maxGrade AND e.status = 'COMPLETED'")
    List<Enrollment> findByFinalGradeLessThan(@Param("maxGrade") Double maxGrade);
    
    @Query("SELECT e FROM Enrollment e WHERE e.instructorId = :instructorId")
    List<Enrollment> findByInstructorId(@Param("instructorId") String instructorId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.isAudit = true")
    List<Enrollment> findAuditEnrollments();
    
    @Query("SELECT e FROM Enrollment e WHERE e.isPassFail = true")
    List<Enrollment> findPassFailEnrollments();
    
    boolean existsByStudentIdAndCourseCodeAndAcademicYearAndSemester(
            String studentId, String courseCode, Integer academicYear, String semester);
    
    @Query("SELECT e FROM Enrollment e WHERE e.studentId = :studentId AND e.courseCode = :courseCode")
    List<Enrollment> findByStudentIdAndCourseCode(@Param("studentId") String studentId, @Param("courseCode") String courseCode);
}
