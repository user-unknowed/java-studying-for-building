import java.sql.*;

/**
 * Step3: 事务 (Transaction) —— Java 业务逻辑与数据库一致性的完美配合
 *
 * 场景: 银行转账: 张三 → 李四 100 元。
 * 数据库中必须"扣款"和"加款"【同时成功】或【同时失败】。
 *
 * 重点体验:
 *   1. setAutoCommit(false) 开启事务
 *   2. commit() 提交
 *   3. 异常时 rollback() 回滚
 *   4. 对比: 没有事务保护时, 钱会凭空消失!
 *
 * 这就是为什么"事务"必须由数据库提供, 而不是 Java 自己想办法 ——
 * 只有数据库知道"这两条 UPDATE 是一个整体"。
 */
public class Step3_Transaction {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:" + (args.length > 0 ? args[0] : "bank.db");
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection(url)) {
            setup(conn);
            resetData(conn);

            System.out.println("========== 场景1: 正常转账 100 元 ==========");
            showBalances(conn);
            transfer(conn, "张三", "李四", 100.00);
            showBalances(conn);
            System.out.println("观察: 张三-100, 李四+100, 总资产不变 ✓");

            System.out.println("\n========== 场景2: 转账到不存在的账户 → 失败并回滚 ==========");
            showBalances(conn);
            try {
                transfer(conn, "张三", "不存在的人", 100.00);
                System.out.println("咦? 不应该走到这里");
            } catch (SQLException e) {
                System.out.println("转账失败: " + e.getMessage());
            }
            showBalances(conn);
            System.out.println("观察: 张三的扣款被【回滚】, 余额没有变化 ✓");

            System.out.println("\n========== 场景3: 对比 —— 没有事务保护会怎样? ==========");
            showBalances(conn);
            brokenTransfer(conn, "张三", "不存在的人", 100.00);
            showBalances(conn);
            System.out.println("观察: 张三的钱扣了, 但没有加到任何人头上 —— 100 元凭空消失 ✗");
            System.out.println("      这就是【数据不一致】, 事务就是防它的防线。");
        }
    }

    /** 正确示范: 一个事务包裹"扣款 + 收款", 要么全成, 要么全败 */
    static void transfer(Connection conn, String from, String to, double amount) throws SQLException {
        conn.setAutoCommit(false); // ① 开启事务: 从这里开始, 操作进入"暂存区"
        try {
            // ② 扣款
            executeUpdate(conn, "UPDATE account SET balance = balance - ? WHERE owner = ?", amount, from);
            System.out.println("   [事务内] " + from + " -" + amount);

            // ③ 收款 (若对方不存在, 影响行数为 0 → 视为业务失败)
            int rows = executeUpdate(conn, "UPDATE account SET balance = balance + ? WHERE owner = ?", amount, to);
            if (rows == 0) throw new SQLException("收款方账户不存在, 转账中止!");

            conn.commit(); // ④ 全部成功才提交
            System.out.println("   [事务] 提交成功: 转账完成 ✓");
        } catch (SQLException e) {
            conn.rollback(); // ⑤ 任何一步失败, 全部回滚 (暂存区的操作全部撤销)
            System.out.println("   [事务] 检测到失败 → 已回滚, 余额保持原样");
            throw e;
        } finally {
            conn.setAutoCommit(true); // 恢复默认的自动提交模式
        }
    }

    /** 错误示范: 没有事务, 两步操作各自独立提交 —— 中途失败就会留下"半截"数据 */
    static void brokenTransfer(Connection conn, String from, String to, double amount) throws SQLException {
        executeUpdate(conn, "UPDATE account SET balance = balance - ? WHERE owner = ?", amount, from);
        System.out.println("   [无事务] " + from + " -" + amount + " (已自动提交, 无法撤销)");
        System.out.println("   [无事务] 此时系统故障... 收款操作未能执行! (钱消失了)");
    }

    static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        }
    }

    static void setup(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS account (
                        id      INTEGER PRIMARY KEY,
                        owner   TEXT NOT NULL,
                        balance REAL NOT NULL DEFAULT 0
                    )""");
        }
    }

    static void resetData(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("DELETE FROM account");
            st.execute("INSERT INTO account(owner, balance) VALUES ('张三', 1000), ('李四', 500)");
        }
    }

    static void showBalances(Connection conn) throws SQLException {
        double total = 0;
        StringBuilder sb = new StringBuilder("   当前余额: ");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT owner, balance FROM account ORDER BY id")) {
            while (rs.next()) {
                sb.append(rs.getString("owner")).append("=").append(rs.getDouble("balance")).append("  ");
                total += rs.getDouble("balance");
            }
        }
        sb.append(" | 总资产=").append(total);
        System.out.println(sb);
    }
}