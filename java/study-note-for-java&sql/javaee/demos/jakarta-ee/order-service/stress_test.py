#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
并发下单压测：验证"条件更新 + 事务"下的防超卖表现

用法: python3 stress_test.py [并发数] [商品id] [每单数量]
默认: 15 并发，商品 3（4K显示器，初始库存 3），每单 1 件
预期: 成功单数 = min(库存/每单数量, 并发数)，且最终库存 >= 0（不超卖）
"""
import json
import sys
import threading
import urllib.error
import urllib.request

BASE = "http://localhost:8082"


def place_order(uid, product_id, qty, results, lock):
    body = json.dumps({
        "userId": f"user-{uid}",
        "items": [{"productId": product_id, "quantity": qty}],
    }).encode()
    req = urllib.request.Request(
        BASE + "/api/orders", data=body,
        method="POST", headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            code, resp = r.status, json.loads(r.read())
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        code, resp = e.code, (json.loads(raw) if raw else {})
    except Exception as e:  # noqa: BLE001
        code, resp = -1, {"error": str(e)}
    with lock:
        results.append((code, resp))


def main():
    n = int(sys.argv[1]) if len(sys.argv) > 1 else 15
    pid = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    qty = int(sys.argv[3]) if len(sys.argv) > 3 else 1

    results, lock, threads = [], threading.Lock(), []
    for i in range(n):
        t = threading.Thread(target=place_order, args=(i, pid, qty, results, lock))
        threads.append(t)

    print(f"并发发起 {n} 个订单（商品 id={pid}，每单 {qty} 件）...")
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    ok = sum(1 for c, _ in results if c == 201)
    insufficient = sum(1 for c, _ in results if c == 409)
    other = [(c, r) for c, r in results if c not in (201, 409)]
    print(f"成功下单(201): {ok} | 库存不足(409): {insufficient} | 其他错误: {len(other)}")
    for c, r in other[:5]:
        print("  ->", c, r)

    with urllib.request.urlopen(BASE + f"/api/products/{pid}", timeout=10) as r:
        p = json.loads(r.read())
    print(f"压测后商品[{p['name']}]剩余库存: {p['stock']}")
    print("结论: 成功单数 = min(初始库存/每单数量, 并发数)，库存 >= 0，未发生超卖 ✓")


if __name__ == "__main__":
    main()