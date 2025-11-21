#!/bin/bash

# Kafka SSL Setup Script
# This script creates SSL certificates for Kafka security testing

set -e

# Configuration
# Resolve SSL_DIR to the repo's kafka-config/ssl directory (works on host and in container)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SSL_DIR="$(cd "$SCRIPT_DIR/../ssl" 2>/dev/null && pwd || echo "$SCRIPT_DIR/../ssl")"
PASSWORD="kafka123"
VALIDITY_DAYS=365

echo "🔐 Setting up Kafka SSL certificates..."

# Create SSL directory
mkdir -p "$SSL_DIR"
cd "$SSL_DIR"

# Create CA key and certificate if absent
if [ ! -f ca-key ] || [ ! -f ca-cert ]; then
    echo "📜 Creating CA key and certificate..."
    openssl req -x509 -newkey rsa:2048 -keyout ca-key -out ca-cert -days "$VALIDITY_DAYS" -nodes -subj "/CN=KafkaTestCA/O=StudentSystem/C=VN"
fi

# Create CA certificate
echo "📜 Creating CA certificate..."
keytool -keystore kafka.server.truststore.jks \
    -alias CARoot \
    -import \
    -file ca-cert \
    -storepass "$PASSWORD" \
    -keypass "$PASSWORD" \
    -noprompt

# Create server keystore
echo "🔑 Creating server keystore..."
keytool -keystore kafka.server.keystore.jks \
    -alias localhost \
    -validity "$VALIDITY_DAYS" \
    -genkeypair \
    -keyalg RSA \
    -storepass "$PASSWORD" \
    -keypass "$PASSWORD" \
    -dname "CN=localhost, OU=KMA, O=StudentSystem, L=Hanoi, ST=Hanoi, C=VN"

# Create server certificate request
echo "📋 Creating server certificate request..."
keytool -keystore kafka.server.keystore.jks \
    -alias localhost \
    -certreq \
    -file cert-file \
    -storepass "$PASSWORD"

# Sign server certificate with CA
echo "✍️ Signing server certificate..."
openssl x509 -req \
    -CA ca-cert \
    -CAkey ca-key \
    -in cert-file \
    -out cert-signed \
    -days "$VALIDITY_DAYS" \
    -CAcreateserial

# Import CA certificate into server keystore
echo "📥 Importing CA certificate into server keystore..."
keytool -keystore kafka.server.keystore.jks \
    -alias CARoot \
    -import \
    -file ca-cert \
    -storepass "$PASSWORD" \
    -noprompt

# Import signed certificate into server keystore
echo "📥 Importing signed certificate into server keystore..."
keytool -keystore kafka.server.keystore.jks \
    -alias localhost \
    -import \
    -file cert-signed \
    -storepass "$PASSWORD" \
    -noprompt

# Create client keystore
echo "🔑 Creating client keystore..."
keytool -keystore kafka.client.keystore.jks \
    -alias localhost \
    -validity "$VALIDITY_DAYS" \
    -genkeypair \
    -keyalg RSA \
    -storepass "$PASSWORD" \
    -keypass "$PASSWORD" \
    -dname "CN=localhost, OU=KMA, O=StudentSystem, L=Hanoi, ST=Hanoi, C=VN"

# Create client certificate request
echo "📋 Creating client certificate request..."
keytool -keystore kafka.client.keystore.jks \
    -alias localhost \
    -certreq \
    -file client-cert-file \
    -storepass "$PASSWORD"

# Sign client certificate with CA
echo "✍️ Signing client certificate..."
openssl x509 -req \
    -CA ca-cert \
    -CAkey ca-key \
    -in client-cert-file \
    -out client-cert-signed \
    -days "$VALIDITY_DAYS" \
    -CAcreateserial

# Import CA certificate into client keystore
echo "📥 Importing CA certificate into client keystore..."
keytool -keystore kafka.client.keystore.jks \
    -alias CARoot \
    -import \
    -file ca-cert \
    -storepass "$PASSWORD" \
    -noprompt

# Import signed certificate into client keystore
echo "📥 Importing signed certificate into client keystore..."
keytool -keystore kafka.client.keystore.jks \
    -alias localhost \
    -import \
    -file client-cert-signed \
    -storepass "$PASSWORD" \
    -noprompt

# Create client truststore
echo "🔒 Creating client truststore..."
keytool -keystore kafka.client.truststore.jks \
    -alias CARoot \
    -import \
    -file ca-cert \
    -storepass "$PASSWORD" \
    -noprompt

# Set permissions
chmod 600 "$SSL_DIR"/*.jks 2>/dev/null || true
chmod 600 "$SSL_DIR"/*.pem 2>/dev/null || true

echo "✅ SSL certificates created successfully!"
echo "📁 Certificates location: $SSL_DIR"
echo ""
echo "🔐 Server keystore: kafka.server.keystore.jks"
echo "🔐 Server truststore: kafka.server.truststore.jks"
echo "🔐 Client keystore: kafka.client.keystore.jks"
echo "🔐 Client truststore: kafka.client.truststore.jks"
echo ""
echo "🔑 Password for all keystores: $PASSWORD"
echo ""
echo "⚠️  IMPORTANT: Keep these certificates secure and do not share the passwords!"
