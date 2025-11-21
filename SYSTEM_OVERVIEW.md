# Tổng quan Hệ thống Quản lý Sinh viên (SAMS)

## Giới thiệu

Hệ thống Quản lý Sinh viên (Student Administration Management System - SAMS) là một hệ thống microservice được thiết kế để nghiên cứu và so sánh hiệu quả của các cấu hình bảo mật Apache Kafka. Hệ thống được xây dựng với kiến trúc event-driven, sử dụng Spring Boot và Apache Kafka làm message broker.

## Mục tiêu Nghiên cứu

1. **Phân tích Bảo mật**: So sánh hiệu quả bảo mật giữa Kafka không bảo mật và bảo mật nâng cao
2. **Đánh giá Performance**: Đo lường tác động của bảo mật lên hiệu suất hệ thống
3. **Nghiên cứu Lỗ hổng**: Minh họa các lỗ hổng bảo mật và biện pháp phòng chống
4. **Thực hành DevOps**: Triển khai và quản lý hệ thống microservice

## Kiến trúc Tổng thể

### 1. Kiến trúc Microservice

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (8080)                      │
│                    - Routing & Load Balancing                  │
│                    - Authentication & Authorization            │
│                    - Rate Limiting & Circuit Breaker          │
└─────────────────────┬───────────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────────┐
│                    Service Layer                                 │
├─────────────────┬─────────────────┬─────────────────┬──────────┤
│  Student        │  Course         │  Grade          │  Enroll  │
│  Service        │  Service        │  Service        │  Service │
│  (8081)         │  (8082)         │  (8083)         │  (8084)  │
├─────────────────┼─────────────────┼─────────────────┼──────────┤
│  Notification   │  Identity       │  Discovery      │  Config  │
│  Service        │  Service        │  Server          │  Server  │
│  (8085)         │  (8086)         │  (8761)         │  (8888)  │
└─────────────────┴─────────────────┴─────────────────┴──────────┘
                      │
┌─────────────────────┴───────────────────────────────────────────┐
│                    Message Layer                                 │
├─────────────────────────────────────────────────────────────────┤
│                    Apache Kafka                                 │
│              - Unsecured (Port 9092)                           │
│              - Secured (Port 9093)                             │
└─────────────────────────────────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────────┐
│                    Data Layer                                   │
├─────────────────┬─────────────────┬─────────────────┬──────────┤
│  PostgreSQL     │  PostgreSQL     │  PostgreSQL     │  ...     │
│  (Student DB)   │  (Course DB)    │  (Grade DB)     │          │
│  Port: 5432     │  Port: 5433     │  Port: 5434     │          │
└─────────────────┴─────────────────┴─────────────────┴──────────┘
```

### 2. Luồng Dữ liệu (Data Flow)

```
Client Request → API Gateway → Service Discovery → Target Service
                     ↓
              Authentication & Authorization
                     ↓
              Business Logic Processing
                     ↓
              Database Operations
                     ↓
              Event Publishing (Kafka)
                     ↓
              Event Consumption (Other Services)
                     ↓
              Response to Client
```

## Danh sách Microservices

### Core Business Services

1. **Student Service (Port: 8081)** - Quản lý thông tin sinh viên
2. **Course Service (Port: 8082)** - Quản lý khóa học và chương trình đào tạo
3. **Grade Service (Port: 8083)** - Quản lý điểm số và đánh giá
4. **Enrollment Service (Port: 8084)** - Quản lý đăng ký khóa học
5. **Notification Service (Port: 8085)** - Gửi thông báo (email, SMS)
6. **Identity Service (Port: 8086)** - Xác thực và phân quyền

### Infrastructure Services

7. **API Gateway (Port: 8080)** - Cổng vào duy nhất cho hệ thống
8. **Discovery Server (Port: 8761)** - Service registry (Eureka)
9. **Config Server (Port: 8888)** - Quản lý cấu hình tập trung

### Message Infrastructure

- **Apache Kafka (Unsecured - Port: 9092)** - Message broker không bảo mật
- **Apache Kafka (Secured - Port: 9093)** - Message broker bảo mật nâng cao
- **Zookeeper (Port: 2181)** - Coordination service cho Kafka

### Database Infrastructure

- **PostgreSQL (Student DB - Port: 5432)** - Database cho Student Service
- **PostgreSQL (Course DB - Port: 5433)** - Database cho Course Service
- **PostgreSQL (Grade DB - Port: 5434)** - Database cho Grade Service
- **PostgreSQL (Enrollment DB - Port: 5435)** - Database cho Enrollment Service
- **PostgreSQL (Notification DB - Port: 5436)** - Database cho Notification Service
- **PostgreSQL (Identity DB - Port: 5437)** - Database cho Identity Service

### Monitoring & Management

- **Kafka UI (Port: 8089)** - Monitoring và quản lý Kafka

## Hai Kịch bản Kafka Security

### 1. Kafka Mặc định (Không bảo mật) - Port 9092

**Mục đích**: Demo các lỗ hổng bảo mật và tấn công

#### Đặc điểm:

- Không mã hóa dữ liệu (PLAINTEXT)
- Không xác thực client
- Không phân quyền (ACL)
- Dễ bị tấn công Man-in-the-Middle
- Có thể nghe lén và sửa đổi message

#### Lỗ hổng bảo mật:

- **Eavesdropping**: Nghe lén message giữa các service
- **Message injection**: Chèn message giả mạo vào topic
- **Topic hijacking**: Chiếm quyền điều khiển topic
- **Data corruption**: Làm hỏng dữ liệu trong topic

### 2. Kafka Bảo mật Nâng cao - Port 9093

**Mục đích**: Demo các biện pháp phòng chống và bảo mật

#### Đặc điểm:

- SSL/TLS encryption cho tất cả traffic
- SASL authentication (SCRAM-SHA-512)
- ACL authorization cho từng user/topic
- Certificate-based security
- Audit logging

#### Biện pháp bảo mật:

- **SSL/TLS Encryption**: Mã hóa toàn bộ traffic
- **SASL Authentication**: Xác thực client bằng username/password
- **ACL Authorization**: Phân quyền chi tiết cho từng user/topic
- **Certificate-based Security**: Sử dụng certificate cho server authentication
- **Audit Logging**: Ghi log tất cả hoạt động

## Cài đặt và Chạy

### Yêu cầu hệ thống

- Docker và Docker Compose
- Java 17+
- Maven 3.6+
- 8GB RAM (khuyến nghị)
- 20GB disk space

### 1. Chạy hệ thống với Kafka không bảo mật

```bash
# Khởi động tất cả services
docker-compose up -d

# Kiểm tra trạng thái
docker-compose ps

# Xem logs
docker-compose logs -f
```

### 2. Chạy hệ thống với Kafka bảo mật

```bash
# Tạo SSL certificates
cd kafka-config/scripts
./setup-ssl.sh

# Khởi động hệ thống bảo mật
docker-compose -f docker-compose.secured.yml up -d

# Thiết lập SASL users
docker exec -it kafka-secured /opt/kafka/scripts/setup-users.sh

# Thiết lập ACLs
docker exec -it kafka-secured /opt/kafka/scripts/setup-acls.sh
```

### 3. Kiểm tra hệ thống

```bash
# Health check
curl http://localhost:8080/actuator/health

# API Gateway
curl http://localhost:8080/api/students

# Kafka UI
open http://localhost:8089
```

## API Endpoints

### Student Service

- `GET /api/students` - Lấy danh sách sinh viên
- `POST /api/students` - Tạo sinh viên mới
- `GET /api/students/{id}` - Lấy thông tin sinh viên
- `PUT /api/students/{id}` - Cập nhật sinh viên
- `DELETE /api/students/{id}` - Xóa sinh viên

### Course Service

- `GET /api/courses` - Lấy danh sách khóa học
- `POST /api/courses` - Tạo khóa học mới
- `GET /api/courses/{id}` - Lấy thông tin khóa học
- `PUT /api/courses/{id}` - Cập nhật khóa học
- `DELETE /api/courses/{id}` - Xóa khóa học

### Grade Service

- `GET /api/grades` - Lấy danh sách điểm
- `POST /api/grades` - Tạo điểm mới
- `GET /api/grades/student/{studentId}` - Lấy điểm của sinh viên
- `PUT /api/grades/{id}` - Cập nhật điểm
- `DELETE /api/grades/{id}` - Xóa điểm

## Monitoring và Logging

### Health Checks

- Discovery Server: http://localhost:8761
- Config Server: http://localhost:8888/actuator/health
- Student Service: http://localhost:8081/actuator/health
- Course Service: http://localhost:8082/actuator/health
- Grade Service: http://localhost:8083/actuator/health
- API Gateway: http://localhost:8080/actuator/health

### Metrics

- Prometheus metrics: http://localhost:8081/actuator/prometheus
- Kafka UI: http://localhost:8089

### Logs

```bash
# Xem logs của tất cả services
docker-compose logs -f

# Xem logs của service cụ thể
docker-compose logs -f student-service
docker-compose logs -f kafka-unsecured
docker-compose logs -f kafka-secured
```

## Performance Analysis

### Unsecured Kafka

- **Latency**: ~2-5ms per message
- **Throughput**: ~100,000 messages/second
- **CPU Usage**: ~10-15%
- **Memory Usage**: ~512MB

### Secured Kafka

- **Latency**: ~5-10ms per message (SSL overhead)
- **Throughput**: ~60,000 messages/second (encryption overhead)
- **CPU Usage**: ~20-25% (encryption/decryption)
- **Memory Usage**: ~768MB (SSL buffers)

### Trade-offs

- **2-3x latency increase**
- **40% throughput reduction**
- **2x CPU usage increase**
- **50% memory usage increase**

## Security Best Practices

### 1. SSL/TLS Configuration

- Sử dụng certificate từ CA đáng tin cậy
- Rotate certificates định kỳ
- Sử dụng strong cipher suites
- Disable weak protocols (SSLv2, SSLv3)

### 2. SASL Authentication

- Sử dụng SCRAM-SHA-512 thay vì PLAIN
- Rotate passwords định kỳ
- Sử dụng strong passwords
- Implement password policies

### 3. ACL Authorization

- Principle of least privilege
- Regular audit of permissions
- Use resource-specific permissions
- Implement role-based access control

### 4. Monitoring và Auditing

- Log tất cả authentication attempts
- Monitor failed authorization attempts
- Track message flow và access patterns
- Set up alerts cho security events

## Troubleshooting

### Common Issues

#### 1. Kafka không khởi động

```bash
# Kiểm tra logs
docker-compose logs kafka-unsecured
docker-compose logs kafka-secured

# Kiểm tra disk space
df -h

# Restart services
docker-compose restart kafka-unsecured kafka-secured
```

#### 2. SSL certificate issues

```bash
# Regenerate certificates
cd kafka-config/scripts
./setup-ssl.sh

# Check certificate validity
keytool -list -v -keystore kafka-config/ssl/kafka.server.keystore.jks
```

#### 3. SASL authentication failures

```bash
# Check user configuration
docker exec -it kafka-secured kafka-configs \
  --zookeeper localhost:2181 \
  --describe \
  --entity-type users

# Recreate users
docker exec -it kafka-secured /opt/kafka/scripts/setup-users.sh
```

#### 4. ACL permission issues

```bash
# Check ACLs
docker exec -it kafka-secured kafka-acls \
  --authorizer-properties zookeeper.connect=localhost:2181 \
  --list

# Recreate ACLs
docker exec -it kafka-secured /opt/kafka/scripts/setup-acls.sh
```

## Tài liệu Tham khảo

- [Chi tiết từng Service](./SERVICES_DETAILED.md)
- [Cấu hình Kafka](./KAFKA_CONFIGURATION.md)
- [Database Schema](./DATABASE_SCHEMA.md)
- [Security Analysis](./SECURITY_ANALYSIS.md)
- [Performance Analysis](./PERFORMANCE_ANALYSIS.md)
- [Deployment Guide](./DEPLOYMENT_GUIDE.md)
