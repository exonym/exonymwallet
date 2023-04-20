#!/bin/bash
mvn clean install -DskipTests
docker build -t exonym-example-sso:latest .
docker compose up -d
docker logs exonym-example-sso -f
