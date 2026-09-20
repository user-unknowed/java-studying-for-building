import java.sql.*;

/**
 * Step4: 批量操作 —— Java 与数据库"高效配合"的意义
 *
 * 对比 1000 条插入的三种方式:
 *   (a) 逐条执行:   每条 SQL 一个来回, 每个来回一次磁盘落盘
 *   (b) 批处理:     addBatch() 攒一批, executeBatch() 一次发送
 *   (c) 批处理+事务: 在批处理基础上, 用事务合并落盘次数 (工业标配)
 *
 * 结论: 同样的 Java 代码能力, 用法不同, 性能差距可达数十倍。
 * 这就是"懂数据库"的程序员和"只会写 SQL"的程序员的差距。
 */
public class Step4_BatchPerf {

    static final int N = 1000;

    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:" + (args.length > 0 ? args[0] : "bank.db");
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS tx_log (id INTEGER PRIMARY KEY, note TEXT, amount REAL)");
                st.execute("DELETE FROM tx_log");
            }

            // (a) 逐条插入
            conn.setAutoCommit(true);
            long t1 = System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tx_log(note, amount) VALUES (?, ?)")) {
                for (int i = 0; i < N; i++) {
                    ps.setString(1, "逐条-" + i);
                    ps.setDouble(2, i * 1.0);
                    ps.executeUpdate();
                }
            }
            long cost1 = System.currentTimeMillis() - t1;
            System.out.printf("(a) 逐条执行      : %5d 条, 耗时 %d ms%n", N, cost1);

            // (b) 批处理
            try (Statement st = conn.createStatement()) { st.execute("DELETE FROM tx_log"); }
            long t2 = System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tx_log(note, amount) VALUES (?, ?)")) {
                for (int i = 0; i < N; i++) {
                    ps.setString(1, "批处理-" + i);
                    ps.setDouble(2, i * 1.0);
                    ps.addBatch(); // 只入队, 不发车
                }
                ps.executeBatch(); // 一次发车
            }
            long cost2 = System.currentTimeMillis() - t2;
            System.out.printf("(b) 批处理        : %5d 条, 耗时 %d ms%n", N, cost2);

            // (c) 批处理 + 事务
            try (Statement st = conn.createStatement()) { st.execute("DELETE FROM tx_log"); }
            conn.setAutoCommit(false);
            long t3 = System.currentTimeMillis();
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tx_log(note, amount) VALUES (?, ?)")) {
                for (int i = 0; i < N; i++) {
                    ps.setString(1, "事务批-" + i);
                    ps.setDouble(2, i * 1.0);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            }
            long cost3 = System.currentTimeMillis() - t3;
            conn.setAutoCommit(true);
            System.out.printf("(c) 批处理+事务   : %5d 条, 耗时 %d ms%n", N, cost3);

            // 数据总量校验
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tx_log")) {
                rs.next();
                System.out.println("\n最终表中数据量: " + rs.getInt(1) + " 条 (三种方式结果相同, 速度天壤之别)");
            }
        }
        System.out.println("Step4 完成 ✓  提示: 网络数据库(MySQL)上差距会更夸张。");
    }
}