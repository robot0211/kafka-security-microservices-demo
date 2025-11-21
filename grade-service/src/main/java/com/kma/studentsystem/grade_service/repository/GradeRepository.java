package com.kma.studentsystem.grade_service.repository;

import com.kma.studentsystem.grade_service.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    List<Grade> findByStudentId(String studentId);
    
    List<Grade> findByCourseCode(String courseCode);
    
    List<Grade> findByStudentIdAndCourseCode(String studentId, String courseCode);
    
    List<Grade> findByStatus(Grade.GradeStatus status);
    
    List<Grade> findByGradeType(Grade.GradeType gradeType);
    
    List<Grade> findByAcademicYearAndSemester(Integer academicYear, String semester);
    
    List<Grade> findByStudentIdAndAcademicYearAndSemester(String studentId, Integer academicYear, String semester);
    
    List<Grade> findByCourseCodeAndAcademicYearAndSemester(String courseCode, Integer academicYear, String semester);
    
    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId AND g.courseCode = :courseCode AND g.isFinalGrade = true")
    Optional<Grade> findFinalGradeByStudentAndCourse(@Param("studentId") String studentId, @Param("courseCode") String courseCode);
    
    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId AND g.courseCode = :courseCode AND g.gradeType = :gradeType")
    List<Grade> findByStudentAndCourseAndType(@Param("studentId") String studentId, 
                                            @Param("courseCode") String courseCode, 
                                            @Param("gradeType") Grade.GradeType gradeType);
    
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.studentId = :studentId AND g.courseCode = :courseCode AND g.status = 'GRADED'")
    BigDecimal getAverageGradeByStudentAndCourse(@Param("studentId") String studentId, @Param("courseCode") String courseCode);
    
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.studentId = :studentId AND g.academicYear = :academicYear AND g.semester = :semester AND g.status = 'GRADED'")
    BigDecimal getAverageGradeByStudentAndSemester(@Param("studentId") String studentId, 
                                                 @Param("academicYear") Integer academicYear, 
                                                 @Param("semester") String semester);
    
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.courseCode = :courseCode AND g.status = 'GRADED'")
    BigDecimal getAverageGradeByCourse(@Param("courseCode") String courseCode);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.courseCode = :courseCode AND g.status = 'GRADED'")
    Long countGradedStudentsByCourse(@Param("courseCode") String courseCode);
    
    @Query("SELECT COUNT(g) FROM Grade g WHERE g.studentId = :studentId AND g.status = 'GRADED'")
    Long countGradedCoursesByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT g FROM Grade g WHERE g.gradeValue >= :minGrade AND g.status = 'GRADED'")
    List<Grade> findByGradeValueGreaterThanEqual(@Param("minGrade") BigDecimal minGrade);
    
    @Query("SELECT g FROM Grade g WHERE g.gradeValue < :maxGrade AND g.status = 'GRADED'")
    List<Grade> findByGradeValueLessThan(@Param("maxGrade") BigDecimal maxGrade);
    
    @Query("SELECT g FROM Grade g WHERE g.instructorId = :instructorId")
    List<Grade> findByInstructorId(@Param("instructorId") String instructorId);
    
    @Query("SELECT g FROM Grade g WHERE g.dueDate < CURRENT_TIMESTAMP AND g.status = 'PENDING'")
    List<Grade> findOverdueGrades();
    
    @Query("SELECT g FROM Grade g WHERE g.isFinalGrade = true AND g.status = 'GRADED'")
    List<Grade> findFinalGrades();
    
    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId AND g.isFinalGrade = true AND g.status = 'GRADED'")
    List<Grade> findFinalGradesByStudent(@Param("studentId") String studentId);
    
    boolean existsByStudentIdAndCourseCodeAndGradeType(String studentId, String courseCode, Grade.GradeType gradeType);
}
