#!/bin/bash
set -e

mvn clean install -DskipTests || exit 1
docker build -t exonym-example-sso:latest . || exit 1
docker compose up -d || exit 1
docker logs exonym-example-sso -f
