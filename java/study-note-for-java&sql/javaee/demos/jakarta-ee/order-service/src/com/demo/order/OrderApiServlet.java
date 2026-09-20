package com.demo.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单接口：
 *   POST /api/orders               下单  {"userId":"u01","items":[{"productId":1,"quantity":2}]}
 *   GET  /api/orders/{id}          订单详情
 *   POST /api/orders/{id}/cancel   取消订单（库存回滚）
 */
public class OrderApiServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private final OrderDao dao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                write(resp, 400, Map.of("error", "请使用 GET /api/orders/{id} 查询订单详情"));
                return;
            }
            long id = Long.parseLong(path.substring(1));
            var order = dao.get(id);
            if (order == null) {
                write(resp, 404, Map.of("error", "订单不存在: id=" + id));
            } else {
                write(resp, 200, order);
            }
        } catch (NumberFormatException e) {
            write(resp, 400, Map.of("error", "非法的订单 id"));
        } catch (Exception e) {
            write(resp, 500, Map.of("error", e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                // 下单
                JsonObject body = readBody(req);
                String userId = body.get("userId").getAsString();
                JsonArray arr = body.getAsJsonArray("items");
                List<OrderDao.Item> items = new ArrayList<>();
                for (var el : arr) {
                    JsonObject o = el.getAsJsonObject();
                    OrderDao.Item it = new OrderDao.Item();
                    it.productId = o.get("productId").getAsLong();
                    it.quantity = o.get("quantity").getAsInt();
                    items.add(it);
                }
                Map<String, Object> result = dao.place(userId, items);
                write(resp, 201, result);
                return;
            }

            String[] seg = path.split("/"); // ["", "5", "cancel"]
            if (seg.length == 3 && seg[2].equals("cancel")) {
                long id = Long.parseLong(seg[1]);
                write(resp, 200, dao.cancel(id));
                return;
            }
            write(resp, 400, Map.of("error", "不支持的路径: " + path));
        } catch (OrderDao.BizException e) {
            write(resp, e.httpCode, Map.of("error", e.getMessage()));
        } catch (NullPointerException | IllegalStateException e) {
            write(resp, 400, Map.of("error", "请求体缺少必要字段或格式错误"));
        } catch (NumberFormatException e) {
            write(resp, 400, Map.of("error", "非法数字格式"));
        } catch (Exception e) {
            write(resp, 500, Map.of("error", e.getMessage()));
        }
    }

    private JsonObject readBody(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
        }
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    private void write(HttpServletResponse resp, int code, Object data) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }
}