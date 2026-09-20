#!/bin/bash
# 完整演示：TCP 链路（编译 → 模拟器 → 采集入库 → 报表 → 下发控制 → 报警 → 报表）
cd "$(dirname "$0")/.." || exit 1
mkdir -p data out
CP="lib/jSerialComm-2.11.0.jar:lib/sqlite-jdbc.jar:out"
rm -f data/monitor.db* data/demo_sim.log

echo "== [1/6] 编译 =="
javac -encoding UTF-8 -cp "lib/jSerialComm-2.11.0.jar" -d out $(find src -name '*.java') 2>&1 || { echo "COMPILE FAILED"; exit 1; }
echo "compile ok"

echo "== [2/6] 启动下位机模拟器 =="
nohup python3 simulator/sim_modbus.py --mode tcp --port 15020 > data/demo_sim.log 2>&1 &
SIM=$!
sleep 1.2

echo "== [3/6] 上位机采集监控（5 轮）=="
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main monitor --tcp 127.0.0.1:15020 --interval 1 --count 5 2>&1
echo "monitor_exit=$?"

echo "== [4/6] 报表查询 =="
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main report 2>&1

echo "== [5/6] 下发控制（上限 → 22.0℃）=="
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main control --tcp 127.0.0.1:15020 --max-temp 22.0 2>&1

echo "== [6/6] 再次采集（观察报警）+ 报表 =="
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main monitor --tcp 127.0.0.1:15020 --interval 1 --count 3 2>&1
echo "--- 第二次报表:"
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main report 2>&1

kill $SIM 2>/dev/null
wait $SIM 2>/dev/null
echo "== 模拟器日志（尾 10 行）=="
tail -10 data/demo_sim.log
echo "RUN_DEMO_DONE"