#!/bin/bash
set -e

# 시스템 업데이트
dnf update -y

# Java 21 설치
dnf install -y java-21-amazon-corretto

echo "Prod EC2 setup completed!" > /var/log/user-data.log
