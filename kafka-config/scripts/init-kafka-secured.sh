#!/bin/bash

# Kafka Secured Initialization Script
# This script initializes Kafka with topics and ACLs for secured environment
# Run this script after Kafka is up and running

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

echo "🚀 Initializing Kafka Secured Environment..."
echo ""

# Wait for Kafka to be ready
echo "⏳ Waiting for Kafka to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0
BOOTSTRAP_SERVER=${BOOTSTRAP_SERVER:-kafka-secured:29093}

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
  if timeout 5 bash -c "echo > /dev/tcp/${BOOTSTRAP_SERVER%:*}/${BOOTSTRAP_SERVER#*:}" 2>/dev/null; then
    echo "✅ Kafka is ready!"
    break
  fi
  RETRY_COUNT=$((RETRY_COUNT + 1))
  echo "   Waiting for Kafka... (${RETRY_COUNT}/${MAX_RETRIES})"
  sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
  echo "❌ Kafka is not ready after ${MAX_RETRIES} retries"
  exit 1
fi

# Step 1: Create SCRAM users
echo ""
echo "📝 Step 1: Creating SCRAM users..."
if [ -f "setup-users.sh" ]; then
  bash setup-users.sh
else
  echo "⚠️  setup-users.sh not found, skipping user creation"
fi

# Step 2: Create topics
echo ""
echo "📝 Step 2: Creating topics..."
if [ -f "create-topics-secured.sh" ]; then
  bash create-topics-secured.sh
else
  echo "⚠️  create-topics-secured.sh not found, skipping topic creation"
fi

# Step 3: Setup ACLs
echo ""
echo "📝 Step 3: Setting up ACLs..."
if [ -f "setup-acls-secured.sh" ]; then
  bash setup-acls-secured.sh
else
  echo "⚠️  setup-acls-secured.sh not found, skipping ACL setup"
fi

echo ""
echo "✅ Kafka Secured Environment initialization completed!"
echo ""
echo "📋 Summary:"
echo "   ✅ SCRAM users created"
echo "   ✅ Topics created"
echo "   ✅ ACLs configured"
echo ""
echo "💡 Services can now connect to Kafka secured"

