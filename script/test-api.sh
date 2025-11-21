#!/bin/bash

# API Test Script for Student Management System
# This script tests all APIs for each service

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost"
STUDENT_PORT=8081
COURSE_PORT=8082
GRADE_PORT=8083
ENROLLMENT_PORT=8084
NOTIFICATION_PORT=8085
IDENTITY_PORT=8086

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test results array
declare -a TEST_RESULTS

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO")
            echo -e "${BLUE}[INFO]${NC} $message"
            ;;
        "SUCCESS")
            echo -e "${GREEN}[SUCCESS]${NC} $message"
            ;;
        "WARNING")
            echo -e "${YELLOW}[WARNING]${NC} $message"
            ;;
        "ERROR")
            echo -e "${RED}[ERROR]${NC} $message"
            ;;
    esac
}

# Function to test HTTP endpoint
test_endpoint() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    local test_name=$5
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    print_status "INFO" "Testing: $test_name"
    print_status "INFO" "URL: $method $url"
    
    # Make HTTP request
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
    fi
    
    # Extract status code and body
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    # Check if request was successful
    if [ "$status_code" = "$expected_status" ]; then
        print_status "SUCCESS" "✓ $test_name - Status: $status_code"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        TEST_RESULTS+=("PASS: $test_name")
    else
        print_status "ERROR" "✗ $test_name - Expected: $expected_status, Got: $status_code"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        TEST_RESULTS+=("FAIL: $test_name (Expected: $expected_status, Got: $status_code)")
        if [ -n "$body" ]; then
            print_status "ERROR" "Response: $body"
        fi
    fi
    
    echo "----------------------------------------"
}

# Function to check if service is running
check_service() {
    local port=$1
    local service_name=$2
    
    print_status "INFO" "Checking if $service_name is running on port $port..."
    
    if curl -s --connect-timeout 5 "$BASE_URL:$port" > /dev/null 2>&1; then
        print_status "SUCCESS" "$service_name is running on port $port"
        return 0
    else
        print_status "ERROR" "$service_name is not running on port $port"
        return 1
    fi
}

# Function to test Student Service
test_student_service() {
    print_status "INFO" "=== Testing Student Service ==="
    
    # Health check
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/health" "" "200" "Student Service Health Check"
    
    # Create student
    student_data='{
        "studentId": "SV001",
        "firstName": "Nguyen Van",
        "lastName": "An",
        "email": "an.nguyen@example.com",
        "phoneNumber": "0123456789",
        "dateOfBirth": "2000-01-01",
        "gender": "MALE",
        "address": "Ha Noi",
        "major": "Cong nghe thong tin",
        "status": "ACTIVE",
        "gpa": 3.5,
        "enrollmentYear": 2024
    }'
    test_endpoint "POST" "$BASE_URL:$STUDENT_PORT/api/students" "$student_data" "201" "Create Student"
    
    # Get all students
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students" "" "200" "Get All Students"
    
    # Get student by ID
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/1" "" "200" "Get Student by ID"
    
    # Get student by student ID
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/student-id/SV001" "" "200" "Get Student by Student ID"
    
    # Update student
    updated_student_data='{
        "studentId": "SV001",
        "firstName": "Nguyen Van",
        "lastName": "An",
        "email": "an.nguyen.updated@example.com",
        "phoneNumber": "0123456789",
        "dateOfBirth": "2000-01-01",
        "gender": "MALE",
        "address": "TP.HCM",
        "major": "Cong nghe thong tin",
        "status": "ACTIVE",
        "gpa": 3.7,
        "enrollmentYear": 2024
    }'
    test_endpoint "PUT" "$BASE_URL:$STUDENT_PORT/api/students/1" "$updated_student_data" "200" "Update Student"
    
    # Get students by status
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/status/ACTIVE" "" "200" "Get Students by Status"
    
    # Get students by major
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/major/Công nghệ thông tin" "" "200" "Get Students by Major"
    
    # Search students by name
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/search?name=An" "" "200" "Search Students by Name"
    
    # Get active student count
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/stats/active-count" "" "200" "Get Active Student Count"
    
    # Get average GPA
    test_endpoint "GET" "$BASE_URL:$STUDENT_PORT/api/students/stats/average-gpa" "" "200" "Get Average GPA"
}

# Function to test Course Service
test_course_service() {
    print_status "INFO" "=== Testing Course Service ==="
    
    # Health check
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/health" "" "200" "Course Service Health Check"
    
    # Create course
    course_data='{
        "courseCode": "CS101",
        "courseName": "Lap trinh co ban",
        "description": "Khoa hoc lap trinh co ban",
        "credits": 3,
        "department": "Khoa Cong nghe thong tin",
        "level": "UNDERGRADUATE",
        "status": "ACTIVE",
        "capacity": 50,
        "enrolledCount": 0,
        "academicYear": 2024,
        "semester": "SPRING"
    }'
    test_endpoint "POST" "$BASE_URL:$COURSE_PORT/api/courses" "$course_data" "201" "Create Course"
    
    # Get all courses
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses" "" "200" "Get All Courses"
    
    # Get course by ID
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/1" "" "200" "Get Course by ID"
    
    # Get course by code
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/code/CS101" "" "200" "Get Course by Code"
    
    # Update course
    updated_course_data='{
        "courseCode": "CS101",
        "courseName": "Lap trinh co ban - Cap nhat",
        "description": "Khoa hoc lap trinh co ban da cap nhat",
        "credits": 4,
        "department": "Khoa Cong nghe thong tin",
        "level": "UNDERGRADUATE",
        "status": "ACTIVE",
        "capacity": 60,
        "enrolledCount": 5,
        "academicYear": 2024,
        "semester": "SPRING"
    }'
    test_endpoint "PUT" "$BASE_URL:$COURSE_PORT/api/courses/1" "$updated_course_data" "200" "Update Course"
    
    # Get courses by status
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/status/ACTIVE" "" "200" "Get Courses by Status"
    
    # Get courses by department
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/department/Khoa Công nghệ thông tin" "" "200" "Get Courses by Department"
    
    # Get available courses
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/available" "" "200" "Get Available Courses"
    
    # Search courses
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/search?q=lập trình" "" "200" "Search Courses"
    
    # Update enrollment count
    test_endpoint "PUT" "$BASE_URL:$COURSE_PORT/api/courses/CS101/enrollment?delta=1" "" "200" "Update Enrollment Count"
    
    # Get active course count
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/stats/active-count" "" "200" "Get Active Course Count"
    
    # Get total enrolled students
    test_endpoint "GET" "$BASE_URL:$COURSE_PORT/api/courses/stats/total-enrolled" "" "200" "Get Total Enrolled Students"
}

# Function to test Grade Service
# test_grade_service() {
#     print_status "INFO" "=== Testing Grade Service ==="
    
#     # Health check
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/health" "" "200" "Grade Service Health Check"
    
#     # Create grade
#     grade_data='{
#         "studentId": "SV001",
#         "courseCode": "CS101",
#         "gradeValue": 8.5,
#         "gradeType": "ASSIGNMENT",
#         "description": "Bai tap tot",
#         "academicYear": 2024,
#         "semester": "SPRING",
#         "status": "PENDING",
#         "isFinalGrade": false
#     }'
#     test_endpoint "POST" "$BASE_URL:$GRADE_PORT/api/grades" "$grade_data" "201" "Create Grade"
    
#     # Get all grades
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades" "" "200" "Get All Grades"
    
#     # Get grade by ID
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/1" "" "200" "Get Grade by ID"
    
#     # Get grades by student
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/student/SV001" "" "200" "Get Grades by Student"
    
#     # Get grades by course
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/course/CS101" "" "200" "Get Grades by Course"
    
#     # Get grades by student and course
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/student/SV001/course/CS101" "" "200" "Get Grades by Student and Course"
    
#     # Get final grade
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/student/SV001/course/CS101/final" "" "200" "Get Final Grade"
    
#     # Update grade
#     updated_grade_data='{
#         "studentId": "SV001",
#         "courseCode": "CS101",
#         "gradeValue": 9.0,
#         "gradeType": "ASSIGNMENT",
#         "description": "Bai tap xuat sac",
#         "academicYear": 2024,
#         "semester": "SPRING",
#         "status": "GRADED",
#         "isFinalGrade": false
#     }'
#     test_endpoint "PUT" "$BASE_URL:$GRADE_PORT/api/grades/1" "$updated_grade_data" "200" "Update Grade"
    
#     # Finalize grade
#     test_endpoint "PUT" "$BASE_URL:$GRADE_PORT/api/grades/1/finalize" "" "200" "Finalize Grade"
    
#     # Get average grade by student and course
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/student/SV001/course/CS101/average" "" "200" "Get Average Grade by Student and Course"
    
#     # Get average grade by semester
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/student/SV001/semester/2024/SPRING/average" "" "200" "Get Average Grade by Semester"
    
#     # Get overdue grades
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/overdue" "" "200" "Get Overdue Grades"
    
#     # Count graded students by course
#     test_endpoint "GET" "$BASE_URL:$GRADE_PORT/api/grades/course/CS101/graded-count" "" "200" "Count Graded Students by Course"
# }

# Function to test Enrollment Service
# test_enrollment_service() {
#     print_status "INFO" "=== Testing Enrollment Service ==="
    
#     # Health check
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/health" "" "200" "Enrollment Service Health Check"
    
#     # Create enrollment
#     enrollment_data='{
#         "studentId": "SV001",
#         "courseCode": "CS101",
#         "academicYear": 2024,
#         "semester": "SPRING",
#         "status": "PENDING",
#         "enrollmentDate": "2024-01-15T10:00:00"
#     }'
#     test_endpoint "POST" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments" "$enrollment_data" "201" "Create Enrollment"
    
#     # Get all enrollments
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments" "" "200" "Get All Enrollments"
    
#     # Get enrollment by ID
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/1" "" "200" "Get Enrollment by ID"
    
#     # Update enrollment
#     updated_enrollment_data='{
#         "studentId": "SV001",
#         "courseCode": "CS101",
#         "academicYear": 2024,
#         "semester": "SPRING",
#         "status": "ENROLLED",
#         "enrollmentDate": "2024-01-15T10:00:00"
#     }'
#     test_endpoint "PUT" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/1" "$updated_enrollment_data" "200" "Update Enrollment"
    
#     # Approve enrollment
#     test_endpoint "POST" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/1/approve" "" "200" "Approve Enrollment"
    
#     # Complete enrollment
#     test_endpoint "POST" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/1/complete?finalGrade=85.5" "" "200" "Complete Enrollment"
    
#     # Get enrollments by student
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/student/SV001" "" "200" "Get Enrollments by Student"
    
#     # Get enrollments by course
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/course/CS101" "" "200" "Get Enrollments by Course"
    
#     # Get active enrollments by student
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/student/SV001/active" "" "200" "Get Active Enrollments by Student"
    
#     # Get completed enrollments by student
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/student/SV001/completed" "" "200" "Get Completed Enrollments by Student"
    
#     # Count active enrollments by course
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/course/CS101/count" "" "200" "Count Active Enrollments by Course"
    
#     # Get average grade by student
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/student/SV001/average-grade" "" "200" "Get Average Grade by Student"
    
#     # Get total credits by student
#     test_endpoint "GET" "$BASE_URL:$ENROLLMENT_PORT/api/enrollments/student/SV001/total-credits" "" "200" "Get Total Credits by Student"
# }

# Function to test Notification Service
# test_notification_service() {
#     print_status "INFO" "=== Testing Notification Service ==="
    
#     # Health check
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/health" "" "200" "Notification Service Health Check"
    
#     # Create notification
#     notification_data='{
#         "recipientId": "SV001",
#         "recipientType": "STUDENT",
#         "title": "Thong bao diem so",
#         "message": "Diem so cua ban da duoc cap nhat",
#         "type": "GRADE_UPDATE",
#         "priority": "MEDIUM",
#         "status": "PENDING"
#     }'
#     test_endpoint "POST" "$BASE_URL:$NOTIFICATION_PORT/api/notifications" "$notification_data" "201" "Create Notification"
    
#     # Get all notifications
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications" "" "200" "Get All Notifications"
    
#     # Get notification by ID
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/1" "" "200" "Get Notification by ID"
    
#     # Mark as read
#     test_endpoint "POST" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/1/read" "" "200" "Mark Notification as Read"
    
#     # Mark as delivered
#     test_endpoint "POST" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/1/delivered" "" "200" "Mark Notification as Delivered"
    
#     # Get notifications by recipient
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/recipient/SV001" "" "200" "Get Notifications by Recipient"
    
#     # Get pending notifications by recipient
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/recipient/SV001/pending" "" "200" "Get Pending Notifications by Recipient"
    
#     # Get unread notifications by recipient
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/recipient/SV001/unread" "" "200" "Get Unread Notifications by Recipient"
    
#     # Count pending notifications by recipient
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/recipient/SV001/pending/count" "" "200" "Count Pending Notifications by Recipient"
    
#     # Count unread notifications by recipient
#     test_endpoint "GET" "$BASE_URL:$NOTIFICATION_PORT/api/notifications/recipient/SV001/unread/count" "" "200" "Count Unread Notifications by Recipient"
# }

# Function to test Identity Service
# test_identity_service() {
#     print_status "INFO" "=== Testing Identity Service ==="
    
#     # Health check
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/health" "" "200" "Identity Service Health Check"
    
#     # Create user
#     user_data='{
#         "username": "testuser",
#         "email": "testuser@example.com",
#         "password": "password123",
#         "firstName": "Test",
#         "lastName": "User",
#         "role": "STUDENT",
#         "status": "ACTIVE"
#     }'
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users" "$user_data" "201" "Create User"
    
#     # Get all users
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users" "" "200" "Get All Users"
    
#     # Get user by ID
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/1" "" "200" "Get User by ID"
    
#     # Get user by username
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/username/testuser" "" "200" "Get User by Username"
    
#     # Get user by email
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/email/testuser@example.com" "" "200" "Get User by Email"
    
#     # Update user
#     updated_user_data='{
#         "username": "testuser",
#         "email": "testuser.updated@example.com",
#         "firstName": "Test",
#         "lastName": "User Updated",
#         "role": "STUDENT",
#         "status": "ACTIVE"
#     }'
#     test_endpoint "PUT" "$BASE_URL:$IDENTITY_PORT/api/users/1" "$updated_user_data" "200" "Update User"
    
#     # Change password
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/1/change-password?oldPassword=password123&newPassword=newpassword123" "" "200" "Change Password"
    
#     # Reset password
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/reset-password?email=testuser@example.com" "" "200" "Reset Password"
    
#     # Lock user
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/1/lock?reason=Test lock" "" "200" "Lock User"
    
#     # Unlock user
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/1/unlock" "" "200" "Unlock User"
    
#     # Change role
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/1/change-role?newRole=ADMIN" "" "200" "Change Role"
    
#     # Login
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/login?usernameOrEmail=testuser&password=password123" "" "200" "User Login"
    
#     # Logout
#     test_endpoint "POST" "$BASE_URL:$IDENTITY_PORT/api/users/logout?username=testuser" "" "200" "User Logout"
    
#     # Get users by role
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/role/STUDENT" "" "200" "Get Users by Role"
    
#     # Get users by status
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/status/ACTIVE" "" "200" "Get Users by Status"
    
#     # Count users by role
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/role/STUDENT/count" "" "200" "Count Users by Role"
    
#     # Count users by status
#     test_endpoint "GET" "$BASE_URL:$IDENTITY_PORT/api/users/status/ACTIVE/count" "" "200" "Count Users by Status"
# }

# Function to print test summary
print_summary() {
    echo ""
    print_status "INFO" "=== TEST SUMMARY ==="
    print_status "INFO" "Total Tests: $TOTAL_TESTS"
    print_status "SUCCESS" "Passed: $PASSED_TESTS"
    print_status "ERROR" "Failed: $FAILED_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        print_status "SUCCESS" "All tests passed! 🎉"
    else
        print_status "WARNING" "Some tests failed. Check the details above."
    fi
    
    echo ""
    print_status "INFO" "=== DETAILED RESULTS ==="
    for result in "${TEST_RESULTS[@]}"; do
        if [[ $result == PASS:* ]]; then
            echo -e "${GREEN}✓${NC} $result"
        else
            echo -e "${RED}✗${NC} $result"
        fi
    done
}

# Function to check all services
check_all_services() {
    print_status "INFO" "=== Checking Service Availability ==="
    
    local all_services_running=true
    
    check_service $STUDENT_PORT "Student Service" || all_services_running=false
    check_service $COURSE_PORT "Course Service" || all_services_running=false
    check_service $GRADE_PORT "Grade Service" || all_services_running=false
    check_service $ENROLLMENT_PORT "Enrollment Service" || all_services_running=false
    check_service $NOTIFICATION_PORT "Notification Service" || all_services_running=false
    check_service $IDENTITY_PORT "Identity Service" || all_services_running=false
    
    if [ "$all_services_running" = false ]; then
        print_status "ERROR" "Some services are not running. Please start all services before running tests."
        exit 1
    fi
    
    print_status "SUCCESS" "All services are running!"
    echo ""
}

# Main function
main() {
    echo "=========================================="
    echo "  Student Management System API Tests"
    echo "=========================================="
    echo ""
    
    # Check if curl is installed
    if ! command -v curl &> /dev/null; then
        print_status "ERROR" "curl is not installed. Please install curl to run this script."
        exit 1
    fi
    
    # Check all services
    #check_all_services
    
    # Run tests for each service
    test_student_service
    test_course_service
    # test_grade_service
    # test_enrollment_service
    # test_notification_service
    # test_identity_service
    
    # Print summary
    print_summary
    
    # Exit with appropriate code
    if [ $FAILED_TESTS -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# Run main function
main "$@"
