package com.demo.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * REST API 路由（Web 层）：
 *
 *   GET  /api/books               图书列表
 *   GET  /api/books/{id}          图书详情
 *   GET  /api/books/popular       借阅排行榜（JOIN 统计）
 *   POST /api/books               新增图书        {"title":"...","author":"...","stock":3}
 *   POST /api/books/{id}/borrow   借书            {"borrower":"张三"}
 *   POST /api/books/{id}/return   还书            {"borrower":"张三"}
 */
public class BookApiServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private final BookDao dao = new BookDao();

    // ==================== GET ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                ok(resp, dao.list());
            } else if (path.equals("/popular")) {
                ok(resp, dao.popular());
            } else {
                long id = Long.parseLong(path.substring(1));
                Book b = dao.get(id);
                if (b == null) {
                    fail(resp, 404, "图书不存在: id=" + id);
                } else {
                    ok(resp, b);
                }
            }
        } catch (NumberFormatException e) {
            fail(resp, 400, "非法的图书 id");
        } catch (Exception e) {
            fail(resp, 500, e.getMessage());
        }
    }

    // ==================== POST ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/")) {
                // 新增图书
                JsonObject body = readBody(req);
                Book created = dao.create(
                        body.get("title").getAsString(),
                        body.get("author").getAsString(),
                        body.has("stock") ? body.get("stock").getAsInt() : 1);
                resp.setStatus(201);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write(GSON.toJson(created));
                return;
            }

            String[] seg = path.split("/"); // 形如 ["", "3", "borrow"]
            if (seg.length == 3) {
                long id = Long.parseLong(seg[1]);
                String action = seg[2];
                String borrower = readBody(req).get("borrower").getAsString();
                switch (action) {
                    case "borrow" -> {
                        String r = dao.borrow(id, borrower);
                        if (r.equals("OK")) {
                            ok(resp, Map.of("result", "借阅成功", "book", dao.get(id)));
                        } else if (r.equals("NO_STOCK")) {
                            fail(resp, 409, "库存不足，借阅失败");
                        } else {
                            fail(resp, 404, "图书不存在: id=" + id);
                        }
                    }
                    case "return" -> {
                        String r = dao.giveBack(id, borrower);
                        if (r.equals("OK")) {
                            ok(resp, Map.of("result", "归还成功", "book", dao.get(id)));
                        } else {
                            fail(resp, 409, "没有该借阅人的未归还记录");
                        }
                    }
                    default -> fail(resp, 400, "未知操作: " + action);
                }
                return;
            }
            fail(resp, 400, "不支持的路径: " + path);
        } catch (NullPointerException | IllegalStateException | UnsupportedOperationException e) {
            fail(resp, 400, "请求体缺少必要字段或格式错误");
        } catch (NumberFormatException e) {
            fail(resp, 400, "非法的图书 id");
        } catch (Exception e) {
            fail(resp, 500, e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================
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

    private void ok(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }

    private void fail(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        JsonObject o = new JsonObject();
        o.addProperty("error", message);
        resp.getWriter().write(GSON.toJson(o));
    }
}
