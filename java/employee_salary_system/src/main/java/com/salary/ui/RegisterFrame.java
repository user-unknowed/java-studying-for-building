package com.salary.ui;

import com.salary.model.User;
import com.salary.service.UserService;
import com.salary.exception.SalaryException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 注册窗口
 * 提供新用户注册功能，包括用户名唯一性校验、密码一致性校验等
 * 
 * @author salary-system
 * @version 1.0
 */
public class RegisterFrame extends JFrame {
    
    // 输入组件
    private JTextField usernameField;           // 用户名输入框
    private JPasswordField passwordField;         // 密码输入框
    private JPasswordField confirmPasswordField; // 确认密码输入框
    private JTextField realNameField;             // 真实姓名输入框
    private JComboBox<String> roleComboBox;       // 角色选择下拉框
    
    // 按钮组件
    private JButton registerButton;              // 注册按钮
    private JButton cancelButton;                 // 取消按钮
    
    // 业务服务
    private UserService userService;             // 用户服务
    
    /**
     * 构造函数，初始化注册窗口
     */
    public RegisterFrame() {
        this.userService = new UserService();
        initComponents();
        setupLayout();
        setupEventListeners();
    }
    
    /**
     * 初始化所有组件
     */
    private void initComponents() {
        // 窗口基本设置
        setTitle("用户注册 - 员工工资管理系统");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 用户名输入框
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 30));
        
        // 密码输入框
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setEchoChar('*');
        
        // 确认密码输入框
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        confirmPasswordField.setPreferredSize(new Dimension(200, 30));
        confirmPasswordField.setEchoChar('*');
        
        // 真实姓名输入框
        realNameField = new JTextField(20);
        realNameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        realNameField.setPreferredSize(new Dimension(200, 30));
        
        // 角色选择下拉框
        String[] roles = {"user", "admin"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        roleComboBox.setPreferredSize(new Dimension(200, 30));
        roleComboBox.setBackground(Color.WHITE);
        
        // 注册按钮
        registerButton = new JButton("注册");
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setToolTipText("点击注册新用户");
        
        // 取消按钮
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.setBackground(new Color(128, 128, 128));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setToolTipText("取消注册并关闭窗口");
    }
    
    /**
     * 设置窗口布局
     */
    private void setupLayout() {
        // 主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.setBackground(new Color(240, 245, 250));
        
        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // 输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 1, 10, 15));
        inputPanel.setBackground(new Color(240, 245, 250));
        
        // 用户名输入行
        JPanel usernamePanel = createInputRow("用户名：", usernameField, 
            "4-20位字母、数字或下划线");
        inputPanel.add(usernamePanel);
        
        // 密码输入行
        JPanel passwordPanel = createInputRow("密码：", passwordField, 
            "至少6位字符");
        inputPanel.add(passwordPanel);
        
        // 确认密码输入行
        JPanel confirmPanel = createInputRow("确认密码：", confirmPasswordField, 
            "再次输入密码");
        inputPanel.add(confirmPanel);
        
        // 真实姓名输入行
        JPanel realNamePanel = createInputRow("真实姓名：", realNameField, 
            "输入您的真实姓名");
        inputPanel.add(realNamePanel);
        
        // 角色选择行
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setBackground(new Color(240, 245, 250));
        
        JLabel roleLabel = new JLabel("角色：");
        roleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        roleLabel.setPreferredSize(new Dimension(100, 30));
        
        rolePanel.add(roleLabel);
        rolePanel.add(roleComboBox);
        
        inputPanel.add(rolePanel);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 245, 250));
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        // 组装主面板
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * 创建输入行面板
     * @param labelText 标签文本
     * @param inputComponent 输入组件
     * @param hintText 提示文本
     * @return 输入行面板
     */
    private JPanel createInputRow(String labelText, JComponent inputComponent, String hintText) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 245, 250));
        
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(100, 30));
        
        JLabel hintLabel = new JLabel(hintText);
        hintLabel.setFont(new Font("微软雅黑", Font.ITALIC, 11));
        hintLabel.setForeground(Color.GRAY);
        
        panel.add(label);
        panel.add(inputComponent);
        panel.add(hintLabel);
        
        return panel;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 注册按钮事件 - 使用匿名内部类
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        
        // 取消按钮事件 - 使用匿名内部类
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancel();
            }
        });
        
        // 用户名输入框键盘事件 - 回车跳转到密码框
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });
        
        // 密码输入框键盘事件 - 回车跳转到确认密码框
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmPasswordField.requestFocus();
                }
            }
        });
        
        // 确认密码输入框键盘事件 - 回车跳转到真实姓名框
        confirmPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realNameField.requestFocus();
                }
            }
        });
        
        // 真实姓名输入框键盘事件 - 回车触发注册
        realNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleRegister();
                }
            }
        });
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleCancel();
            }
        });
    }
    
    /**
     * 处理注册操作
     * 验证输入数据并调用服务层完成注册
     */
    private void handleRegister() {
        // 获取输入数据
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String realName = realNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();
        
        // ===== 输入验证 =====
        
        // 验证用户名
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
        
        // 用户名格式验证：4-20位字母、数字或下划线
        if (!username.matches("^[a-zA-Z0-9_]{4,20}$")) {
            JOptionPane.showMessageDialog(
                this,
                "用户名格式不正确！\n用户名必须为4-20位字母、数字或下划线",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            usernameField.requestFocus();
            usernameField.selectAll();
            return;
        }
        
        // 验证密码
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
        
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(
                this,
                "密码长度不能少于6位！",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            passwordField.requestFocus();
            passwordField.selectAll();
            return;
        }
        
        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "请再次输入密码！",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            confirmPasswordField.requestFocus();
            return;
        }
        
        // 验证密码一致性
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                this,
                "两次输入的密码不一致！",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocus();
            return;
        }
        
        // 验证真实姓名
        if (realName.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "真实姓名不能为空！",
                "输入错误",
                JOptionPane.ERROR_MESSAGE
            );
            realNameField.requestFocus();
            return;
        }
        
        // ===== 调用服务层注册 =====
        
        try {
            // 创建用户对象
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRealName(realName);
            user.setRole("admin".equals(role) ? "管理员" : "普通用户");
            
            // 调用服务层注册
            Long userId = userService.register(user);
            
            // 注册成功提示
            JOptionPane.showMessageDialog(
                this,
                "注册成功！\n您的用户ID是：" + userId + "\n请牢记您的用户名和密码！",
                "注册成功",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // 关闭窗口
            this.dispose();
            
        } catch (SalaryException ex) {
            // 注册失败，根据错误类型显示不同提示
            String errorMessage = ex.getMessage();
            
            if (errorMessage.contains("用户名已存在")) {
                JOptionPane.showMessageDialog(
                    this,
                    "用户名已被占用，请选择其他用户名！",
                    "注册失败",
                    JOptionPane.ERROR_MESSAGE
                );
                usernameField.requestFocus();
                usernameField.selectAll();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "注册失败：" + errorMessage,
                    "错误",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "系统错误：" + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 处理取消操作
     * 确认后关闭注册窗口
     */
    private void handleCancel() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要取消注册吗？\n已输入的信息将不会保存。",
            "取消确认",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
        }
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
        
        // 在事件分发线程中创建窗口
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RegisterFrame().setVisible(true);
            }
        });
    }
}
