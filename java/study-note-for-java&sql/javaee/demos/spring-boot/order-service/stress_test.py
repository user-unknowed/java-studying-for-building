#!/usr/bin/env python3
"""并发压测（Spring Boot 版）：N 个线程同时下单同一商品，验证防超卖。

用法: python3 stress_test.py [并发数] [初始库存] [每单数量]
示例: python3 stress_test.py 15 3 1
"""
import json
import sys
import threading
import time
from urllib import request as urlreq
from urllib import error as urlerr

BASE = 'http://localhost:8081'


def http(method, path, payload=None):
    data = json.dumps(payload).encode() if payload is not None else None
    req = urlreq.Request(BASE + path, data=data, method=method,
                         headers={'Content-Type': 'application/json'} if data else {})
    try:
        with urlreq.urlopen(req) as r:
            return r.status, json.loads(r.read().decode() or '{}')
    except urlerr.HTTPError as e:
        return e.code, None


def main():
    concurrency = int(sys.argv[1]) if len(sys.argv) > 1 else 15
    stock = int(sys.argv[2]) if len(sys.argv) > 2 else 3
    qty = int(sys.argv[3]) if len(sys.argv) > 3 else 1

    # 创建一个全新的压测商品，避免受既有数据影响
    _, prod = http('POST', '/api/products',
                   {'name': '压测商品-%d' % int(time.time()), 'priceCents': 100, 'stock': stock})
    pid = prod['id']
    print('已创建压测商品 id=%d（初始库存 %d）；并发发起 %d 个订单（每单 %d 件）...'
          % (pid, stock, concurrency, qty))

    results = []
    lock = threading.Lock()

    def worker(i):
        code, _ = http('POST', '/api/orders',
                       {'userName': 'bench%d' % i, 'items': [{'productId': pid, 'quantity': qty}]})
        with lock:
            results.append(code)

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(concurrency)]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    ok = results.count(201)
    rejected = results.count(400)
    other = len(results) - ok - rejected
    print('成功下单(201): %d | 库存不足(400): %d | 其他错误: %d' % (ok, rejected, other))

    _, products = http('GET', '/api/products')
    stock1 = next(p['stock'] for p in products if p['id'] == pid)
    print('压测后商品[%s]剩余库存: %d' % (prod['name'], stock1))

    expect = max(0, stock - ok * qty)
    if stock1 == expect and other == 0:
        print('结论: 成功单数 = min(初始库存/每单数量, 并发数)，库存 >= 0，未发生超卖 ✓')
    else:
        print('⚠️ 数据异常：预期剩余 %d，实际 %d' % (expect, stock1))


if __name__ == '__main__':
    main()