#!/usr/bin/env bash
set -euo pipefail

# Configuration
ROOT_DIR="/Users/mythreya/Desktop/trial"
NS="${DOCKERHUB_NAMESPACE:-mythreya9850}"

APP_IMAGE="$NS/timesheet-app:latest"
NOTIFY_IMAGE="$NS/notification-service:latest"
EXTVAL_IMAGE="$NS/external-validation-api:latest"
SFTP_IMAGE="$NS/mock-sftp-server:latest"

echo "Using Docker Hub namespace: $NS"

# Optional Docker login (non-interactive) if credentials provided
if [[ -n "${DOCKERHUB_USERNAME:-}" && -n "${DOCKERHUB_TOKEN:-}" ]]; then
  echo "Logging into Docker Hub as $DOCKERHUB_USERNAME..."
  echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
else
  echo "Docker Hub credentials not provided; proceeding without explicit login."
fi

echo "Building Maven artifacts (skip tests)..."
( cd "$ROOT_DIR" && mvn -q -DskipTests clean package )
( cd "$ROOT_DIR/notification-service" && mvn -q -DskipTests clean package )
( cd "$ROOT_DIR/external-validation-api" && mvn -q -DskipTests clean package )

BUILD_DATE="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo "Build date: $BUILD_DATE"

echo "Building Docker images with --no-cache..."
# Ensure buildx is available and select a builder
docker buildx ls >/dev/null 2>&1 || true
docker buildx create --use >/dev/null 2>&1 || true

PLATFORMS="linux/amd64,linux/arm64"
echo "Building and pushing multi-arch images for platforms: $PLATFORMS"

# Root app image (multi-arch)
docker buildx build \
  --no-cache \
  --platform "$PLATFORMS" \
  --build-arg BUILD_DATE="$BUILD_DATE" \
  --build-arg JAR_TIMESTAMP="$BUILD_DATE" \
  -t "$APP_IMAGE" "$ROOT_DIR" --push

# Notification service image (multi-arch)
docker buildx build \
  --no-cache \
  --platform "$PLATFORMS" \
  -t "$NOTIFY_IMAGE" "$ROOT_DIR/notification-service" --push

# External validation API image (multi-arch)
docker buildx build \
  --no-cache \
  --platform "$PLATFORMS" \
  -t "$EXTVAL_IMAGE" "$ROOT_DIR/external-validation-api" --push

# Mock SFTP server image (multi-arch)
docker buildx build \
  --no-cache \
  --platform "$PLATFORMS" \
  -t "$SFTP_IMAGE" "$ROOT_DIR/mock-sftp-server" --push

echo "All multi-arch images built and pushed successfully."


