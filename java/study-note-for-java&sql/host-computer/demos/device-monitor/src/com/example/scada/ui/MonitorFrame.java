package com.example.scada.ui;

import com.example.scada.db.MonitorDb;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;

/**
 * Swing 监控窗口（在 PC 桌面运行）。
 *
 * <p>注意：本教程的开发环境是无显示终端（headless），GUI 无法运行 ——
 * 本类做"编译级验证"；在你的电脑上执行 <code>java ... Main gui</code> 即可弹出窗口。
 */
public class MonitorFrame extends JFrame {

    private final MonitorDb db;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"时间", "温度℃", "湿度%", "光照lx", "土壤%", "状态"}, 0);
    private final JLabel statusBar = new JLabel(" ");

    public MonitorFrame(String dbFile) throws Exception {
        super("温室环境监控上位机 — DeviceMonitor");
        this.db = new MonitorDb(dbFile);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(760, 420);
        setLocationRelativeTo(null);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusBar, BorderLayout.SOUTH);

        new Timer(2000, e -> refresh()).start(); // 每 2 秒刷新
        refresh();
    }

    private void refresh() {
        try {
            // 演示版：直接读最近 20 条（真实项目可用增量查询避免全量重绘）
            model.setRowCount(0);
            for (String[] row : db.recentReadingRows(20)) {
                model.addRow(row);
            }
            statusBar.setText("数据源: SQLite · 刷新于 " + java.time.LocalTime.now().withNano(0));
        } catch (Exception e) {
            statusBar.setText("刷新失败: " + e.getMessage());
        }
    }

    /** 入口辅助：由 Main 调用。 */
    public static void launch(String dbFile) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MonitorFrame(dbFile).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}