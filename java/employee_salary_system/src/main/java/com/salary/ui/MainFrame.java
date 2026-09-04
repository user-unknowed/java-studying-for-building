package com.salary.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 主窗口
 */
public class MainFrame extends JFrame {
    private String currentUser;
    private JSplitPane splitPane;
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private Timer timer;

    // 功能面板
    private JPanel employeePanel;
    private JPanel statisticsPanel;
    private JPanel queryPanel;
    private JPanel scoreAnalysisPanel;

    public MainFrame(String username) {
        this.currentUser = username;
        initComponents();
        startClock();
    }

    private void initComponents() {
        // 窗口基本设置
        setTitle("员工工资管理系统 - 主界面");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建菜单栏
        setJMenuBar(createMenuBar());

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 顶部标题栏
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);

        // 中间分栏
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);
        splitPane.setLeftComponent(createNavigationPanel());
        splitPane.setRightComponent(createContentPanel());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 底部状态栏
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    /**
     * 创建菜单栏
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setFont(new Font("微软雅黑", Font.PLAIN, 13));

        JMenuItem exitItem = new JMenuItem("退出(X)", KeyEvent.VK_X);
        exitItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        exitItem.setToolTipText("退出系统");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleWindowClosing();
            }
        });
        fileMenu.add(exitItem);

        // 系统菜单
        JMenu systemMenu = new JMenu("系统(S)");
        systemMenu.setMnemonic(KeyEvent.VK_S);
        systemMenu.setFont(new Font("微软雅黑", Font.PLAIN, 13));

        JMenuItem aboutItem = new JMenuItem("关于(A)", KeyEvent.VK_A);
        aboutItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        aboutItem.setToolTipText("查看系统信息");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "员工工资管理系统 v1.0\n\n用于管理员工信息和工资统计",
                    "关于",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        systemMenu.add(aboutItem);

        JMenuItem helpItem = new JMenuItem("帮助(H)", KeyEvent.VK_H);
        helpItem.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        helpItem.setToolTipText("获取帮助");
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "如有疑问，请联系系统管理员",
                    "帮助",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        systemMenu.add(helpItem);

        menuBar.add(fileMenu);
        menuBar.add(systemMenu);

        return menuBar;
    }

    /**
     * 创建顶部标题栏
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 左侧标题
        JLabel titleLabel = new JLabel("员工工资管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 右侧用户信息和退出按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setOpaque(false);

        JLabel userLabel = new JLabel("当前用户：" + currentUser);
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 80, 80));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setToolTipText("退出当前登录，返回登录界面");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });

        rightPanel.add(userLabel);
        rightPanel.add(logoutButton);

        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    /**
     * 创建左侧导航面板
     */
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(new Color(230, 236, 245));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // 导航标题
        JLabel navTitle = new JLabel("功能菜单");
        navTitle.setFont(new Font("微软雅黑", Font.BOLD, 16));
        navTitle.setForeground(new Color(70, 130, 180));
        navTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        navPanel.add(navTitle);
        navPanel.add(Box.createVerticalStrut(20));

        // 员工信息管理按钮
        JButton employeeBtn = createNavButton("员工信息管理", "查看和管理员工信息");
        employeeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("employee");
            }
        });

        // 工资统计按钮
        JButton statisticsBtn = createNavButton("工资统计", "统计和分析工资数据");
        statisticsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("statistics");
            }
        });

        // 数据查询按钮
        JButton queryBtn = createNavButton("数据查询", "查询员工和工资信息");
        queryBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("query");
            }
        });

        // 成绩分析按钮
        JButton scoreBtn = createNavButton("成绩分析", "分析员工绩效成绩");
        scoreBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPanel("score");
            }
        });

        navPanel.add(employeeBtn);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(statisticsBtn);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(queryBtn);
        navPanel.add(Box.createVerticalStrut(10));
        navPanel.add(scoreBtn);

        // 添加一个弹性区域将按钮推向上方
        navPanel.add(Box.createVerticalGlue());

        return navPanel;
    }

    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(170, 45));
        button.setMaximumSize(new Dimension(170, 45));
        button.setMinimumSize(new Dimension(170, 45));
        button.setFocusPainted(false);
        button.setBackground(new Color(245, 245, 245));
        button.setForeground(new Color(60, 60, 60));
        button.setToolTipText(tooltip);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // 鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(245, 245, 245));
                button.setForeground(new Color(60, 60, 60));
            }
        });

        return button;
    }

    /**
     * 创建右侧功能面板区域
     */
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        cardLayout = new CardLayout();
        panel.setLayout(cardLayout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建功能面板
        employeePanel = createEmployeePanel();
        statisticsPanel = createStatisticsPanel();
        queryPanel = createQueryPanel();
        scoreAnalysisPanel = createScoreAnalysisPanel();

        // 添加到卡片布局
        panel.add(employeePanel, "employee");
        panel.add(statisticsPanel, "statistics");
        panel.add(queryPanel, "query");
        panel.add(scoreAnalysisPanel, "score");

        // 默认显示员工面板
        cardLayout.show(panel, "employee");

        return panel;
    }

    /**
     * 切换显示面板
     */
    private void showPanel(String panelName) {
        cardLayout.show(contentPanel, panelName);
    }

    /**
     * 创建员工信息管理面板
     */
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // 标题
        JLabel title = new JLabel("员工信息管理");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        title.setForeground(new Color(70, 130, 180));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(title, BorderLayout.NORTH);

        // 内容区域
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        content.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("<html><body style='font-size:14px;'>"
            + "<h2>员工信息管理功能</h2>"
            + "<p>• 添加新员工信息</p>"
            + "<p>• 修改员工信息</p>"
            + "<p>• 删除员工信息</p>"
            + "<p>• 查看员工列表</p>"
            + "<p>• 导入/导出员工数据</p>"
            + "</body></html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(infoLabel, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建工资统计面板
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("工资统计");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        title.setForeground(new Color(70, 130, 180));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        content.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("<html><body style='font-size:14px;'>"
            + "<h2>工资统计功能</h2>"
            + "<p>• 按部门统计工资</p>"
            + "<p>• 按时间周期统计</p>"
            + "<p>• 工资报表生成</p>"
            + "<p>• 工资对比分析</p>"
            + "<p>• 导出统计报表</p>"
            + "</body></html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(infoLabel, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建数据查询面板
     */
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("数据查询");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        title.setForeground(new Color(70, 130, 180));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        content.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("<html><body style='font-size:14px;'>"
            + "<h2>数据查询功能</h2>"
            + "<p>• 按工号查询</p>"
            + "<p>• 按姓名查询</p>"
            + "<p>• 按部门查询</p>"
            + "<p>• 按日期范围查询</p>"
            + "<p>• 高级组合查询</p>"
            + "</body></html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(infoLabel, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建成绩分析面板
     */
    private JPanel createScoreAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("成绩分析");
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        title.setForeground(new Color(70, 130, 180));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        content.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("<html><body style='font-size:14px;'>"
            + "<h2>成绩分析功能</h2>"
            + "<p>• 员工绩效评分</p>"
            + "<p>• 成绩分布统计</p>"
            + "<p>• 同比环比分析</p>"
            + "<p>• 成绩趋势图表</p>"
            + "<p>• 导出分析报告</p>"
            + "</body></html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.add(infoLabel, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建底部状态栏
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        JLabel versionLabel = new JLabel("  版本 1.0");
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        statusPanel.add(versionLabel, BorderLayout.WEST);
        statusPanel.add(statusLabel, BorderLayout.EAST);

        return statusPanel;
    }

    /**
     * 启动时钟
     */
    private void startClock() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTime();
            }
        });
        timer.start();
        updateTime();
    }

    /**
     * 更新时间显示
     */
    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
        statusLabel.setText(sdf.format(new Date()) + "  ");
    }

    /**
     * 处理退出登录
     */
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出登录吗？",
            "退出登录",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            if (timer != null) {
                timer.stop();
            }

            // 打开登录窗口
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);

            // 关闭主窗口
            this.dispose();
        }
    }

    /**
     * 处理窗口关闭
     */
    private void handleWindowClosing() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "确定要退出系统吗？",
            "退出确认",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            if (timer != null) {
                timer.stop();
            }
            System.exit(0);
        }
    }
}
