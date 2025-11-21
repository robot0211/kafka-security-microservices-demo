#!/bin/bash

# Kafka SCRAM Users Setup Script (Confluent-compatible)
# Creates SCRAM-SHA-512 users using kafka-configs with --bootstrap-server.

set -euo pipefail

KAFKA_CONFIGS_BIN=${KAFKA_CONFIGS_BIN:-$(command -v kafka-configs || true)}
BOOTSTRAP=${BOOTSTRAP:-kafka-secured:29093}

if [[ -z "${KAFKA_CONFIGS_BIN}" ]]; then
  echo "Error: kafka-configs not found in PATH. Set KAFKA_CONFIGS_BIN to the binary path." >&2
  exit 1
fi

echo "👥 Setting up Kafka SCRAM users against ${BOOTSTRAP}..."
echo "🔐 Creating SCRAM-SHA-512 users..."

# Create a temporary config file with timeout and SSL settings
TEMP_CONFIG=$(mktemp)
cat > "${TEMP_CONFIG}" <<EOF
security.protocol=SASL_SSL
sasl.mechanism=SCRAM-SHA-512
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="admin123";
ssl.keystore.location=/etc/kafka/secrets/kafka.client.keystore.jks
ssl.keystore.password=kafka123
ssl.truststore.location=/etc/kafka/secrets/kafka.client.truststore.jks
ssl.truststore.password=kafka123
ssl.endpoint.identification.algorithm=
# Increase timeouts for SSL connections
request.timeout.ms=60000
connections.max.idle.ms=540000
EOF

# Cleanup function
cleanup() {
  rm -f "${TEMP_CONFIG}"
}
trap cleanup EXIT

add_user() {
  local username="$1"
  local password="$2"
  local max_retries=3
  local retry_count=0
  
  # Set heap size for kafka-configs to prevent OutOfMemoryError
  # Use larger heap size (1GB) for the config command to handle SSL connections
  export KAFKA_HEAP_OPTS="${KAFKA_HEAP_OPTS:--Xmx1024M -Xms512M}"
  
  echo "   Creating user: ${username}"
  
  while [ $retry_count -lt $max_retries ]; do
    local cmd_output
    cmd_output=$("${KAFKA_CONFIGS_BIN}" \
      --bootstrap-server "${BOOTSTRAP}" \
      --command-config "${TEMP_CONFIG}" \
      --alter \
      --add-config "SCRAM-SHA-512=[password=${password}]" \
      --entity-type users \
      --entity-name "${username}" 2>&1)
    
    local exit_code=$?
    
    # Check if command succeeded (no errors in output)
    if [[ ${exit_code} -eq 0 ]] && ! echo "${cmd_output}" | grep -qiE "(error|exception|failed|timeout)" 2>/dev/null; then
      echo "   ✅ User ${username} created successfully"
      sleep 1
      return 0
    else
      retry_count=$((retry_count + 1))
      
      if [ $retry_count -lt $max_retries ]; then
        echo "   ⏳ Retry ${retry_count}/${max_retries} for user ${username}..."
        sleep 3
      else
        # Check if user already exists
        local describe_output
        describe_output=$("${KAFKA_CONFIGS_BIN}" \
          --bootstrap-server "${BOOTSTRAP}" \
          --command-config "${TEMP_CONFIG}" \
          --describe \
          --entity-type users \
          --entity-name "${username}" 2>&1)
        
        if echo "${describe_output}" | grep -qi "${username}" 2>/dev/null; then
          echo "   ℹ️  User ${username} already exists"
          return 0
        else
          echo "   ⚠️  Failed to create user ${username} after ${max_retries} attempts"
          # Show error (first line only to avoid clutter)
          echo "${cmd_output}" | grep -iE "(error|exception|timeout)" | head -1 | sed 's/^/      /' || true
          return 1
        fi
      fi
    fi
  done
  
  return 1
}

# Track failures but continue
FAILED_USERS=0

add_user admin admin123 || FAILED_USERS=$((FAILED_USERS + 1))
add_user student-service student123 || FAILED_USERS=$((FAILED_USERS + 1))
add_user course-service course123 || FAILED_USERS=$((FAILED_USERS + 1))
add_user grade-service grade123 || FAILED_USERS=$((FAILED_USERS + 1))
add_user enrollment-service enrollment123 || FAILED_USERS=$((FAILED_USERS + 1))
add_user notification-service notification123 || FAILED_USERS=$((FAILED_USERS + 1))
add_user identity-service identity123 || FAILED_USERS=$((FAILED_USERS + 1))

if [ $FAILED_USERS -eq 0 ]; then
  echo ""
  echo "✅ SCRAM users created successfully!"
else
  echo ""
  echo "⚠️  Created users with ${FAILED_USERS} failure(s)"
fi
echo ""
echo "👥 Created users:"
echo "   - admin (password: admin123)"
echo "   - student-service (password: student123)"
echo "   - course-service (password: course123)"
echo "   - grade-service (password: grade123)"
echo "   - enrollment-service (password: enrollment123)"
echo "   - notification-service (password: notification123)"
echo "   - identity-service (password: identity123)"
