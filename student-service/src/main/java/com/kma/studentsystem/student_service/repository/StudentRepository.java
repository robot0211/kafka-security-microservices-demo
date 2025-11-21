package com.kma.studentsystem.student_service.repository;

import com.kma.studentsystem.student_service.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    Optional<Student> findByStudentId(String studentId);
    
    Optional<Student> findByEmail(String email);
    
    List<Student> findByStatus(Student.StudentStatus status);
    
    List<Student> findByMajor(String major);
    
    List<Student> findByEnrollmentYear(Integer enrollmentYear);
    
    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %:name% OR s.lastName LIKE %:name%")
    List<Student> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT s FROM Student s WHERE s.gpa >= :minGpa")
    List<Student> findByGpaGreaterThanEqual(@Param("minGpa") Double minGpa);
    
    @Query("SELECT s FROM Student s WHERE s.status = 'ACTIVE' AND s.major = :major")
    List<Student> findActiveStudentsByMajor(@Param("major") String major);
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.status = 'ACTIVE'")
    Long countActiveStudents();
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.major = :major AND s.status = 'ACTIVE'")
    Long countActiveStudentsByMajor(@Param("major") String major);
    
    @Query("SELECT AVG(s.gpa) FROM Student s WHERE s.status = 'ACTIVE' AND s.gpa IS NOT NULL")
    Double getAverageGpa();
    
    @Query("SELECT AVG(s.gpa) FROM Student s WHERE s.major = :major AND s.status = 'ACTIVE' AND s.gpa IS NOT NULL")
    Double getAverageGpaByMajor(@Param("major") String major);
    
    boolean existsByStudentId(String studentId);
    
    boolean existsByEmail(String email);
}
