#!/bin/bash
# 启动下位机模拟器（TCP 模式）并做一次原始字节级的契约测试
cd "$(dirname "$0")/.." || exit 1
mkdir -p data out
rm -f data/sim.log
nohup python3 simulator/sim_modbus.py --mode tcp --port 15020 > data/sim.log 2>&1 &
SIM=$!
sleep 1.2
python3 scripts/test_sim.py 2>&1
echo "test_exit=$?"
echo "== sim log =="
tail -12 data/sim.log
kill $SIM 2>/dev/null
wait $SIM 2>/dev/null
echo "SIM_TEST_DONE"