#!/bin/bash
# 下载 host-computer / device-monitor 所需外部库到 lib/ 目录（jSerialComm + SQLite JDBC）
# 用法: bash download-deps.sh
cd "$(dirname "$0")" || exit 1
mkdir -p lib

MIRROR="https://repo1.maven.org/maven2"

download() {
  local path="$1" file="$2"
  if [ -f "lib/$file" ]; then
    echo "[skip] lib/$file 已存在"
    return
  fi
  echo "[get ] $file"
  if ! curl -L --fail -o "lib/$file" "$MIRROR/$path"; then
    echo "下载失败: $file"
    exit 1
  fi
}

# 串口通信：jSerialComm（真机串口；PTY 调试模式用 JDK 自带文件通道，不依赖它）
download "com/fazecast/jSerialComm/2.11.0/jSerialComm-2.11.0.jar" "jSerialComm-2.11.0.jar"
# SQLite JDBC 驱动（历史存储）：源文件为 sqlite-jdbc-3.47.0.0.jar，脚本内统一命名为 sqlite-jdbc.jar
download "org/xerial/sqlite-jdbc/3.47.0.0/sqlite-jdbc-3.47.0.0.jar" "sqlite-jdbc.jar"

echo "依赖就绪:"
ls -lh lib/