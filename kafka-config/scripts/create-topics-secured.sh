#!/bin/bash

# Kafka Topics Creation Script for Secured Kafka (SASL_SSL)
# This script creates all required topics for the student management system

set -euo pipefail

# Configuration
BOOTSTRAP_SERVER=${BOOTSTRAP_SERVER:-kafka-secured:29093}
SECURITY_PROTOCOL=SASL_SSL
SASL_MECHANISM=SCRAM-SHA-512
SASL_JAAS_CONFIG='org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="admin123";'
SSL_KEYSTORE_LOCATION=${SSL_KEYSTORE_LOCATION:-/etc/kafka/secrets/kafka.client.keystore.jks}
SSL_KEYSTORE_PASSWORD=${SSL_KEYSTORE_PASSWORD:-kafka123}
SSL_TRUSTSTORE_LOCATION=${SSL_TRUSTSTORE_LOCATION:-/etc/kafka/secrets/kafka.client.truststore.jks}
SSL_TRUSTSTORE_PASSWORD=${SSL_TRUSTSTORE_PASSWORD:-kafka123}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-1}
PARTITIONS=${PARTITIONS:-3}

# Check if kafka-topics.sh is available
KAFKA_TOPICS_BIN=${KAFKA_TOPICS_BIN:-$(command -v kafka-topics || true)}
if [[ -z "${KAFKA_TOPICS_BIN}" ]]; then
  echo "Error: kafka-topics not found in PATH. Set KAFKA_TOPICS_BIN to the binary path." >&2
  echo "Example: export KAFKA_TOPICS_BIN=/opt/kafka/bin/kafka-topics.sh" >&2
  exit 1
fi

echo "📝 Creating Kafka topics on ${BOOTSTRAP_SERVER}..."
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

# Function to create a topic
create_topic() {
  local topic_name="$1"
  local replication_factor="${2:-${REPLICATION_FACTOR}}"
  local partitions="${3:-${PARTITIONS}}"
  
  echo "Creating topic: ${topic_name} (partitions: ${partitions}, replication: ${replication_factor})"
  
  "${KAFKA_TOPICS_BIN}" \
    --bootstrap-server "${BOOTSTRAP_SERVER}" \
    --create \
    --topic "${topic_name}" \
    --partitions "${partitions}" \
    --replication-factor "${replication_factor}" \
    --command-config <(cat <<EOF
security.protocol=${SECURITY_PROTOCOL}
sasl.mechanism=${SASL_MECHANISM}
sasl.jaas.config=${SASL_JAAS_CONFIG}
ssl.keystore.location=${SSL_KEYSTORE_LOCATION}
ssl.keystore.password=${SSL_KEYSTORE_PASSWORD}
ssl.truststore.location=${SSL_TRUSTSTORE_LOCATION}
ssl.truststore.password=${SSL_TRUSTSTORE_PASSWORD}
ssl.endpoint.identification.algorithm=
EOF
) || {
    echo "⚠️  Topic ${topic_name} might already exist, skipping..."
  }
}

# Create all required topics
create_topic "student-events" "${REPLICATION_FACTOR}" "${PARTITIONS}"
create_topic "course-events" "${REPLICATION_FACTOR}" "${PARTITIONS}"
create_topic "grade-events" "${REPLICATION_FACTOR}" "${PARTITIONS}"
create_topic "enrollment-events" "${REPLICATION_FACTOR}" "${PARTITIONS}"
create_topic "notification-events" "${REPLICATION_FACTOR}" "${PARTITIONS}"
create_topic "identity-events" "${REPLICATION_FACTOR}" "${PARTITIONS}"

echo ""
echo "✅ Topics creation completed!"
echo ""
echo "📋 Created topics:"
echo "   - student-events"
echo "   - course-events"
echo "   - grade-events"
echo "   - enrollment-events"
echo "   - notification-events"
echo "   - identity-events"
echo ""
echo "💡 Next step: Run setup-acls-secured.sh to set up ACLs for these topics"

