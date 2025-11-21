#!/bin/bash

# Kafka SCRAM Users Check Script
# Lists all SCRAM users configured in Kafka

set -euo pipefail

KAFKA_CONFIGS_BIN=${KAFKA_CONFIGS_BIN:-$(command -v kafka-configs || true)}
BOOTSTRAP=${BOOTSTRAP:-kafka-secured:29093}

if [[ -z "${KAFKA_CONFIGS_BIN}" ]]; then
  echo "Error: kafka-configs not found in PATH. Set KAFKA_CONFIGS_BIN to the binary path." >&2
  exit 1
fi

echo "🔍 Checking SCRAM users on ${BOOTSTRAP}..."
echo ""

# Create a temporary config file with SSL and SASL settings
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
request.timeout.ms=60000
connections.max.idle.ms=540000
EOF

# Cleanup function
cleanup() {
  rm -f "${TEMP_CONFIG}"
}
trap cleanup EXIT

# Set heap size
export KAFKA_HEAP_OPTS="${KAFKA_HEAP_OPTS:--Xmx512M -Xms256M}"

# Function to check a specific user
check_user() {
  local username="$1"
  echo "   Checking user: ${username}"
  
  local output
  output=$("${KAFKA_CONFIGS_BIN}" \
    --bootstrap-server "${BOOTSTRAP}" \
    --command-config "${TEMP_CONFIG}" \
    --describe \
    --entity-type users \
    --entity-name "${username}" \
    2>&1)
  
  if echo "${output}" | grep -qiE "(error|exception|timeout|failed)" || ! echo "${output}" | grep -qi "${username}"; then
    echo "      ❌ Not found"
    return 1
  else
    echo "      ✅ Found"
    echo "${output}" | grep -A 5 "${username}" | head -10 | sed 's/^/         /'
    return 0
  fi
}

echo "🔎 Checking individual users:"
echo ""

# Expected users
EXPECTED_USERS=(
  "admin"
  "student-service"
  "course-service"
  "grade-service"
  "enrollment-service"
  "notification-service"
  "identity-service"
)

# First, check each expected user individually
FOUND_COUNT=0
for user in "${EXPECTED_USERS[@]}"; do
  if check_user "${user}"; then
    FOUND_COUNT=$((FOUND_COUNT + 1))
  fi
  echo ""
done

echo "📊 Summary: ${FOUND_COUNT}/${#EXPECTED_USERS[@]} users found"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📋 Listing all SCRAM users:"
echo ""

# Get all users
echo "   Running kafka-configs --describe --entity-type users..."
ALL_USERS_OUTPUT=$("${KAFKA_CONFIGS_BIN}" \
  --bootstrap-server "${BOOTSTRAP}" \
  --command-config "${TEMP_CONFIG}" \
  --describe \
  --entity-type users \
  2>&1)

EXIT_CODE=$?

echo "   Command exit code: ${EXIT_CODE}"
echo ""

# Check for errors
if [[ ${EXIT_CODE} -ne 0 ]] || echo "${ALL_USERS_OUTPUT}" | grep -qiE "(error|exception|timeout|failed)"; then
  echo "❌ Error retrieving users:"
  echo "${ALL_USERS_OUTPUT}" | head -20
  echo ""
  echo "Full output:"
  echo "${ALL_USERS_OUTPUT}"
  exit 1
fi

# Show raw output for debugging
echo "Raw output:"
echo "${ALL_USERS_OUTPUT}"
echo ""

# Extract user names from output - try multiple patterns
FOUND_USERS=$(echo "${ALL_USERS_OUTPUT}" | grep -oE "(User:([^\s,]+)|entity-name[=:]([^\s]+))" | sed -E 's/(User:|entity-name[=:])//' | sort -u | grep -v "^$" || echo "")

if [[ -z "${FOUND_USERS}" ]] || [[ "${FOUND_USERS}" == "" ]]; then
  echo "⚠️  No SCRAM users found in output"
  echo ""
  echo "The describe command may have returned empty results."
  echo "This could mean:"
  echo "   1. No users have been created yet"
  echo "   2. Users exist but were not returned by the command"
  echo ""
  echo "Expected users:"
  for user in "${EXPECTED_USERS[@]}"; do
    echo "   - ${user}"
  done
  echo ""
  echo "💡 Try checking individual users:"
  for user in "${EXPECTED_USERS[@]}"; do
    echo "   kafka-configs --bootstrap-server ${BOOTSTRAP} --command-config <config> --describe --entity-type users --entity-name ${user}"
  done
  exit 0
fi

echo "✅ Found SCRAM users:"
echo ""
echo "${FOUND_USERS}" | while read -r user; do
  echo "   ✓ ${user}"
done

echo ""
echo "📊 Checking expected users:"
echo ""

MISSING_COUNT=0
for expected_user in "${EXPECTED_USERS[@]}"; do
  if echo "${FOUND_USERS}" | grep -q "^${expected_user}$"; then
    echo "   ✅ ${expected_user}"
  else
    echo "   ❌ ${expected_user} (missing)"
    MISSING_COUNT=$((MISSING_COUNT + 1))
  fi
done

echo ""
if [[ ${MISSING_COUNT} -eq 0 ]]; then
  echo "✅ All expected users are present!"
else
  echo "⚠️  ${MISSING_COUNT} expected user(s) are missing"
fi

echo ""
echo "📋 Full user details:"
echo "${ALL_USERS_OUTPUT}" | grep -A 2 "User:" | head -30
