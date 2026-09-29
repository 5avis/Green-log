#!/usr/bin/env bash
# ==============================================================================
# GreenLog Auto-Launcher & Port Guard
# Safely frees port 8080 from any lingering processes and starts Spring Boot
# ==============================================================================
set -e

TARGET_PORT="${PORT:-8080}"

echo "🔍 Checking port ${TARGET_PORT}..."
EXISTING_PID=$(lsof -ti :${TARGET_PORT} 2>/dev/null || fuser ${TARGET_PORT}/tcp 2>/dev/null || true)

if [ -n "$EXISTING_PID" ]; then
    echo "⚠️  Found previous process using port ${TARGET_PORT} (PID: ${EXISTING_PID}). Cleaning it up..."
    kill -9 $EXISTING_PID 2>/dev/null || true
    sleep 1
    echo "✅ Port ${TARGET_PORT} freed."
else
    echo "✅ Port ${TARGET_PORT} is free."
fi

echo "🚀 Starting GreenLog application on port ${TARGET_PORT}..."
exec ./mvnw spring-boot:run
