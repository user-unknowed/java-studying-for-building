package com.learn.order.web;

import com.learn.order.model.CreateOrderRequest;
import com.learn.order.model.OrderDetail;
import com.learn.order.model.Product;
import com.learn.order.repo.OrderRepository;
import com.learn.order.repo.ProductRepository;
import com.learn.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 表现层（Controller）：只做「接收请求 → 调 Service → 返回结果」。
 *
 * <p>不写业务逻辑、不碰数据库 —— 这就是「分层架构」的纪律。</p>
 */
@RestController
@RequestMapping("/api")
public class ShopController {

    private final OrderService orderService;
    private final ProductRepository products;
    private final OrderRepository orders;

    public ShopController(OrderService orderService, ProductRepository products, OrderRepository orders) {
        this.orderService = orderService;
        this.products = products;
        this.orders = orders;
    }

    /** 商品列表 */
    @GetMapping("/products")
    public List<Product> listProducts() {
        return products.findAll();
    }

    /** 新增商品（教学便利接口） */
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createProduct(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        long priceCents = ((Number) body.get("priceCents")).longValue();
        int stock = ((Number) body.getOrDefault("stock", 0)).intValue();
        long id = products.insert(name, priceCents, stock);
        return Map.of("id", id, "name", name, "priceCents", priceCents, "stock", stock);
    }

    /** 下单（核心接口） */
    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> placeOrder(@Valid @RequestBody CreateOrderRequest req) {
        long orderId = orderService.placeOrder(req);
        return Map.of("orderId", orderId, "status", "PAID");
    }

    /** 订单详情（含明细） */
    @GetMapping("/orders/{id}")
    public OrderDetail orderDetail(@PathVariable long id) {
        return orderService.detail(id);
    }

    /** 销售排行统计 */
    @GetMapping("/stats/sales")
    public List<Map<String, Object>> sales() {
        return orders.salesByProduct();
    }

    /** 每日销售额统计 */
    @GetMapping("/stats/daily")
    public List<Map<String, Object>> daily() {
        return orders.dailySales();
    }
}