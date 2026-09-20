#!/usr/bin/env bash
# order-service（Spring Boot 版） API 测试脚本（需服务已在 8081 端口运行）
BASE=http://localhost:8081

echo '===== 0. 商品列表（种子数据） ====='
curl -s $BASE/api/products; echo

echo '===== 1. 新增秒杀商品（库存 2） ====='
NAME="限量版机械键盘-$(date +%s)"
R=$(curl -s -X POST $BASE/api/products -H 'Content-Type: application/json' \
  -d "{\"name\":\"$NAME\",\"priceCents\":49900,\"stock\":2}")
echo "$R"
PID=$(echo "$R" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')
echo "新商品 id = $PID"

echo '===== 2. 下单：u01 买 2 件（库存清空） ====='
R2=$(curl -s -X POST $BASE/api/orders -H 'Content-Type: application/json' \
  -d "{\"userName\":\"u01\",\"items\":[{\"productId\":$PID,\"quantity\":2}]}")
echo "$R2"
OID=$(echo "$R2" | sed -n 's/.*"orderId":\([0-9]*\).*/\1/p')

echo '===== 3. 订单详情（含明细） ====='
curl -s $BASE/api/orders/$OID; echo

echo '===== 4. 再买 1 件 → 库存不足应 400 ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders -H 'Content-Type: application/json' \
  -d "{\"userName\":\"u02\",\"items\":[{\"productId\":$PID,\"quantity\":1}]}"

echo '===== 5. 参数校验：items 为空 → 应 400 ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders -H 'Content-Type: application/json' \
  -d '{"userName":"u03","items":[]}'

echo '===== 6. 销售统计（JOIN + GROUP BY） ====='
curl -s $BASE/api/stats/sales; echo

echo '===== 7. 每日销售额统计 ====='
curl -s $BASE/api/stats/daily; echo