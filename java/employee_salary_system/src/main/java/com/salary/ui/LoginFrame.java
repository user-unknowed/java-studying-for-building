package com.salary.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 登录界面
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        // 窗口基本设置
        setTitle("员工工资管理系统 - 登录");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(240, 245, 250));

        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("员工工资管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);

        // 输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 1, 5, 10));
        inputPanel.setBackground(new Color(240, 245, 250));

        // 用户名
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.setBackground(new Color(240, 245, 250));
        JLabel usernameLabel = new JLabel("用户名：");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField = new JTextField(18);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(180, 30));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);

        // 密码
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setBackground(new Color(240, 245, 250));
        JLabel passwordLabel = new JLabel("密  码：");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField = new JPasswordField(18);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(180, 30));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(240, 245, 250));

        loginButton = new JButton("登录");
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setToolTipText("输入用户名和密码后登录系统");

        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.setBackground(new Color(100, 149, 237));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setToolTipText("注册新用户");

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        // 组装输入面板
        inputPanel.add(usernamePanel);
        inputPanel.add(passwordPanel);
        inputPanel.add(new JPanel());
        inputPanel.add(buttonPanel);

        // 组装主面板
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        add(mainPanel);

        // 登录按钮事件 - 使用匿名内部类
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // 注册按钮事件 - 使用匿名内部类
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });

        // 键盘事件处理 - 使用适配器，回车登录
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        usernameField.addKeyListener(keyAdapter);
        passwordField.addKeyListener(keyAdapter);

        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(
                    LoginFrame.this,
                    "确定要退出系统吗？",
                    "退出确认",
                    JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    /**
     * 处理登录逻辑
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // 输入验证
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "用户名不能为空！",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "密码不能为空！",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            passwordField.requestFocus();
            return;
        }

        // 这里可以添加实际的登录验证逻辑
        // 示例：简单验证
        if ("admin".equals(username) && "123456".equals(password)) {
            JOptionPane.showMessageDialog(
                this,
                "登录成功！",
                "提示",
                JOptionPane.INFORMATION_MESSAGE
            );

            // 打开主窗口
            MainFrame mainFrame = new MainFrame(username);
            mainFrame.setVisible(true);

            // 关闭登录窗口
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(
                this,
                "用户名或密码错误！",
                "登录失败",
                JOptionPane.ERROR_MESSAGE
            );
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    /**
     * 处理注册逻辑
     */
    private void handleRegister() {
        JOptionPane.showMessageDialog(
            this,
            "注册功能开发中...",
            "提示",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        // 设置Swing外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
