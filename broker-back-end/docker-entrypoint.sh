#!/bin/bash
set -e

cd /workspace

echo "Downloading Maven dependencies..."
/workspace/mvnw dependency:resolve -q || echo "Dependencies already resolved or error occurred, continuing..."

echo "Starting Quarkus in dev mode..."
exec /workspace/mvnw compile quarkus:dev
