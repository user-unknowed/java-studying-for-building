#!/usr/bin/env bash
# 短链接服务 API 测试脚本（需服务已在 8083 端口运行）
BASE=http://localhost:8083

echo '===== 0. 短链列表（含种子数据） ====='
curl -s $BASE/api/links; echo

echo '===== 1. 创建短链（自动短码） ====='
R=$(curl -s -X POST $BASE/api/links -H 'Content-Type: application/json' \
  -d '{"originalUrl":"https://github.com/user-unknowed/java-studying-for-building/tree/main/java"}')
echo "$R"
CODE=$(echo "$R" | sed -n 's/.*"code":"\([^"]*\)".*/\1/p')
echo "生成短码: $CODE"

echo '===== 2. 创建短链（自定义短码 github） ====='
curl -s -X POST $BASE/api/links -H 'Content-Type: application/json' \
  -d '{"originalUrl":"github.com","customCode":"github"}'; echo

echo '===== 3. 自定义短码重复 → 应 409 ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/links -H 'Content-Type: application/json' \
  -d '{"originalUrl":"https://example.com","customCode":"github"}'

echo '===== 4. 访问短链 → 应 302 跳转（看 Location 头） ====='
curl -s -o /dev/null -D - $BASE/$CODE | grep -E 'HTTP|Location'

echo '===== 5. 再点两次（累计 3 次点击） ====='
curl -s -o /dev/null $BASE/$CODE
curl -s -o /dev/null $BASE/$CODE

echo '===== 6. 短码统计（clicks 应为 3） ====='
curl -s $BASE/api/links/$CODE; echo

echo '===== 7. 不存在的短码 → 应 404 ====='
curl -s -w '\nHTTP %{http_code}\n' $BASE/api/links/nope404
curl -s -o /dev/null -w '跳转不存在的短码: HTTP %{http_code}\n' $BASE/nope404

echo '===== 8. 参数校验失败（originalUrl 为空） → 应 400 ====='
curl -s -w '\nHTTP %{http_code}\n' -X POST $BASE/api/links -H 'Content-Type: application/json' \
  -d '{"originalUrl":""}'

echo '===== 9. 最终短链列表 ====='
curl -s $BASE/api/links; echo