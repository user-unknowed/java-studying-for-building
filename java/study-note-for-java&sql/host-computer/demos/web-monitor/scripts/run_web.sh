#!/bin/bash
# 一键启动 web-monitor：下位机模拟器（后台）+ Web 版上位机（前台）
# 用法: bash scripts/run_web.sh
cd "$(dirname "$0")/.." || exit 1
mkdir -p data out
LIB="lib/tomcat-embed-core-10.1.60.jar:lib/gson-2.11.0.jar:lib/sqlite-jdbc-3.47.0.0.jar:lib/jakarta.annotation-api-2.1.1.jar"

if [ ! -f lib/tomcat-embed-core-10.1.60.jar ]; then
  echo "缺少依赖，请先执行: bash download-deps.sh"
  exit 1
fi

echo "== [1/3] 编译 =="
javac -encoding UTF-8 -cp "$LIB" -d out $(find src -name '*.java') || { echo "COMPILE FAILED"; exit 1; }
echo "compile ok"

echo "== [2/3] 启动下位机模拟器（Modbus TCP :15020）=="
nohup python3 simulator/sim_modbus.py --mode tcp --port 15020 > data/web_sim.log 2>&1 &
SIM=$!
trap 'echo; echo "[stop] 关闭模拟器..."; kill $SIM 2>/dev/null' EXIT INT TERM
sleep 1.2

echo "== [3/3] 启动 Web 版上位机（前台运行，Ctrl+C 退出）=="
echo "看板地址: http://localhost:8090/  （浏览器打开）"
java -Dfile.encoding=UTF-8 -cp "$LIB:out" com.example.webmonitor.Main --port 8090 --sim-port 15020 --interval 2