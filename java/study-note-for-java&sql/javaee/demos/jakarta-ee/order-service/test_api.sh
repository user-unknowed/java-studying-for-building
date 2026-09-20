#!/bin/bash
# order-service API 冒烟测试 + 完整演示序列
# 前置：先启动服务（java -cp "out:lib/*" com.demo.order.Main）
BASE=http://localhost:8082

echo '===== 0. 商品列表（种子数据） ====='
curl -s $BASE/api/products
echo; echo

echo '===== 1. 下单：u01 买 1 件机械键盘（399） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u01","items":[{"productId":1,"quantity":1}]}'
echo; echo

echo '===== 2. 下单：u02 买 2 件机械键盘 ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u02","items":[{"productId":1,"quantity":2}]}'
echo '----- 键盘当前库存（应为 5-1-2=2） -----'
curl -s $BASE/api/products/1
echo; echo

echo '===== 3. 订单2 详情（含明细） ====='
curl -s $BASE/api/orders/2
echo; echo

echo '===== 4. 取消订单2 → 库存回滚（应为 2+2=4） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders/2/cancel
echo '----- 键盘库存 -----'
curl -s $BASE/api/products/1
echo; echo

echo '===== 5. 重复取消订单2 → 应拒绝（409） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders/2/cancel
echo; echo

echo '===== 6. 超量下单：买 9 件键盘（库存仅 4）→ 应拒绝（409，事务回滚） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"userId":"u03","items":[{"productId":1,"quantity":9}]}'
echo; echo

echo '===== 7. 并发压测：15 个并发抢购 3 台 4K显示器（防超卖验证） ====='
python3 stress_test.py 15 3 1
echo; echo

echo '===== 8. 销量统计（取消订单不计入） ====='
curl -s $BASE/api/stats/sales
echo; echo

echo '===== 9. 订单概况 ====='
curl -s $BASE/api/stats/summary
echo