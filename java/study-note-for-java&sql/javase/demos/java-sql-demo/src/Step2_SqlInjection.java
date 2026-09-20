import java.sql.*;

/**
 * Step2: SQL 注入攻击演示与防御
 *
 * 场景: 最常见的"按姓名查账户"功能。
 *
 *   危险写法: 把用户输入直接拼进 SQL 字符串
 *       -> 用户输入变成了 SQL 代码的一部分, 可以"篡改"语义!
 *
 *   安全写法: PreparedStatement 预编译 + 占位符 ?
 *       -> SQL 结构先编译定型, 参数永远只是"数据", 不可能变成"代码"
 *
 * 这就是为什么 JDBC / MyBatis 都强制推荐预编译参数 (MyBatis 的 #{} 就是它)。
 */
public class Step2_SqlInjection {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:" + (args.length > 0 ? args[0] : "bank.db");
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection(url)) {
            setup(conn);

            // 三个"用户输入": 正常输入 / 经典注入 / 毁灭性注入
            String[] inputs = {
                    "张三",
                    "' OR '1'='1",
                    "王五'; DROP TABLE account; --"
            };

            System.out.println("========== [危险] 字符串拼接 SQL ==========");
            for (String input : inputs) {
                riskyQuery(conn, input);
                System.out.println();
            }
            System.out.println(">>> 恢复数据, 继续演示安全写法\n");
            setup(conn); // 如果表被注入攻击删了, 这里重建

            System.out.println("========== [安全] PreparedStatement 预编译 ==========");
            for (String input : inputs) {
                safeQuery(conn, input);
                System.out.println();
            }

            System.out.println("Step2 完成 ✓");
            System.out.println("结论: 攻击字符串在预编译里只是普通的'一串字符', 不具备任何破坏力。");
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
            st.execute("DELETE FROM account");
            st.execute("INSERT INTO account(owner, balance) VALUES ('张三', 1000), ('李四', 500)");
        }
    }

    /** [危险] 直接拼接: 用户输入混入了 SQL 语法 */
    static void riskyQuery(Connection conn, String userInput) throws SQLException {
        String sql = "SELECT owner, balance FROM account WHERE owner = '" + userInput + "'";
        System.out.println("SQL: " + sql);
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("     查到 → " + rs.getString("owner") + ", 余额 " + rs.getDouble("balance"));
            }
            System.out.println("     共 " + count + " 条");
        } catch (SQLException e) {
            System.out.println("     执行出错: " + e.getMessage());
        }
    }

    /** [安全] 预编译: 结构是结构, 数据是数据, 永不混淆 */
    static void safeQuery(Connection conn, String userInput) throws SQLException {
        String sql = "SELECT owner, balance FROM account WHERE owner = ?";
        System.out.println("SQL: " + sql + "   (参数: " + userInput + ")");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userInput); // 无论内容多恶劣, 都只被当作一个普通字符串值
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("     查到 → " + rs.getString("owner") + ", 余额 " + rs.getDouble("balance"));
                }
                System.out.println("     共 " + count + " 条");
            }
        }
    }
}