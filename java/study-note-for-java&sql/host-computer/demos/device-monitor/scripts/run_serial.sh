#!/bin/bash
# 串口链路演示：PTY 虚拟串口 + Modbus RTU（设备文件传输）
cd "$(dirname "$0")/.." || exit 1
mkdir -p data out
CP="lib/jSerialComm-2.11.0.jar:lib/sqlite-jdbc.jar:out"
rm -f data/device_pty.txt data/monitor.db* data/rtu_sim.log

echo "== [1/4] 编译 =="
javac -encoding UTF-8 -cp "lib/jSerialComm-2.11.0.jar" -d out $(find src -name '*.java') 2>&1 || { echo "COMPILE FAILED"; exit 1; }
echo "compile ok"

echo "== [2/4] 启动串口模式模拟器（PTY）=="
nohup python3 simulator/sim_modbus.py --mode serial --pty-file data/device_pty.txt > data/rtu_sim.log 2>&1 &
SIM=$!
for i in $(seq 1 20); do
  [ -f data/device_pty.txt ] && break
  sleep 0.25
done
PTY=$(cat data/device_pty.txt 2>/dev/null)
echo "虚拟串口 = $PTY"
sleep 0.5

echo "== [3/4] 串口采集（3 轮）+ 下发控制 + 再采集（2 轮）=="
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main monitor --serial "$PTY" --interval 1 --count 3 2>&1
echo "--- control ---"
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main control --serial "$PTY" --max-temp 21.0 2>&1
echo "--- monitor again ---"
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main monitor --serial "$PTY" --interval 1 --count 2 2>&1

echo "== [4/4] 报表 =="
java -Dfile.encoding=UTF-8 -cp "$CP" com.example.scada.Main report 2>&1

kill $SIM 2>/dev/null
wait $SIM 2>/dev/null
echo "== 模拟器日志（尾 10 行）=="
tail -10 data/rtu_sim.log
echo "RUN_SERIAL_DONE"