package com.learn.order.service;

import com.learn.order.common.BizException;
import com.learn.order.model.CreateOrderRequest;
import com.learn.order.model.Order;
import com.learn.order.model.OrderDetail;
import com.learn.order.model.OrderItem;
import com.learn.order.model.Product;
import com.learn.order.repo.OrderRepository;
import com.learn.order.repo.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 业务层：承载「下单」等业务规则与事务边界 */
@Service
public class OrderService {

    private final ProductRepository products;
    private final OrderRepository orders;

    public OrderService(ProductRepository products, OrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    /**
     * 下单 = 扣库存 + 建订单 + 写明细
     *
     * <p>@Transactional：整个方法在一个数据库事务里执行。
     * 任何一步抛异常，全部操作回滚 —— 不会出现「扣了库存但订单没建成」的中间态。
     * 对比 JavaSE/Java×SQL 章节手写的 connection.setAutoCommit(false) + commit/rollback：
     * 现在只需一行注解。</p>
     *
     * <p>并发细节：循环里「先 UPDATE 扣库存、后 SELECT 读单价」的顺序不是随手写的——
     * SQLite WAL 下需要写操作先行，否则高并发时会出现快照冲突（见教程第 10 章实战记录）。</p>
     */
    @Transactional
    public long placeOrder(CreateOrderRequest req) {
        long total = 0;
        List<OrderItem> pending = new ArrayList<>();

        for (CreateOrderRequest.Item it : req.items()) {
            // ① 先扣库存（写操作先行）——
            // SQLite WAL 模式下，事务"先读后写"时，若快照期间其他事务已提交，
            // 写操作会因快照过期直接失败（SQLITE_BUSY）；让事务的第一条语句就是写，
            // 并发请求即可干净地排队等待写锁。与原生版 place() 的语句顺序一致。
            int affected = products.decreaseStock(it.productId(), it.quantity());
            if (affected == 0) {
                // 扣不动：区分「商品不存在」与「库存不足」，给调用方准确原因
                Product p = products.findById(it.productId())
                        .orElseThrow(() -> new BizException("商品不存在: id=" + it.productId()));
                throw new BizException("库存不足: " + p.name()
                        + "（剩余 " + p.stock() + "，需要 " + it.quantity() + "）");
            }

            // ② 读取单价（同一事务内、写锁已持有，数据一致）
            Product p = products.findById(it.productId())
                    .orElseThrow(() -> new BizException("商品不存在: id=" + it.productId()));
            total += p.priceCents() * it.quantity();
            pending.add(new OrderItem(0, 0, p.id(), p.name(), p.priceCents(), it.quantity()));
        }

        long orderId = orders.insertOrder(req.userName(), total);
        for (OrderItem item : pending) {
            orders.insertItem(orderId, item);
        }
        return orderId;
    }

    /** 查询订单详情（订单 + 明细） */
    public OrderDetail detail(long orderId) {
        Order order = orders.findById(orderId)
                .orElseThrow(() -> new BizException("订单不存在: id=" + orderId));
        return new OrderDetail(order, orders.findItems(orderId));
    }
}