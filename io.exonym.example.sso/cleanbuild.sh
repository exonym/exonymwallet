#!/bin/bash
set -e

mvn clean install -DskipTests || exit 1
docker build -t exonym-example-sso:latest . || exit 1

HOST_IP=$(ifconfig en0 | grep "inet " | awk '{ print $2 }')
echo "Host IP: ${HOST_IP}"
HOST_IP=$HOST_IP docker compose up -d || exit 1
