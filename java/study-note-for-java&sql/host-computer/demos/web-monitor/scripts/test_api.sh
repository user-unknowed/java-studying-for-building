#!/bin/bash
# 对运行中的 web-monitor 做一轮 API 自检（JSON 美化输出，同时校验格式合法性）
# 用法: bash scripts/test_api.sh [base-url]
BASE=${1:-http://localhost:8090}

echo "[1] /api/summary"
curl -s -m 3 "$BASE/api/summary" | python3 -m json.tool
echo "[2] /api/readings?limit=3"
curl -s -m 3 "$BASE/api/readings?limit=3" | python3 -m json.tool
echo "[3] /api/alarms"
curl -s -m 3 "$BASE/api/alarms" | python3 -m json.tool
echo "TEST_API_DONE"