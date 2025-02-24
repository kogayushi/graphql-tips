#!/bin/bash

IMAGE_NAME="kogayushi/graphql-tips"

if ! docker images --format "{{.Repository}}" | grep -q "^$IMAGE_NAME$"; then
  echo "Docker image '$IMAGE_NAME' not found. Running bootBuild..."
  ./gradlew bootBuild
else
  echo "Docker image '$IMAGE_NAME' already exists. Skipping bootBuild."
fi

# Docker Compose を起動
docker compose up -d