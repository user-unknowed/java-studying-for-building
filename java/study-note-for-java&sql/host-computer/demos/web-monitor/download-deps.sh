#!/bin/bash
# 下载 web-monitor 所需外部库到 lib/ 目录（嵌入式 Tomcat + Gson + SQLite JDBC）
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

# Web 容器：嵌入式 Tomcat 10.1（Jakarta EE 10 / Servlet 6.0）
download "org/apache/tomcat/embed/tomcat-embed-core/10.1.60/tomcat-embed-core-10.1.60.jar" "tomcat-embed-core-10.1.60.jar"
# JSON 序列化：Gson（API 层输出 JSON）
download "com/google/code/gson/gson/2.11.0/gson-2.11.0.jar" "gson-2.11.0.jar"
# 历史存储：SQLite JDBC 驱动
download "org/xerial/sqlite-jdbc/3.47.0.0/sqlite-jdbc-3.47.0.0.jar" "sqlite-jdbc-3.47.0.0.jar"
# 注解 API：Tomcat 扫描/加载 Servlet 注解时需要（jakarta.annotation）
download "jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar" "jakarta.annotation-api-2.1.1.jar"

echo "依赖就绪:"
ls -lh lib/