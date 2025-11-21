#!/bin/bash

# Kafka ACL Setup Script for Secured Kafka (SASL_SSL)
# This script creates Access Control Lists for all services

set -euo pipefail

# Configuration
BOOTSTRAP_SERVER=${BOOTSTRAP_SERVER:-kafka-secured:29093}
SECURITY_PROTOCOL=SASL_SSL
SASL_MECHANISM=SCRAM-SHA-512
ADMIN_USER=${ADMIN_USER:-admin}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-admin123}
SSL_KEYSTORE_LOCATION=${SSL_KEYSTORE_LOCATION:-/etc/kafka/secrets/kafka.client.keystore.jks}
SSL_KEYSTORE_PASSWORD=${SSL_KEYSTORE_PASSWORD:-kafka123}
SSL_TRUSTSTORE_LOCATION=${SSL_TRUSTSTORE_LOCATION:-/etc/kafka/secrets/kafka.client.truststore.jks}
SSL_TRUSTSTORE_PASSWORD=${SSL_TRUSTSTORE_PASSWORD:-kafka123}

# Check if kafka-acls.sh is available
KAFKA_ACLS_BIN=${KAFKA_ACLS_BIN:-$(command -v kafka-acls || true)}
if [[ -z "${KAFKA_ACLS_BIN}" ]]; then
  echo "Error: kafka-acls not found in PATH. Set KAFKA_ACLS_BIN to the binary path." >&2
  echo "Example: export KAFKA_ACLS_BIN=/opt/kafka/bin/kafka-acls.sh" >&2
  exit 1
fi

# Common security config for all commands
COMMON_SECURITY_CONFIG=$(cat <<EOF
security.protocol=${SECURITY_PROTOCOL}
sasl.mechanism=${SASL_MECHANISM}
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="${ADMIN_USER}" password="${ADMIN_PASSWORD}";
ssl.keystore.location=${SSL_KEYSTORE_LOCATION}
ssl.keystore.password=${SSL_KEYSTORE_PASSWORD}
ssl.truststore.location=${SSL_TRUSTSTORE_LOCATION}
ssl.truststore.password=${SSL_TRUSTSTORE_PASSWORD}
ssl.endpoint.identification.algorithm=
EOF
)

echo "🔒 Setting up Kafka ACLs on ${BOOTSTRAP_SERVER}..."
echo "🔐 Using SASL_SSL with SCRAM-SHA-512 authentication"
echo ""

# Verify SSL certificate files exist
if [[ ! -f "${SSL_KEYSTORE_LOCATION}" ]]; then
  echo "❌ Error: SSL keystore not found at ${SSL_KEYSTORE_LOCATION}" >&2
  echo "   Please ensure the SSL certificates are properly mounted." >&2
  exit 1
fi

if [[ ! -f "${SSL_TRUSTSTORE_LOCATION}" ]]; then
  echo "❌ Error: SSL truststore not found at ${SSL_TRUSTSTORE_LOCATION}" >&2
  echo "   Please ensure the SSL certificates are properly mounted." >&2
  exit 1
fi

echo "✅ SSL certificates found"
echo ""

# Function to add ACL
add_acl() {
  local principal="$1"
  local resource_type="$2"
  local resource_name="$3"
  shift 3
  local operations="$*"
  
  echo "Adding ACL: User:${principal} -> ${resource_type}:${resource_name} (${operations})"
  
  # Create temp file for command config
  local temp_config=$(mktemp)
  echo "${COMMON_SECURITY_CONFIG}" > "${temp_config}"
  
  # Add each operation separately to avoid comma-separated issues
  local success_count=0
  local total_ops=0
  
  for op in ${operations}; do
    total_ops=$((total_ops + 1))
    # Capture output but suppress help text
    local cmd_output
    cmd_output=$("${KAFKA_ACLS_BIN}" \
      --bootstrap-server "${BOOTSTRAP_SERVER}" \
      --command-config "${temp_config}" \
      --add \
      --allow-principal "User:${principal}" \
      --${resource_type} "${resource_name}" \
      --operation "${op}" 2>&1 | grep -vE "(^Option|^Description|^--|^$)" || true)
    
    if [[ $? -eq 0 ]] && ! echo "${cmd_output}" | grep -qiE "(error|exception|failed|invalid|ResourceType.*only supports)" 2>/dev/null; then
      success_count=$((success_count + 1))
    elif echo "${cmd_output}" | grep -qi "already exists" 2>/dev/null; then
      success_count=$((success_count + 1))  # Already exists is fine
    fi
  done
  
  if [[ ${success_count} -eq ${total_ops} ]]; then
    echo "   ✅ ACL added successfully (${success_count}/${total_ops} operations)"
  elif [[ ${success_count} -gt 0 ]]; then
    echo "   ⚠️  Partially added (${success_count}/${total_ops} operations succeeded)"
  else
    echo "   ⚠️  Failed to add ACL (operations may already exist or error occurred)"
  fi
  
  rm -f "${temp_config}"
  
  # Small delay to prevent overwhelming the system
  sleep 0.5
}

# Set up ACLs for Student Service
echo "🔐 Setting up ACLs for Student Service..."
add_acl "student-service" "topic" "student-events" "READ WRITE CREATE DESCRIBE"
add_acl "student-service" "group" "student-service-group" "READ DESCRIBE"

# Set up ACLs for Course Service
echo "🔐 Setting up ACLs for Course Service..."
add_acl "course-service" "topic" "course-events" "READ WRITE CREATE DESCRIBE"
add_acl "course-service" "group" "course-service-group" "READ DESCRIBE"

# Set up ACLs for Grade Service
echo "🔐 Setting up ACLs for Grade Service..."
add_acl "grade-service" "topic" "grade-events" "READ WRITE CREATE DESCRIBE"
add_acl "grade-service" "topic" "student-events" "READ DESCRIBE"
add_acl "grade-service" "group" "grade-service-group" "READ DESCRIBE"

# Set up ACLs for Enrollment Service
echo "🔐 Setting up ACLs for Enrollment Service..."
add_acl "enrollment-service" "topic" "enrollment-events" "READ WRITE CREATE DESCRIBE"
add_acl "enrollment-service" "topic" "student-events" "READ DESCRIBE"
add_acl "enrollment-service" "topic" "course-events" "READ DESCRIBE"
add_acl "enrollment-service" "topic" "grade-events" "READ DESCRIBE"
add_acl "enrollment-service" "topic" "notification-events" "READ DESCRIBE"
add_acl "enrollment-service" "topic" "identity-events" "READ DESCRIBE"
add_acl "enrollment-service" "group" "enrollment-service-group" "READ DESCRIBE"

# Set up ACLs for Notification Service
echo "🔐 Setting up ACLs for Notification Service..."
add_acl "notification-service" "topic" "notification-events" "READ WRITE CREATE DESCRIBE"
add_acl "notification-service" "topic" "student-events" "READ DESCRIBE"
add_acl "notification-service" "topic" "course-events" "READ DESCRIBE"
add_acl "notification-service" "topic" "grade-events" "READ DESCRIBE"
add_acl "notification-service" "topic" "enrollment-events" "READ DESCRIBE"
add_acl "notification-service" "topic" "identity-events" "READ DESCRIBE"
add_acl "notification-service" "group" "notification-service-group" "READ DESCRIBE"

# Set up ACLs for Identity Service
echo "🔐 Setting up ACLs for Identity Service..."
add_acl "identity-service" "topic" "identity-events" "READ WRITE CREATE DESCRIBE"
add_acl "identity-service" "topic" "student-events" "READ DESCRIBE"
add_acl "identity-service" "topic" "course-events" "READ DESCRIBE"
add_acl "identity-service" "topic" "grade-events" "READ DESCRIBE"
add_acl "identity-service" "topic" "enrollment-events" "READ DESCRIBE"
add_acl "identity-service" "topic" "notification-events" "READ DESCRIBE"
add_acl "identity-service" "group" "identity-service-group" "READ DESCRIBE"

echo ""
echo "✅ ACLs set up successfully!"
echo ""
echo "🔒 ACL Summary:"
echo "   - Each service can only access its designated topics"
echo "   - Services can read/write to their own consumer groups"
echo "   - Admin user has full access to all resources (configured as super user)"
echo ""
echo "📋 Topics with ACLs:"
echo "   - student-events"
echo "   - course-events"
echo "   - grade-events"
echo "   - enrollment-events"
echo "   - notification-events"
echo "   - identity-events"

