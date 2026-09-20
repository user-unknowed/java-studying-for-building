#!/bin/bash
# 下载 javase-demo 所需外部库到 lib/ 目录（Jackson / SLF4J / Commons Lang3 / JUnit Console）
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

download "com/fasterxml/jackson/core/jackson-databind/2.17.2/jackson-databind-2.17.2.jar" "jackson-databind-2.17.2.jar"
download "com/fasterxml/jackson/core/jackson-core/2.17.2/jackson-core-2.17.2.jar" "jackson-core-2.17.2.jar"
download "com/fasterxml/jackson/core/jackson-annotations/2.17.2/jackson-annotations-2.17.2.jar" "jackson-annotations-2.17.2.jar"
download "org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar" "slf4j-api-2.0.13.jar"
download "org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar" "slf4j-simple-2.0.13.jar"
download "org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar" "commons-lang3-3.14.0.jar"
download "org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar" "junit-platform-console-standalone-1.10.2.jar"

echo "依赖就绪:"
ls -lh lib/