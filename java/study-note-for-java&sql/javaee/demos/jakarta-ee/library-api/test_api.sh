#!/bin/bash
# library-api API 冒烟测试 + 完整演示序列
# 前置：先启动服务（java -cp "out:lib/*" com.demo.library.Main）
BASE=http://localhost:8081

echo '===== 0. 图书列表（初始为空） ====='
curl -s $BASE/api/books
echo; echo

echo '===== 1. 新增三本图书 ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"深入理解Java虚拟机","author":"周志明","stock":2}'
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"Java并发编程实战","author":"Brian Goetz","stock":1}'
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"SQL必知必会","author":"Ben Forta","stock":3}'
echo; echo

echo '===== 2. 图书列表 ====='
curl -s $BASE/api/books
echo; echo

echo '===== 3. 张三借《深入理解Java虚拟机》（库存 2→1） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books/1/borrow \
  -H 'Content-Type: application/json' -d '{"borrower":"张三"}'
echo; echo

echo '===== 4. 李四也借同一本（库存 1→0） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books/1/borrow \
  -H 'Content-Type: application/json' -d '{"borrower":"李四"}'
echo; echo

echo '===== 5. 王五再借 → 库存不足（409） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books/1/borrow \
  -H 'Content-Type: application/json' -d '{"borrower":"王五"}'
echo; echo

echo '===== 6. 张三还书（库存 0→1） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books/1/return \
  -H 'Content-Type: application/json' -d '{"borrower":"张三"}'
echo; echo

echo '===== 7. 王五借《Java并发编程实战》（唯一 1 本） ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/books/2/borrow \
  -H 'Content-Type: application/json' -d '{"borrower":"王五"}'
echo; echo

echo '===== 8. 借阅排行榜（LEFT JOIN + GROUP BY 统计） ====='
curl -s $BASE/api/books/popular
echo; echo

echo '===== 9. 不存在的书（404） ====='
curl -s -w '\nHTTP %{http_code}\n' $BASE/api/books/999
echo