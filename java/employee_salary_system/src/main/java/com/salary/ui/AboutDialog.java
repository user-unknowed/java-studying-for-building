package com.salary.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 关于对话框
 * 显示系统名称、版本号、开发者信息、JDK版本等系统信息
 * 
 * @author salary-system
 * @version 1.0
 */
public class AboutDialog extends JDialog {
    
    // 系统信息常量
    private static final String SYSTEM_NAME = "员工工资管理系统";    // 系统名称
    private static final String VERSION = "1.0";                     // 版本号
    private static final String DEVELOPER = "薪资系统开发团队";      // 开发者
    private static final String COPYRIGHT = "版权所有 © 2024";      // 版权信息
    private static final String DESCRIPTION = "一款高效、稳定的员工工资管理系统";  // 系统描述
    
    // 组件
    private JButton okButton;  // 确定按钮
    
    /**
     * 构造函数，初始化关于对话框
     * @param parent 父窗口
     */
    public AboutDialog(Frame parent) {
        // 调用父类构造函数，设置模态
        super(parent, "关于", true);
        
        initComponents();
        setupLayout();
        setupEventListeners();
        
        // 设置对话框大小和位置
        setSize(450, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    /**
     * 初始化所有组件
     */
    private void initComponents() {
        // 确定按钮
        okButton = new JButton("确定");
        okButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        okButton.setPreferredSize(new Dimension(100, 35));
        okButton.setBackground(new Color(70, 130, 180));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setToolTipText("关闭关于对话框");
    }
    
    /**
     * 设置对话框布局
     */
    private void setupLayout() {
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        mainPanel.setBackground(new Color(240, 248, 255));
        
        // ===== 顶部：系统图标和名称 =====
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(new Color(240, 248, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        
        // 系统图标（使用文字代替）
        JLabel iconLabel = new JLabel("💰");
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(iconLabel);
        
        // 系统名称
        JLabel nameLabel = new JLabel(SYSTEM_NAME);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        nameLabel.setForeground(new Color(70, 130, 180));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(nameLabel);
        
        // 版本号
        JLabel versionLabel = new JLabel("版本 " + VERSION);
        versionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        versionLabel.setForeground(Color.GRAY);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(versionLabel);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // ===== 中部：系统详细信息 =====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // 创建信息项
        centerPanel.add(createInfoRow("系统名称", SYSTEM_NAME));
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createSeparator());
        centerPanel.add(Box.createVerticalStrut(12));
        
        centerPanel.add(createInfoRow("版本号", VERSION));
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createSeparator());
        centerPanel.add(Box.createVerticalStrut(12));
        
        centerPanel.add(createInfoRow("开发者", DEVELOPER));
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createSeparator());
        centerPanel.add(Box.createVerticalStrut(12));
        
        centerPanel.add(createInfoRow("JDK版本", getJdkVersion()));
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createSeparator());
        centerPanel.add(Box.createVerticalStrut(12));
        
        centerPanel.add(createInfoRow("Java版本", System.getProperty("java.version")));
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createSeparator());
        centerPanel.add(Box.createVerticalStrut(12));
        
        centerPanel.add(createInfoRow("操作系统", System.getProperty("os.name") + " " + System.getProperty("os.version")));
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(createSeparator());
        centerPanel.add(Box.createVerticalStrut(12));
        
        centerPanel.add(createInfoRow("系统架构", System.getProperty("os.arch")));
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // ===== 底部：版权信息和确定按钮 =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(new Color(240, 248, 255));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        
        // 系统描述
        JLabel descLabel = new JLabel("<html><center>" + DESCRIPTION + "</center></html>");
        descLabel.setFont(new Font("微软雅黑", Font.ITALIC, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(descLabel);
        
        // 版权信息
        JLabel copyrightLabel = new JLabel(COPYRIGHT);
        copyrightLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        copyrightLabel.setForeground(Color.GRAY);
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(copyrightLabel);
        
        // 确定按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(okButton);
        bottomPanel.add(buttonPanel);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建信息行
     * @param label 标签文本
     * @param value 值文本
     * @return 包含标签和值的面板
     */
    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel labelComponent = new JLabel(label + "：");
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 13));
        labelComponent.setForeground(new Color(80, 80, 80));
        labelComponent.setPreferredSize(new Dimension(100, 25));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        valueComponent.setForeground(new Color(50, 50, 50));
        
        panel.add(labelComponent);
        panel.add(valueComponent);
        
        return panel;
    }
    
    /**
     * 创建分隔线
     * @return 分隔线面板
     */
    private JPanel createSeparator() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(Color.WHITE);
        
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setForeground(new Color(220, 220, 220));
        panel.add(separator);
        
        return panel;
    }
    
    /**
     * 获取JDK版本信息
     * @return JDK版本字符串
     */
    private String getJdkVersion() {
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        
        // 解析主版本号
        if (javaVersion.startsWith("1.8")) {
            return "JDK 1.8.0 (" + javaVendor + ")";
        } else if (javaVersion.startsWith("11")) {
            return "JDK 11 (" + javaVendor + ")";
        } else if (javaVersion.startsWith("17")) {
            return "JDK 17 (" + javaVendor + ")";
        } else if (javaVersion.startsWith("21")) {
            return "JDK 21 (" + javaVendor + ")";
        } else {
            return javaVersion + " (" + javaVendor + ")";
        }
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 确定按钮事件 - 使用匿名内部类
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();  // 关闭对话框
            }
        });
        
        // 确定按钮键盘事件 - 回车触发关闭
        okButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dispose();
                }
            }
        });
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 模态对话框，直接关闭即可
            }
        });
    }
    
    /**
     * 显示关于对话框的静态方法
     * @param parent 父窗口
     */
    public static void showAbout(Frame parent) {
        AboutDialog dialog = new AboutDialog(parent);
        dialog.setVisible(true);
    }
    
    /**
     * 主函数，用于测试
     */
    public static void main(String[] args) {
        // 设置Swing外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在事件分发线程中创建对话框
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AboutDialog.showAbout(null);
            }
        });
    }
}
