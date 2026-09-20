import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Step1: JDBC 六步全流程 + CRUD + "表 → Java 对象"的映射
 *
 * JDBC 六步:
 *   1. 加载驱动     Class.forName("org.sqlite.JDBC")
 *   2. 建立连接     DriverManager.getConnection(url)
 *   3. 创建语句对象 connection.createStatement() / prepareStatement()
 *   4. 执行 SQL     executeQuery() / executeUpdate()
 *   5. 处理结果集   ResultSet
 *   6. 关闭资源     try-with-resources 自动关闭
 *
 * 核心理解:
 *   - Java 是"业务的大脑",数据库是"数据的仓库",JDBC 是连接两者的桥
 *   - 数据库里的一行  <===>  Java 里的一个对象
 */
public class Step1_JdbcBasics {

    /** account 表 → Account 类的映射 */
    static class Account {
        long id;
        String owner;
        double balance;

        Account(long id, String owner, double balance) {
            this.id = id;
            this.owner = owner;
            this.balance = balance;
        }

        @Override
        public String toString() {
            return String.format("Account{id=%d, owner='%s', balance=%.2f}", id, owner, balance);
        }
    }

    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:" + (args.length > 0 ? args[0] : "bank.db");

        // ===== 1&2. 加载驱动 + 建立连接 =====
        Class.forName("org.sqlite.JDBC"); // JDBC 4.0+ 可省略, 显式写便于理解"驱动"的存在
        try (Connection conn = DriverManager.getConnection(url)) {
            System.out.println("[1] 连接成功: " + url);

            // ===== 建表 (DDL) =====
            try (Statement st = conn.createStatement()) {
                st.execute("""
                        CREATE TABLE IF NOT EXISTS account (
                            id      INTEGER PRIMARY KEY,
                            owner   TEXT NOT NULL,
                            balance REAL NOT NULL DEFAULT 0
                        )""");
                System.out.println("[2] 建表完成: account(id, owner, balance)");
            }

            // ===== INSERT: 用占位符 ? 传参 (防注入, 见 Step2) =====
            String insertSql = "INSERT INTO account(owner, balance) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, "张三");
                ps.setDouble(2, 1000.00);
                ps.executeUpdate();

                ps.setString(1, "李四");
                ps.setDouble(2, 500.00);
                ps.executeUpdate();
                System.out.println("[3] INSERT 完成: 插入 张三/李四 两条记录");
            }

            // ===== SELECT + 映射为 Java 对象列表 =====
            List<Account> accounts = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT id, owner, balance FROM account ORDER BY id")) {
                while (rs.next()) { // 游标逐行移动, 每行 = 一个 Java 对象
                    accounts.add(new Account(
                            rs.getLong("id"),
                            rs.getString("owner"),
                            rs.getDouble("balance")));
                }
            }
            System.out.println("[4] SELECT 结果映射为 Java 对象:");
            for (Account a : accounts) System.out.println("      " + a);

            // ===== UPDATE =====
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE account SET balance = balance + ? WHERE owner = ?")) {
                ps.setDouble(1, 88.88);
                ps.setString(2, "张三");
                int rows = ps.executeUpdate();
                System.out.println("[5] UPDATE 张三 +88.88, 影响行数: " + rows);
            }

            // ===== 聚合查询: 用 SQL 的强大算力, Java 只负责读取 =====
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                         "SELECT COUNT(*) AS cnt, SUM(balance) AS total FROM account")) {
                if (rs.next()) {
                    System.out.println("[6] 聚合查询: 账户数=" + rs.getInt("cnt")
                            + ", 总资产=" + rs.getDouble("total"));
                }
            }

            // ===== DELETE =====
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM account WHERE owner = ?")) {
                ps.setString(1, "李四");
                System.out.println("[7] DELETE 李四, 影响行数: " + ps.executeUpdate());
            }
        }

        System.out.println("\nStep1 完成 ✓");
        System.out.println("关键认识: 数据保存在数据库文件里, 而不是 Java 的内存里。");
        System.out.println("Java 进程结束了, 数据依然在 —— 这就是'持久化'的意义。");
    }
}