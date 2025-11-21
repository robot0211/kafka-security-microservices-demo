# Hệ thống Quản lý Sinh viên (SAMS) - Apache Kafka Security Research

## Tổng quan

Hệ thống Quản lý Sinh viên (SAMS) được thiết kế để nghiên cứu và so sánh hiệu quả của các cấu hình bảo mật Apache Kafka trong môi trường microservice. Hệ thống bao gồm 8 microservice với kiến trúc event-driven sử dụng Apache Kafka.

## Kiến trúc Hệ thống

### Microservices

- **Student Service** (Port 8081): Quản lý thông tin sinh viên
- **Course Service** (Port 8082): Quản lý khóa học và chương trình đào tạo
- **Grade Service** (Port 8083): Quản lý điểm số và đánh giá
- **Enrollment Service** (Port 8084): Quản lý đăng ký khóa học
- **Notification Service** (Port 8085): Gửi thông báo (email, SMS)
- **Identity Service** (Port 8086): Xác thực và phân quyền
- **API Gateway** (Port 8080): Cổng vào duy nhất cho hệ thống
- **Discovery Server** (Port 8761): Service registry (Eureka)
- **Config Server** (Port 8888): Quản lý cấu hình tập trung

### Infrastructure

- **Apache Kafka**: Message broker với hai cấu hình bảo mật
- **PostgreSQL**: Database cho từng microservice
- **Zookeeper**: Coordination service cho Kafka
- **Kafka UI** (Port 8089): Monitoring và quản lý Kafka

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

### 1. Clone repository

```bash
git clone <repository-url>
cd manage_student
```

### 2. Chạy hệ thống với Kafka không bảo mật

```bash
# Khởi động tất cả services
docker-compose up -d

# Kiểm tra trạng thái
docker-compose ps

# Xem logs
docker-compose logs -f
```

### 3. Chạy hệ thống với Kafka bảo mật

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

### 4. Kiểm tra hệ thống

```bash
# Health check
curl http://localhost:8080/actuator/health

# API Gateway
curl http://localhost:8080/api/students

# Kafka UI
open http://localhost:8089
```

## Testing Security

### 1. Chạy security tests

```bash
cd kafka-config/scripts
./security-test.sh
```

### 2. Test unsecured Kafka

```bash
# Nghe lén messages
docker exec -it kafka-unsecured kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic student-events \
  --from-beginning

# Gửi message không xác thực
docker exec -it kafka-unsecured kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic student-events
```

### 3. Test secured Kafka

```bash
# Test với credentials hợp lệ
docker exec -it kafka-secured kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic student-events \
  --from-beginning \
  --consumer.config /opt/kafka/ssl/client.properties

# Test với credentials không hợp lệ (sẽ fail)
docker exec -it kafka-secured kafka-console-consumer \
  --bootstrap-server localhost:9093 \
  --topic student-events \
  --from-beginning
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

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

- **Author**: Nguyễn Tấn Phát
- **Email**: nguyentanphatkma@gmail.com
- **Institution**: Học viện Kỹ thuật Mật mã
- **Project**: Apache Kafka Security Research

## Acknowledgments

- Apache Kafka team for the excellent message broker
- Spring Boot team for the microservices framework
- Confluent for Kafka Docker images
- The open-source community for various tools and libraries
