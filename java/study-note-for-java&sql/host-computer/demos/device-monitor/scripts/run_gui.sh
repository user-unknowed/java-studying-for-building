#!/bin/bash
# 启动 Swing 监控窗口（需要桌面环境；headless 环境仅编译验证）
cd "$(dirname "$0")/.." || exit 1
mkdir -p data out
CP="lib/jSerialComm-2.11.0.jar:lib/sqlite-jdbc.jar:out"

javac -encoding UTF-8 -cp "lib/jSerialComm-2.11.0.jar" -d out $(find src -name '*.java') 2>&1 || exit 1
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main gui --db data/monitor.db