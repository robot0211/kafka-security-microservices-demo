# Chi tiết từng Microservice - Hệ thống SAMS

## 1. Student Service (Port: 8081)

### Chức năng Chính

- **Quản lý thông tin sinh viên**: CRUD operations cho dữ liệu sinh viên
- **Tìm kiếm và lọc**: Theo tên, chuyên ngành, trạng thái
- **Thống kê**: Số lượng sinh viên, GPA trung bình
- **Event Publishing**: Phát sự kiện khi có thay đổi thông tin sinh viên

### Cấu trúc Dữ liệu

```java
@Entity
public class Student {
    private Long id;
    private String studentId;        // Mã sinh viên duy nhất
    private String firstName;         // Tên
    private String lastName;          // Họ
    private String email;             // Email
    private String phoneNumber;       // Số điện thoại
    private LocalDate dateOfBirth;    // Ngày sinh
    private Gender gender;            // Giới tính
    private String address;           // Địa chỉ
    private String major;             // Chuyên ngành
    private StudentStatus status;     // Trạng thái
    private Double gpa;               // Điểm GPA
    private Integer enrollmentYear;   // Năm nhập học
    private LocalDateTime createdAt;  // Thời gian tạo
    private LocalDateTime updatedAt;  // Thời gian cập nhật
}
```

### API Endpoints

- `GET /api/students` - Lấy danh sách tất cả sinh viên
- `POST /api/students` - Tạo sinh viên mới
- `GET /api/students/{id}` - Lấy thông tin sinh viên theo ID
- `PUT /api/students/{id}` - Cập nhật thông tin sinh viên
- `DELETE /api/students/{id}` - Xóa sinh viên
- `GET /api/students/status/{status}` - Lọc theo trạng thái
- `GET /api/students/major/{major}` - Lọc theo chuyên ngành
- `GET /api/students/search?name={name}` - Tìm kiếm theo tên
- `GET /api/students/stats/active-count` - Thống kê số sinh viên hoạt động
- `GET /api/students/stats/average-gpa` - Thống kê GPA trung bình

### Database Schema

```sql
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    major VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    gpa DECIMAL(3,2),
    enrollment_year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Kafka Events

- **Topic**: `student-events`
- **Event Types**: `STUDENT_CREATED`, `STUDENT_UPDATED`, `STUDENT_DELETED`, `STUDENT_STATUS_CHANGED`

---

## 2. Course Service (Port: 8082)

### Chức năng Chính

- **Quản lý khóa học**: CRUD operations cho thông tin khóa học
- **Quản lý lịch học**: Thời gian, địa điểm, giảng viên
- **Kiểm tra tiên quyết**: Xác định khóa học tiên quyết
- **Quản lý sức chứa**: Theo dõi số lượng đăng ký

### Cấu trúc Dữ liệu

```java
@Entity
public class Course {
    private Long id;
    private String courseCode;        // Mã khóa học
    private String courseName;        // Tên khóa học
    private String description;      // Mô tả
    private Integer credits;          // Số tín chỉ
    private String department;        // Khoa/Bộ môn
    private CourseLevel level;       // Cấp độ (Đại học, Thạc sĩ, Tiến sĩ)
    private CourseStatus status;     // Trạng thái
    private Integer capacity;         // Sức chứa
    private Integer enrolledCount;   // Số lượng đã đăng ký
    private Integer academicYear;     // Năm học
    private String semester;         // Học kỳ
    private String prerequisites;    // Điều kiện tiên quyết
    private String instructorName;    // Tên giảng viên
    private String instructorEmail;   // Email giảng viên
    private String schedule;         // Lịch học
    private String location;         // Địa điểm
}
```

### API Endpoints

- `GET /api/courses` - Lấy danh sách khóa học
- `POST /api/courses` - Tạo khóa học mới
- `GET /api/courses/{id}` - Lấy thông tin khóa học
- `PUT /api/courses/{id}` - Cập nhật khóa học
- `DELETE /api/courses/{id}` - Xóa khóa học
- `GET /api/courses/department/{department}` - Lọc theo khoa
- `GET /api/courses/level/{level}` - Lọc theo cấp độ
- `GET /api/courses/available` - Khóa học còn chỗ
- `GET /api/courses/instructor/{instructorName}` - Lọc theo giảng viên

### Database Schema

```sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(200) NOT NULL,
    description TEXT,
    credits INTEGER NOT NULL CHECK (credits >= 1 AND credits <= 10),
    department VARCHAR(100) NOT NULL,
    level VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    capacity INTEGER NOT NULL CHECK (capacity >= 1),
    enrolled_count INTEGER DEFAULT 0,
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
```

### Kafka Events

- **Topic**: `course-events`
- **Event Types**: `COURSE_CREATED`, `COURSE_UPDATED`, `COURSE_DELETED`, `COURSE_CAPACITY_CHANGED`

---

## 3. Grade Service (Port: 8083)

### Chức năng Chính

- **Quản lý điểm số**: CRUD operations cho điểm của sinh viên
- **Tính toán GPA**: Tự động tính điểm trung bình
- **Phân loại điểm**: Theo loại (Bài tập, Thi giữa kỳ, Thi cuối kỳ)
- **Theo dõi tiến độ**: Trạng thái chấm điểm

### Cấu trúc Dữ liệu

```java
@Entity
public class Grade {
    private Long id;
    private String studentId;        // Mã sinh viên
    private String courseCode;       // Mã khóa học
    private BigDecimal gradeValue;  // Điểm số (0.0 - 10.0)
    private GradeType gradeType;     // Loại điểm
    private String description;      // Mô tả
    private Integer academicYear;     // Năm học
    private String semester;         // Học kỳ
    private GradeStatus status;      // Trạng thái
    private String instructorName;   // Tên giảng viên
    private String instructorId;     // ID giảng viên
    private LocalDateTime gradedAt;  // Thời gian chấm điểm
    private LocalDateTime dueDate;   // Hạn nộp
    private LocalDateTime submittedAt; // Thời gian nộp
    private String comments;         // Nhận xét
    private Boolean isFinalGrade;   // Điểm cuối kỳ
    private BigDecimal weight;       // Trọng số
}
```

### API Endpoints

- `GET /api/grades` - Lấy danh sách điểm
- `POST /api/grades` - Tạo điểm mới
- `GET /api/grades/{id}` - Lấy thông tin điểm
- `PUT /api/grades/{id}` - Cập nhật điểm
- `DELETE /api/grades/{id}` - Xóa điểm
- `GET /api/grades/student/{studentId}` - Điểm của sinh viên
- `GET /api/grades/course/{courseCode}` - Điểm của khóa học
- `GET /api/grades/type/{gradeType}` - Lọc theo loại điểm
- `GET /api/grades/status/{status}` - Lọc theo trạng thái

### Database Schema

```sql
CREATE TABLE grades (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    grade_value DECIMAL(3,2) NOT NULL CHECK (grade_value >= 0.0 AND grade_value <= 10.0),
    grade_type VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    academic_year INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
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
```

### Kafka Events

- **Topic**: `grade-events`
- **Event Types**: `GRADE_CREATED`, `GRADE_UPDATED`, `GRADE_DELETED`, `GRADE_FINALIZED`

---

## 4. Enrollment Service (Port: 8084)

### Chức năng Chính

- **Quản lý đăng ký**: CRUD operations cho việc đăng ký khóa học
- **Workflow đăng ký**: Pending → Enrolled → Completed
- **Kiểm tra điều kiện**: Tiên quyết, sức chứa, thời gian
- **Tính toán tín chỉ**: Tổng số tín chỉ đã đăng ký

### Cấu trúc Dữ liệu

```java
@Entity
public class Enrollment {
    private Long id;
    private String studentId;        // Mã sinh viên
    private String courseCode;       // Mã khóa học
    private Integer academicYear;     // Năm học
    private String semester;         // Học kỳ
    private EnrollmentStatus status; // Trạng thái đăng ký
    private LocalDateTime enrollmentDate;    // Ngày đăng ký
    private LocalDateTime completionDate;   // Ngày hoàn thành
    private LocalDateTime withdrawalDate;   // Ngày rút khóa học
    private Double finalGrade;      // Điểm cuối kỳ
    private String letterGrade;      // Điểm chữ
    private Integer creditsEarned;   // Số tín chỉ đạt được
    private Double gpaPoints;        // Điểm GPA
    private String instructorName;   // Tên giảng viên
    private String instructorId;     // ID giảng viên
    private String notes;            // Ghi chú
    private Boolean isAudit;         // Học thử
    private Boolean isPassFail;      // Đậu/Rớt
    private Boolean prerequisiteMet; // Đã đạt điều kiện tiên quyết
}
```

### API Endpoints

- `GET /api/enrollments` - Lấy danh sách đăng ký
- `POST /api/enrollments` - Tạo đăng ký mới
- `GET /api/enrollments/{id}` - Lấy thông tin đăng ký
- `PUT /api/enrollments/{id}` - Cập nhật đăng ký
- `DELETE /api/enrollments/{id}` - Xóa đăng ký
- `GET /api/enrollments/student/{studentId}` - Đăng ký của sinh viên
- `GET /api/enrollments/course/{courseCode}` - Đăng ký của khóa học
- `GET /api/enrollments/status/{status}` - Lọc theo trạng thái
- `POST /api/enrollments/{id}/withdraw` - Rút khóa học

### Database Schema

```sql
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    academic_year INTEGER NOT NULL,
    semester VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    enrollment_date TIMESTAMP,
    completion_date TIMESTAMP,
    withdrawal_date TIMESTAMP,
    final_grade DECIMAL(3,2),
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
```

### Kafka Events

- **Topic**: `enrollment-events`
- **Event Types**: `ENROLLMENT_CREATED`, `ENROLLMENT_UPDATED`, `ENROLLMENT_WITHDRAWN`, `ENROLLMENT_COMPLETED`

---

## 5. Notification Service (Port: 8085)

### Chức năng Chính

- **Gửi thông báo đa kênh**: Email, SMS, Push notification
- **Template system**: Sử dụng template cho các loại thông báo
- **Retry mechanism**: Thử lại khi gửi thất bại
- **Delivery tracking**: Theo dõi trạng thái gửi

### Cấu trúc Dữ liệu

```java
@Entity
public class Notification {
    private Long id;
    private String recipientId;      // ID người nhận
    private RecipientType recipientType; // Loại người nhận
    private String title;           // Tiêu đề
    private String message;          // Nội dung
    private NotificationType type;   // Loại thông báo
    private Priority priority;       // Độ ưu tiên
    private NotificationStatus status; // Trạng thái
    private LocalDateTime sentAt;   // Thời gian gửi
    private LocalDateTime readAt;   // Thời gian đọc
    private Integer deliveryAttempts; // Số lần thử gửi
    private Integer maxDeliveryAttempts; // Số lần thử tối đa
    private LocalDateTime nextRetryAt; // Thời gian thử lại tiếp theo
    private LocalDateTime expiresAt; // Thời gian hết hạn
    private String metadata;         // Metadata
    private String sourceService;    // Service nguồn
    private String correlationId;   // ID tương quan
    private String templateId;       // ID template
    private String templateVariables; // Biến template
    private Channel channel;         // Kênh gửi
    private String externalId;      // ID bên ngoài
    private String errorMessage;     // Thông báo lỗi
}
```

### API Endpoints

- `GET /api/notifications` - Lấy danh sách thông báo
- `POST /api/notifications` - Tạo thông báo mới
- `GET /api/notifications/{id}` - Lấy thông tin thông báo
- `PUT /api/notifications/{id}` - Cập nhật thông báo
- `DELETE /api/notifications/{id}` - Xóa thông báo
- `GET /api/notifications/recipient/{recipientId}` - Thông báo của người dùng
- `GET /api/notifications/status/{status}` - Lọc theo trạng thái
- `POST /api/notifications/{id}/mark-read` - Đánh dấu đã đọc
- `POST /api/notifications/{id}/retry` - Thử gửi lại

### Database Schema

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id VARCHAR(50) NOT NULL,
    recipient_type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
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
    channel VARCHAR(20) DEFAULT 'EMAIL',
    external_id VARCHAR(200),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Kafka Events

- **Topic**: `notification-events`
- **Event Types**: `NOTIFICATION_CREATED`, `NOTIFICATION_SENT`, `NOTIFICATION_DELIVERED`, `NOTIFICATION_FAILED`

---

## 6. Identity Service (Port: 8086)

### Chức năng Chính

- **Quản lý người dùng**: CRUD operations cho tài khoản
- **Xác thực**: Login, logout, session management
- **Phân quyền**: Role-based access control (RBAC)
- **Bảo mật**: Password reset, 2FA, account lockout

### Cấu trúc Dữ liệu

```java
@Entity
public class User implements UserDetails {
    private Long id;
    private String username;         // Tên đăng nhập
    private String email;            // Email
    private String password;         // Mật khẩu (hashed)
    private String firstName;       // Tên
    private String lastName;        // Họ
    private UserRole role;           // Vai trò
    private UserStatus status;       // Trạng thái
    private String phoneNumber;      // Số điện thoại
    private String address;         // Địa chỉ
    private LocalDateTime dateOfBirth; // Ngày sinh
    private String profilePictureUrl; // URL ảnh đại diện
    private LocalDateTime lastLoginAt; // Lần đăng nhập cuối
    private String lastLoginIp;     // IP đăng nhập cuối
    private Integer failedLoginAttempts; // Số lần đăng nhập thất bại
    private LocalDateTime lockedUntil; // Khóa đến khi nào
    private LocalDateTime passwordChangedAt; // Thời gian đổi mật khẩu
    private Boolean emailVerified;  // Email đã xác thực
    private String emailVerificationToken; // Token xác thực email
    private String passwordResetToken; // Token reset mật khẩu
    private LocalDateTime passwordResetExpiresAt; // Hết hạn token reset
    private Boolean twoFactorEnabled; // Bật 2FA
    private String twoFactorSecret;  // Secret 2FA
    private String preferredLanguage; // Ngôn ngữ ưa thích
    private String timezone;        // Múi giờ
}
```

### API Endpoints

- `GET /api/users` - Lấy danh sách người dùng
- `POST /api/users` - Tạo người dùng mới
- `GET /api/users/{id}` - Lấy thông tin người dùng
- `PUT /api/users/{id}` - Cập nhật người dùng
- `DELETE /api/users/{id}` - Xóa người dùng
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/logout` - Đăng xuất
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/reset-password` - Reset mật khẩu
- `POST /api/auth/verify-email` - Xác thực email
- `GET /api/users/role/{role}` - Lọc theo vai trò
- `GET /api/users/status/{status}` - Lọc theo trạng thái

### Database Schema

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
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
```

### Kafka Events

- **Topic**: `identity-events`
- **Event Types**: `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `USER_LOGIN`, `USER_LOGOUT`, `PASSWORD_CHANGED`

---

## Infrastructure Services

### 7. API Gateway (Port: 8080)

#### Chức năng

- **Service Routing**: Định tuyến request đến service phù hợp
- **Load Balancing**: Phân tải giữa các instance của service
- **Authentication**: Xác thực người dùng
- **Authorization**: Phân quyền truy cập
- **Rate Limiting**: Giới hạn số request
- **Circuit Breaker**: Ngắt kết nối khi service lỗi
- **Request/Response Logging**: Ghi log request/response
- **CORS Configuration**: Cấu hình CORS

### 8. Eureka Discovery Server (Port: 8761)

#### Chức năng

- **Service Registration**: Đăng ký service
- **Service Discovery**: Tìm kiếm service
- **Health Monitoring**: Theo dõi sức khỏe service
- **Load Balancing**: Hỗ trợ load balancing
- **Dashboard UI**: Giao diện quản lý

### 9. Config Server (Port: 8888)

#### Chức năng

- **Centralized Configuration**: Quản lý cấu hình tập trung
- **Environment-specific Settings**: Cấu hình theo môi trường
- **Profile-based Configuration**: Cấu hình theo profile
- **Dynamic Configuration Updates**: Cập nhật cấu hình động
- **Git-based Storage**: Lưu trữ cấu hình trong Git

## Event-Driven Architecture

### Event Flow Pattern

```
Service A → Publish Event → Kafka Topic → Consume Event → Service B
```

### Event Types by Service

#### Student Service Events

- `STUDENT_CREATED` - Sinh viên được tạo
- `STUDENT_UPDATED` - Thông tin sinh viên được cập nhật
- `STUDENT_DELETED` - Sinh viên bị xóa
- `STUDENT_STATUS_CHANGED` - Trạng thái sinh viên thay đổi

#### Course Service Events

- `COURSE_CREATED` - Khóa học được tạo
- `COURSE_UPDATED` - Thông tin khóa học được cập nhật
- `COURSE_DELETED` - Khóa học bị xóa
- `COURSE_CAPACITY_CHANGED` - Sức chứa khóa học thay đổi

#### Grade Service Events

- `GRADE_CREATED` - Điểm được tạo
- `GRADE_UPDATED` - Điểm được cập nhật
- `GRADE_DELETED` - Điểm bị xóa
- `GRADE_FINALIZED` - Điểm được hoàn thiện

#### Enrollment Service Events

- `ENROLLMENT_CREATED` - Đăng ký được tạo
- `ENROLLMENT_UPDATED` - Đăng ký được cập nhật
- `ENROLLMENT_WITHDRAWN` - Đăng ký bị rút
- `ENROLLMENT_COMPLETED` - Đăng ký hoàn thành

#### Notification Service Events

- `NOTIFICATION_CREATED` - Thông báo được tạo
- `NOTIFICATION_SENT` - Thông báo được gửi
- `NOTIFICATION_DELIVERED` - Thông báo được giao
- `NOTIFICATION_FAILED` - Thông báo gửi thất bại

#### Identity Service Events

- `USER_CREATED` - Người dùng được tạo
- `USER_UPDATED` - Thông tin người dùng được cập nhật
- `USER_DELETED` - Người dùng bị xóa
- `USER_LOGIN` - Người dùng đăng nhập
- `USER_LOGOUT` - Người dùng đăng xuất
- `PASSWORD_CHANGED` - Mật khẩu được thay đổi

### Event Schema

```json
{
  "eventId": "uuid",
  "eventType": "string",
  "timestamp": "datetime",
  "sourceService": "string",
  "version": "string",
  "data": {
    "id": "string",
    "action": "string",
    "payload": "object"
  },
  "metadata": {
    "correlationId": "string",
    "causationId": "string",
    "userId": "string",
    "sessionId": "string"
  }
}
```
