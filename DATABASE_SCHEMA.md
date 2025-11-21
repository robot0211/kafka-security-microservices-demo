# Database Schema - Hệ thống SAMS

## Tổng quan

Hệ thống SAMS sử dụng kiến trúc **Database per Service** pattern, trong đó mỗi microservice có database riêng biệt. Điều này đảm bảo:

- **Data Isolation**: Dữ liệu của mỗi service được tách biệt
- **Independent Scaling**: Có thể scale database độc lập
- **Technology Diversity**: Có thể sử dụng công nghệ database khác nhau
- **Fault Isolation**: Lỗi database của một service không ảnh hưởng đến service khác

## Database Architecture

### 1. Database per Service Pattern

```
┌─────────────────────────────────────────────────────────────────┐
│                    Database Layer                                │
├─────────────────┬─────────────────┬─────────────────┬──────────┤
│  PostgreSQL     │  PostgreSQL     │  PostgreSQL     │  ...     │
│  (Student DB)   │  (Course DB)    │  (Grade DB)     │          │
│  Port: 5432     │  Port: 5433     │  Port: 5434     │          │
├─────────────────┼─────────────────┼─────────────────┼──────────┤
│  PostgreSQL     │  PostgreSQL     │  PostgreSQL     │          │
│  (Enrollment)   │  (Notification) │  (Identity)     │          │
│  Port: 5435     │  Port: 5436     │  Port: 5437     │          │
└─────────────────┴─────────────────┴─────────────────┴──────────┘
```

### 2. Database Mapping

| Service | Database | Port | Username | Password |
|---------|----------|------|----------|----------|
| Student Service | student_db | 5432 | student_user | student_password |
| Course Service | course_db | 5433 | course_user | course_password |
| Grade Service | grade_db | 5434 | grade_user | grade_password |
| Enrollment Service | enrollment_db | 5435 | enrollment_user | enrollment_password |
| Notification Service | notification_db | 5436 | notification_user | notification_password |
| Identity Service | identity_db | 5437 | identity_user | identity_password |

## Chi tiết Database Schema

### 1. Student Database (Port: 5432)

#### Students Table
```sql
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    address VARCHAR(255) NOT NULL,
    major VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED', 'DROPPED')),
    gpa DECIMAL(3,2) CHECK (gpa >= 0.0 AND gpa <= 10.0),
    enrollment_year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_students_student_id ON students(student_id);
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_students_major ON students(major);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_enrollment_year ON students(enrollment_year);
CREATE INDEX idx_students_created_at ON students(created_at);
```

#### Sample Data
```sql
INSERT INTO students (student_id, first_name, last_name, email, phone_number, date_of_birth, gender, address, major, status, gpa, enrollment_year) VALUES
('STU001', 'Nguyen', 'Van A', 'nguyenvana@kma.edu.vn', '0123456789', '2000-01-15', 'MALE', '123 Le Loi, Ha Noi', 'Computer Science', 'ACTIVE', 8.5, 2020),
('STU002', 'Tran', 'Thi B', 'tranthib@kma.edu.vn', '0987654321', '2001-03-20', 'FEMALE', '456 Nguyen Trai, Ho Chi Minh', 'Information Technology', 'ACTIVE', 9.0, 2021),
('STU003', 'Le', 'Van C', 'levanc@kma.edu.vn', '0369258147', '1999-12-10', 'MALE', '789 Tran Hung Dao, Da Nang', 'Cybersecurity', 'GRADUATED', 8.8, 2019);
```

---

### 2. Course Database (Port: 5433)

#### Courses Table
```sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    description TEXT,
    credits INTEGER NOT NULL CHECK (credits >= 1 AND credits <= 10),
    department VARCHAR(100) NOT NULL,
    level VARCHAR(20) NOT NULL CHECK (level IN ('UNDERGRADUATE', 'GRADUATE', 'DOCTORATE')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'CANCELLED', 'COMPLETED')),
    capacity INTEGER NOT NULL CHECK (capacity >= 1),
    enrolled_count INTEGER DEFAULT 0 CHECK (enrolled_count >= 0),
    academic_year INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    prerequisites TEXT,
    instructor_name VARCHAR(100),
    instructor_email VARCHAR(100),
    schedule VARCHAR(200),
    location VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_courses_course_code ON courses(course_code);
CREATE INDEX idx_courses_department ON courses(department);
CREATE INDEX idx_courses_level ON courses(level);
CREATE INDEX idx_courses_status ON courses(status);
CREATE INDEX idx_courses_academic_year ON courses(academic_year);
CREATE INDEX idx_courses_semester ON courses(semester);
CREATE INDEX idx_courses_instructor_name ON courses(instructor_name);
```

#### Sample Data
```sql
INSERT INTO courses (course_code, course_name, description, credits, department, level, status, capacity, enrolled_count, academic_year, semester, prerequisites, instructor_name, instructor_email, schedule, location) VALUES
('CS101', 'Introduction to Computer Science', 'Basic concepts of computer science', 3, 'Computer Science', 'UNDERGRADUATE', 'ACTIVE', 50, 25, 2024, 'Fall', NULL, 'Dr. Nguyen Van D', 'nguyenvand@kma.edu.vn', 'Mon-Wed-Fri 8:00-9:30', 'Room A101'),
('CS201', 'Data Structures and Algorithms', 'Advanced data structures and algorithms', 4, 'Computer Science', 'UNDERGRADUATE', 'ACTIVE', 40, 30, 2024, 'Fall', 'CS101', 'Dr. Tran Thi E', 'tranthie@kma.edu.vn', 'Tue-Thu 10:00-11:30', 'Room A102'),
('CS301', 'Database Systems', 'Database design and implementation', 3, 'Computer Science', 'UNDERGRADUATE', 'ACTIVE', 35, 20, 2024, 'Fall', 'CS201', 'Dr. Le Van F', 'levanf@kma.edu.vn', 'Mon-Wed 14:00-15:30', 'Room A103');
```

---

### 3. Grade Database (Port: 5434)

#### Grades Table
```sql
CREATE TABLE grades (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    grade_value DECIMAL(3,2) NOT NULL CHECK (grade_value >= 0.0 AND grade_value <= 10.0),
    grade_type VARCHAR(20) NOT NULL CHECK (grade_type IN ('ASSIGNMENT', 'QUIZ', 'MIDTERM', 'FINAL', 'PROJECT', 'PARTICIPATION', 'HOMEWORK', 'LAB')),
    description VARCHAR(200),
    academic_year INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'GRADED', 'APPROVED', 'REJECTED', 'APPEALED')),
    instructor_name VARCHAR(100),
    instructor_id VARCHAR(50),
    graded_at TIMESTAMP,
    due_date TIMESTAMP,
    submitted_at TIMESTAMP,
    comments TEXT,
    is_final_grade BOOLEAN DEFAULT FALSE,
    weight DECIMAL(3,2) CHECK (weight >= 0.0 AND weight <= 1.0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_grades_student_id ON grades(student_id);
CREATE INDEX idx_grades_course_code ON grades(course_code);
CREATE INDEX idx_grades_grade_type ON grades(grade_type);
CREATE INDEX idx_grades_status ON grades(status);
CREATE INDEX idx_grades_academic_year ON grades(academic_year);
CREATE INDEX idx_grades_semester ON grades(semester);
CREATE INDEX idx_grades_instructor_id ON grades(instructor_id);
```

#### Sample Data
```sql
INSERT INTO grades (student_id, course_code, grade_value, grade_type, description, academic_year, semester, status, instructor_name, instructor_id, graded_at, due_date, submitted_at, is_final_grade, weight) VALUES
('STU001', 'CS101', 8.5, 'FINAL', 'Final exam', 2024, 'Fall', 'GRADED', 'Dr. Nguyen Van D', 'INST001', '2024-12-15 10:00:00', '2024-12-10 23:59:59', '2024-12-10 20:30:00', TRUE, 0.4),
('STU001', 'CS101', 9.0, 'MIDTERM', 'Midterm exam', 2024, 'Fall', 'GRADED', 'Dr. Nguyen Van D', 'INST001', '2024-11-15 14:00:00', '2024-11-10 23:59:59', '2024-11-10 19:45:00', FALSE, 0.3),
('STU002', 'CS101', 9.5, 'FINAL', 'Final exam', 2024, 'Fall', 'GRADED', 'Dr. Nguyen Van D', 'INST001', '2024-12-15 10:00:00', '2024-12-10 23:59:59', '2024-12-10 21:15:00', TRUE, 0.4);
```

---

### 4. Enrollment Database (Port: 5435)

#### Enrollments Table
```sql
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    academic_year INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ENROLLED', 'COMPLETED', 'WITHDRAWN', 'FAILED', 'AUDIT')),
    enrollment_date TIMESTAMP,
    completion_date TIMESTAMP,
    withdrawal_date TIMESTAMP,
    final_grade DECIMAL(3,2) CHECK (final_grade >= 0.0 AND final_grade <= 10.0),
    letter_grade VARCHAR(5),
    credits_earned INTEGER,
    gpa_points DECIMAL(3,2),
    instructor_name VARCHAR(100),
    instructor_id VARCHAR(50),
    notes TEXT,
    is_audit BOOLEAN DEFAULT FALSE,
    is_pass_fail BOOLEAN DEFAULT FALSE,
    prerequisite_met BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_course_code ON enrollments(course_code);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_academic_year ON enrollments(academic_year);
CREATE INDEX idx_enrollments_semester ON enrollments(semester);
CREATE INDEX idx_enrollments_instructor_id ON enrollments(instructor_id);
```

#### Sample Data
```sql
INSERT INTO enrollments (student_id, course_code, academic_year, semester, status, enrollment_date, final_grade, letter_grade, credits_earned, gpa_points, instructor_name, instructor_id, is_audit, is_pass_fail, prerequisite_met) VALUES
('STU001', 'CS101', 2024, 'Fall', 'ENROLLED', '2024-08-15 09:00:00', NULL, NULL, NULL, NULL, 'Dr. Nguyen Van D', 'INST001', FALSE, FALSE, TRUE),
('STU002', 'CS101', 2024, 'Fall', 'ENROLLED', '2024-08-15 09:30:00', NULL, NULL, NULL, NULL, 'Dr. Nguyen Van D', 'INST001', FALSE, FALSE, TRUE),
('STU001', 'CS201', 2024, 'Fall', 'PENDING', '2024-08-20 10:00:00', NULL, NULL, NULL, NULL, 'Dr. Tran Thi E', 'INST002', FALSE, FALSE, TRUE);
```

---

### 5. Notification Database (Port: 5436)

#### Notifications Table
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id VARCHAR(50) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL CHECK (recipient_type IN ('STUDENT', 'INSTRUCTOR', 'ADMIN', 'SYSTEM')),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('ENROLLMENT_CONFIRMATION', 'ENROLLMENT_CANCELLATION', 'GRADE_UPDATE', 'COURSE_UPDATE', 'DEADLINE_REMINDER', 'SYSTEM_MAINTENANCE', 'SECURITY_ALERT', 'WELCOME', 'PASSWORD_RESET', 'ACCOUNT_ACTIVATION', 'GENERAL')),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'EXPIRED', 'CANCELLED')),
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    delivery_attempts INTEGER DEFAULT 0,
    max_delivery_attempts INTEGER DEFAULT 3,
    next_retry_at TIMESTAMP,
    expires_at TIMESTAMP,
    metadata TEXT,
    source_service VARCHAR(100),
    correlation_id VARCHAR(100),
    template_id VARCHAR(100),
    template_variables TEXT,
    channel VARCHAR(20) DEFAULT 'EMAIL' CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP', 'WEBHOOK')),
    external_id VARCHAR(200),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_recipient_type ON notifications(recipient_type);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_priority ON notifications(priority);
CREATE INDEX idx_notifications_channel ON notifications(channel);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

#### Sample Data
```sql
INSERT INTO notifications (recipient_id, recipient_type, title, message, type, priority, status, channel, source_service, correlation_id) VALUES
('STU001', 'STUDENT', 'Enrollment Confirmation', 'Your enrollment in CS101 has been confirmed.', 'ENROLLMENT_CONFIRMATION', 'MEDIUM', 'SENT', 'EMAIL', 'enrollment-service', 'ENR-001'),
('STU002', 'STUDENT', 'Grade Update', 'Your grade for CS101 has been updated.', 'GRADE_UPDATE', 'HIGH', 'DELIVERED', 'EMAIL', 'grade-service', 'GRD-001'),
('INST001', 'INSTRUCTOR', 'New Student Enrollment', 'A new student has enrolled in your course CS101.', 'COURSE_UPDATE', 'MEDIUM', 'PENDING', 'EMAIL', 'course-service', 'CRS-001');
```

---

### 6. Identity Database (Port: 5437)

#### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'INSTRUCTOR', 'ADMIN', 'SUPER_ADMIN')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_VERIFICATION', 'LOCKED')),
    phone_number VARCHAR(20),
    address TEXT,
    date_of_birth TIMESTAMP,
    profile_picture_url VARCHAR(500),
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    password_changed_at TIMESTAMP,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires_at TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    preferred_language VARCHAR(10) DEFAULT 'vi',
    timezone VARCHAR(50) DEFAULT 'Asia/Ho_Chi_Minh',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_last_login_at ON users(last_login_at);
CREATE INDEX idx_users_created_at ON users(created_at);
```

#### Sample Data
```sql
INSERT INTO users (username, email, password, first_name, last_name, role, status, phone_number, address, email_verified, preferred_language, timezone) VALUES
('admin', 'admin@kma.edu.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqQ8zJ8J8J8J8J8J8J8J8J8J8', 'System', 'Administrator', 'SUPER_ADMIN', 'ACTIVE', '0123456789', 'KMA Campus', TRUE, 'vi', 'Asia/Ho_Chi_Minh'),
('nguyenvana', 'nguyenvana@kma.edu.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqQ8zJ8J8J8J8J8J8J8J8J8J8', 'Nguyen', 'Van A', 'STUDENT', 'ACTIVE', '0123456789', '123 Le Loi, Ha Noi', TRUE, 'vi', 'Asia/Ho_Chi_Minh'),
('tranthib', 'tranthib@kma.edu.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqQ8zJ8J8J8J8J8J8J8J8J8J8', 'Tran', 'Thi B', 'STUDENT', 'ACTIVE', '0987654321', '456 Nguyen Trai, Ho Chi Minh', TRUE, 'vi', 'Asia/Ho_Chi_Minh'),
('nguyenvand', 'nguyenvand@kma.edu.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVqQ8zJ8J8J8J8J8J8J8J8J8J8', 'Nguyen', 'Van D', 'INSTRUCTOR', 'ACTIVE', '0369258147', '789 Tran Hung Dao, Da Nang', TRUE, 'vi', 'Asia/Ho_Chi_Minh');
```

## Database Initialization Scripts

### 1. Student Database Init Script
```sql
-- init-student.sql
CREATE DATABASE student_db;
\c student_db;

CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    address VARCHAR(255) NOT NULL,
    major VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED', 'SUSPENDED', 'DROPPED')),
    gpa DECIMAL(3,2) CHECK (gpa >= 0.0 AND gpa <= 10.0),
    enrollment_year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_students_student_id ON students(student_id);
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_students_major ON students(major);
CREATE INDEX idx_students_status ON students(status);
```

### 2. Course Database Init Script
```sql
-- init-course.sql
CREATE DATABASE course_db;
\c course_db;

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    description TEXT,
    credits INTEGER NOT NULL CHECK (credits >= 1 AND credits <= 10),
    department VARCHAR(100) NOT NULL,
    level VARCHAR(20) NOT NULL CHECK (level IN ('UNDERGRADUATE', 'GRADUATE', 'DOCTORATE')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'CANCELLED', 'COMPLETED')),
    capacity INTEGER NOT NULL CHECK (capacity >= 1),
    enrolled_count INTEGER DEFAULT 0 CHECK (enrolled_count >= 0),
    academic_year INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    prerequisites TEXT,
    instructor_name VARCHAR(100),
    instructor_email VARCHAR(100),
    schedule VARCHAR(200),
    location VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_courses_course_code ON courses(course_code);
CREATE INDEX idx_courses_department ON courses(department);
CREATE INDEX idx_courses_level ON courses(level);
CREATE INDEX idx_courses_status ON courses(status);
```

## Database Connection Configuration

### 1. Student Service Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres-student:5432/student_db
    username: student_user
    password: student_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### 2. Course Service Configuration
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres-course:5433/course_db
    username: course_user
    password: course_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

## Database Monitoring

### 1. Connection Pool Configuration
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

### 2. Health Check Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    diskspace:
      enabled: true
```

### 3. Database Metrics
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

## Data Consistency Patterns

### 1. Saga Pattern
```java
@Component
public class EnrollmentSaga {
    
    @SagaOrchestrationStart
    public void handle(EnrollmentRequested event) {
        // Step 1: Check student eligibility
        // Step 2: Check course availability
        // Step 3: Create enrollment
        // Step 4: Update course capacity
        // Step 5: Send notification
    }
}
```

### 2. Event Sourcing
```java
@Entity
public class StudentEvent {
    private Long id;
    private String studentId;
    private String eventType;
    private String eventData;
    private LocalDateTime timestamp;
    private String sourceService;
}
```

### 3. CQRS Pattern
```java
// Command Side
@Service
public class StudentCommandService {
    public void createStudent(CreateStudentCommand command) {
        // Handle command
    }
}

// Query Side
@Service
public class StudentQueryService {
    public StudentView getStudent(String studentId) {
        // Handle query
    }
}
```

## Database Security

### 1. Connection Security
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres-student:5432/student_db?ssl=true&sslmode=require
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 2. SQL Injection Prevention
```java
@Repository
public class StudentRepository extends JpaRepository<Student, Long> {
    
    @Query("SELECT s FROM Student s WHERE s.studentId = :studentId")
    Optional<Student> findByStudentId(@Param("studentId") String studentId);
    
    @Query("SELECT s FROM Student s WHERE s.major = :major AND s.status = :status")
    List<Student> findByMajorAndStatus(@Param("major") String major, @Param("status") StudentStatus status);
}
```

### 3. Data Encryption
```java
@Entity
public class Student {
    @Convert(converter = EncryptedStringConverter.class)
    private String phoneNumber;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String address;
}
```

## Performance Optimization

### 1. Indexing Strategy
```sql
-- Composite indexes for common queries
CREATE INDEX idx_students_major_status ON students(major, status);
CREATE INDEX idx_courses_department_level ON courses(department, level);
CREATE INDEX idx_grades_student_course ON grades(student_id, course_code);

-- Partial indexes for filtered queries
CREATE INDEX idx_students_active ON students(student_id) WHERE status = 'ACTIVE';
CREATE INDEX idx_courses_available ON courses(course_code) WHERE status = 'ACTIVE' AND enrolled_count < capacity;
```

### 2. Query Optimization
```java
@Repository
public class StudentRepository extends JpaRepository<Student, Long> {
    
    @Query("SELECT s FROM Student s WHERE s.major = :major AND s.status = :status ORDER BY s.createdAt DESC")
    Page<Student> findByMajorAndStatus(@Param("major") String major, 
                                      @Param("status") StudentStatus status, 
                                      Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE s.gpa >= :minGpa ORDER BY s.gpa DESC")
    List<Student> findTopStudents(@Param("minGpa") Double minGpa);
}
```

### 3. Caching Strategy
```java
@Service
@CacheConfig(cacheNames = "students")
public class StudentService {
    
    @Cacheable(key = "#studentId")
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }
    
    @CacheEvict(key = "#student.studentId")
    public Student updateStudent(Student student) {
        return studentRepository.save(student);
    }
}
```

## Backup and Recovery

### 1. Database Backup
```bash
#!/bin/bash
# backup-databases.sh

# Backup all databases
docker exec postgres-student pg_dump -U student_user student_db > backup/student_db_$(date +%Y%m%d_%H%M%S).sql
docker exec postgres-course pg_dump -U course_user course_db > backup/course_db_$(date +%Y%m%d_%H%M%S).sql
docker exec postgres-grade pg_dump -U grade_user grade_db > backup/grade_db_$(date +%Y%m%d_%H%M%S).sql
docker exec postgres-enrollment pg_dump -U enrollment_user enrollment_db > backup/enrollment_db_$(date +%Y%m%d_%H%M%S).sql
docker exec postgres-notification pg_dump -U notification_user notification_db > backup/notification_db_$(date +%Y%m%d_%H%M%S).sql
docker exec postgres-identity pg_dump -U identity_user identity_db > backup/identity_db_$(date +%Y%m%d_%H%M%S).sql
```

### 2. Database Recovery
```bash
#!/bin/bash
# restore-databases.sh

# Restore from backup
docker exec -i postgres-student psql -U student_user student_db < backup/student_db_20241201_120000.sql
docker exec -i postgres-course psql -U course_user course_db < backup/course_db_20241201_120000.sql
docker exec -i postgres-grade psql -U grade_user grade_db < backup/grade_db_20241201_120000.sql
docker exec -i postgres-enrollment psql -U enrollment_user enrollment_db < backup/enrollment_db_20241201_120000.sql
docker exec -i postgres-notification psql -U notification_user notification_db < backup/notification_db_20241201_120000.sql
docker exec -i postgres-identity psql -U identity_user identity_db < backup/identity_db_20241201_120000.sql
```

## Kết luận

Database schema của hệ thống SAMS được thiết kế theo kiến trúc **Database per Service** pattern, đảm bảo:

1. **Data Isolation**: Mỗi service có database riêng biệt
2. **Independent Scaling**: Có thể scale database độc lập
3. **Fault Tolerance**: Lỗi database của một service không ảnh hưởng đến service khác
4. **Technology Diversity**: Có thể sử dụng công nghệ database khác nhau
5. **Security**: Mỗi database có user và password riêng biệt

Hệ thống đã sẵn sàng để triển khai và sử dụng cho mục đích nghiên cứu, giáo dục và phát triển ứng dụng thực tế.
