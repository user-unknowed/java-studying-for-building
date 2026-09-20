#!/bin/bash
# 全自动演示（无需浏览器）：编译 → 起模拟器 + 服务 → 看板页/API 自检 → 控制下发 → 收尾
# 用法: bash scripts/run_demo.sh
cd "$(dirname "$0")/.." || exit 1
mkdir -p data out
LIB="lib/tomcat-embed-core-10.1.60.jar:lib/gson-2.11.0.jar:lib/sqlite-jdbc-3.47.0.0.jar:lib/jakarta.annotation-api-2.1.1.jar"
rm -f data/web-monitor.db* data/web_sim.log data/server.log

echo "== [1/6] 编译 =="
javac -encoding UTF-8 -cp "$LIB" -d out $(find src -name '*.java') || { echo "COMPILE FAILED"; exit 1; }
echo "compile ok"

echo "== [2/6] 启动模拟器 + Web 服务（后台）=="
nohup python3 simulator/sim_modbus.py --mode tcp --port 15020 > data/web_sim.log 2>&1 &
SIM=$!
nohup java -Dfile.encoding=UTF-8 -cp "$LIB:out" com.example.webmonitor.Main --port 8090 --sim-port 15020 --interval 1 > data/server.log 2>&1 &
SRV=$!

echo "--- 等待服务就绪..."
READY=0
for i in $(seq 1 30); do
  if curl -s -m 1 http://localhost:8090/api/summary > /dev/null 2>&1; then READY=1; break; fi
  sleep 0.5
done
if [ "$READY" != "1" ]; then
  echo "SERVER NOT READY"
  tail -30 data/server.log
  kill $SIM 2>/dev/null
  exit 1
fi
sleep 2

echo "== [3/6] 看板页与 API 自检 =="
echo "GET / → HTTP $(curl -s -o /dev/null -w '%{http_code}' http://localhost:8090/)"
curl -s http://localhost:8090/ | grep -o '<title>[^<]*</title>'
bash scripts/test_api.sh
echo ""

echo "== [4/6] 控制下发：报警上限 → 21.5℃（随后温度越限应出现报警）=="
curl -s -m 3 -X POST http://localhost:8090/api/command \
  -H 'Content-Type: application/json' \
  -d '{"cmd":"set_temp_limit","value":21.5}'
echo ""
sleep 3

echo "== [5/6] 下发结果核验 =="
echo "--- 下发后的 summary:"
curl -s -m 3 http://localhost:8090/api/summary
echo ""
echo "--- 报警记录:"
curl -s -m 3 http://localhost:8090/api/alarms
echo ""

echo "== [6/6] 收尾 =="
kill $SRV 2>/dev/null
kill $SIM 2>/dev/null
wait $SRV 2>/dev/null
wait $SIM 2>/dev/null
echo "--- 服务日志（尾 12 行）:"
tail -12 data/server.log
echo "--- 模拟器日志（尾 6 行）:"
tail -6 data/web_sim.log
echo "RUN_WEB_DEMO_DONE"