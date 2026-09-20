#!/bin/bash
# 下载 SQLite JDBC 驱动（sqlite-jdbc）到 lib/ 目录
# 用法: bash download-driver.sh
#
# 版本与本项目演示环境一致：3.47.0.0
cd "$(dirname "$0")" || exit 1
mkdir -p lib

VERSION="3.47.0.0"
JAR="sqlite-jdbc-${VERSION}.jar"

if [ -f lib/sqlite-jdbc.jar ]; then
  echo "lib/sqlite-jdbc.jar 已存在，跳过下载。"
  exit 0
fi

echo "正在下载 sqlite-jdbc ${VERSION} ..."
if curl -L --fail -o lib/sqlite-jdbc.jar "https://github.com/xerial/sqlite-jdbc/releases/download/${VERSION}/${JAR}"; then
  echo "下载完成: $(ls -lh lib/sqlite-jdbc.jar | awk '{print $5}')"
else
  echo "下载失败，请检查网络后重试。"
  exit 1
fi