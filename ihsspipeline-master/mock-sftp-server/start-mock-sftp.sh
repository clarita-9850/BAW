#!/bin/bash

echo "🚀 Starting Mock SFTP Server for Testing"
echo "=========================================="

# Create reports directory if it doesn't exist
mkdir -p reports/daily

# Start the mock SFTP server
echo "📁 Creating SFTP directories..."
mkdir -p reports/daily/2025-01
mkdir -p reports/daily/2025-02
mkdir -p reports/daily/2025-03

echo "🔧 Building and starting mock SFTP server..."
docker-compose up --build -d

echo "⏳ Waiting for SFTP server to start..."
sleep 5

echo "✅ Mock SFTP Server Status:"
echo "🌐 Host: localhost:2222"
echo "👤 Username: sftpuser"
echo "🔑 Password: password"
echo "📁 Base Path: /reports"
echo ""
echo "🧪 Test SFTP connection:"
echo "sftp -P 2222 sftpuser@localhost"
echo ""
echo "📊 Monitor uploaded files:"
echo "ls -la reports/daily/"
echo ""
echo "🛑 Stop server: docker-compose down"
