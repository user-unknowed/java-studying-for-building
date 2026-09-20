package com.demo.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** 商品接口：GET /api/products 、 GET /api/products/{id} */
public class ProductApiServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private final ProductDao dao = new ProductDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                write(resp, 200, dao.list());
            } else {
                long id = Long.parseLong(path.substring(1));
                var p = dao.get(id);
                if (p == null) {
                    write(resp, 404, Map.of("error", "商品不存在: id=" + id));
                } else {
                    write(resp, 200, p);
                }
            }
        } catch (NumberFormatException e) {
            write(resp, 400, Map.of("error", "非法的商品 id"));
        } catch (Exception e) {
            write(resp, 500, Map.of("error", e.getMessage()));
        }
    }

    private void write(HttpServletResponse resp, int code, Object data) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }
}