package com.salary.ui;

import com.salary.model.Score;
import com.salary.service.ScoreService;
import com.salary.exception.SalaryException;
import com.salary.util.ExcelUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 成绩分析面板
 * 提供成绩的统计分析功能，包括优秀率计算、不及格学生筛选等
 * 
 * @author salary-system
 * @version 1.0
 */
public class ScoreAnalysisPanel extends JPanel {
    
    // 科目选择相关组件
    private JComboBox<String> subjectComboBox;  // 科目下拉框
    private JButton queryButton;                 // 查询按钮
    
    // 优秀率计算相关组件
    private JTextField excellentStandardField;   // 优秀标准输入框（默认90分）
    private JLabel excellentRateLabel;           // 优秀率显示标签
    private JButton calculateExcellentRateButton;  // 计算优秀率按钮
    
    // 不及格学生筛选相关组件
    private JTextField passingScoreField;        // 及格线输入框（默认60分）
    private JTable failedStudentTable;           // 不及格学生列表
    private DefaultTableModel tableModel;        // 表格数据模型
    private JButton exportFailedListButton;     // 导出不及格名单按钮
    
    // 统计信息显示组件
    private JLabel totalCountLabel;              // 参考人数标签
    private JLabel avgScoreLabel;                // 平均分标签
    private JLabel maxScoreLabel;                // 最高分标签
    private JLabel minScoreLabel;                // 最低分标签
    private JLabel passRateLabel;                // 及格率标签
    
    // 操作按钮
    private JButton refreshButton;               // 刷新按钮
    
    // 业务服务
    private ScoreService scoreService;           // 成绩服务
    
    // 当前选中的科目
    private String currentSubject;               // 当前选中的科目名称
    
    // 表格列名
    private static final String[] TABLE_HEADERS = {"学号", "姓名", "科目", "分数", "班级"};
    
    /**
     * 构造函数，初始化成绩分析面板
     */
    public ScoreAnalysisPanel() {
        this.scoreService = new ScoreService();
        initComponents();
        setupLayout();
        setupEventListeners();
    }
    
    /**
     * 初始化所有组件
     */
    private void initComponents() {
        // 科目下拉框初始化
        String[] subjects = {"数学", "语文", "英语"};
        subjectComboBox = new JComboBox<>(subjects);
        subjectComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subjectComboBox.setPreferredSize(new Dimension(120, 30));
        
        // 查询按钮
        queryButton = new JButton("查询");
        queryButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        queryButton.setPreferredSize(new Dimension(80, 30));
        queryButton.setFocusPainted(false);
        
        // 优秀标准输入框（默认90分）
        excellentStandardField = new JTextField("90", 8);
        excellentStandardField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        excellentStandardField.setPreferredSize(new Dimension(80, 30));
        
        // 优秀率显示标签
        excellentRateLabel = new JLabel("0.0%");
        excellentRateLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        excellentRateLabel.setForeground(new Color(0, 100, 0));  // 深绿色
        excellentRateLabel.setPreferredSize(new Dimension(100, 30));
        
        // 计算优秀率按钮
        calculateExcellentRateButton = new JButton("计算优秀率");
        calculateExcellentRateButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        calculateExcellentRateButton.setPreferredSize(new Dimension(120, 35));
        calculateExcellentRateButton.setBackground(new Color(60, 179, 113));
        calculateExcellentRateButton.setForeground(Color.WHITE);
        calculateExcellentRateButton.setFocusPainted(false);
        
        // 及格线输入框（默认60分）
        passingScoreField = new JTextField("60", 8);
        passingScoreField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passingScoreField.setPreferredSize(new Dimension(80, 30));
        
        // 不及格学生表格初始化
        tableModel = new DefaultTableModel(TABLE_HEADERS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // 表格单元格不可编辑
            }
        };
        failedStudentTable = new JTable(tableModel);
        failedStudentTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        failedStudentTable.setRowHeight(25);
        failedStudentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        failedStudentTable.getTableHeader().setBackground(new Color(135, 206, 235));
        
        // 导出不及格名单按钮
        exportFailedListButton = new JButton("导出不及格名单");
        exportFailedListButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        exportFailedListButton.setPreferredSize(new Dimension(150, 35));
        exportFailedListButton.setBackground(new Color(255, 140, 0));
        exportFailedListButton.setForeground(Color.WHITE);
        exportFailedListButton.setFocusPainted(false);
        
        // 刷新按钮
        refreshButton = new JButton("刷新");
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.setBackground(new Color(70, 130, 180));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        
        // 统计信息标签初始化
        totalCountLabel = new JLabel("0");
        totalCountLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalCountLabel.setForeground(new Color(25, 25, 112));
        
        avgScoreLabel = new JLabel("0.0");
        avgScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        avgScoreLabel.setForeground(new Color(25, 25, 112));
        
        maxScoreLabel = new JLabel("0.0");
        maxScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        maxScoreLabel.setForeground(new Color(25, 25, 112));
        
        minScoreLabel = new JLabel("0.0");
        minScoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        minScoreLabel.setForeground(new Color(25, 25, 112));
        
        passRateLabel = new JLabel("0.0%");
        passRateLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        passRateLabel.setForeground(new Color(25, 25, 112));
    }
    
    /**
     * 设置面板布局
     */
    private void setupLayout() {
        // 使用BorderLayout布局
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 248, 255));
        
        // ===== 顶部：科目选择区域 =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(new Color(240, 248, 255));
        
        JLabel subjectLabel = new JLabel("选择科目：");
        subjectLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        topPanel.add(subjectLabel);
        topPanel.add(subjectComboBox);
        topPanel.add(queryButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // ===== 中部：分析结果展示区域 =====
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 248, 255));
        
        // 优秀率计算区域
        JPanel excellentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        excellentPanel.setBackground(new Color(245, 255, 245));
        excellentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 179, 113), 2),
            "优秀率计算",
            0, 0,
            new Font("微软雅黑", Font.BOLD, 14),
            new Color(0, 100, 0)
        ));
        
        JLabel standardLabel = new JLabel("优秀标准（分）：");
        standardLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        JLabel excellentRateTitleLabel = new JLabel("优秀率：");
        excellentRateTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        excellentPanel.add(standardLabel);
        excellentPanel.add(excellentStandardField);
        excellentPanel.add(Box.createHorizontalStrut(20));
        excellentPanel.add(excellentRateTitleLabel);
        excellentPanel.add(excellentRateLabel);
        excellentPanel.add(Box.createHorizontalStrut(20));
        excellentPanel.add(calculateExcellentRateButton);
        
        // 统计信息展示区域
        JPanel statisticsPanel = new JPanel(new GridLayout(1, 5, 15, 5));
        statisticsPanel.setBackground(new Color(230, 240, 250));
        statisticsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2),
            "统计信息",
            0, 0,
            new Font("微软雅黑", Font.BOLD, 14),
            new Color(70, 130, 180)
        ));
        
        // 参考人数
        JPanel countPanel = createStatItemPanel("参考人数", totalCountLabel);
        statisticsPanel.add(countPanel);
        
        // 平均分
        JPanel avgPanel = createStatItemPanel("平均分", avgScoreLabel);
        statisticsPanel.add(avgPanel);
        
        // 最高分
        JPanel maxPanel = createStatItemPanel("最高分", maxScoreLabel);
        statisticsPanel.add(maxPanel);
        
        // 最低分
        JPanel minPanel = createStatItemPanel("最低分", minScoreLabel);
        statisticsPanel.add(minPanel);
        
        // 及格率
        JPanel passRatePanel = createStatItemPanel("及格率", passRateLabel);
        statisticsPanel.add(passRatePanel);
        
        // 不及格学生筛选区域
        JPanel failedPanel = new JPanel(new BorderLayout(10, 10));
        failedPanel.setBackground(new Color(255, 245, 238));
        failedPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(255, 140, 0), 2),
            "不及格学生筛选",
            0, 0,
            new Font("微软雅黑", Font.BOLD, 14),
            new Color(255, 140, 0)
        ));
        
        // 及格线设置面板
        JPanel passingScorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        passingScorePanel.setBackground(new Color(255, 245, 238));
        
        JLabel passingLabel = new JLabel("及格线（分）：");
        passingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        passingScorePanel.add(passingLabel);
        passingScorePanel.add(passingScoreField);
        passingScorePanel.add(Box.createHorizontalStrut(20));
        passingScorePanel.add(exportFailedListButton);
        
        // 表格滚动面板
        JScrollPane tableScrollPane = new JScrollPane(failedStudentTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 200));
        
        failedPanel.add(passingScorePanel, BorderLayout.NORTH);
        failedPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // 组装中部面板
        centerPanel.add(excellentPanel, BorderLayout.NORTH);
        centerPanel.add(statisticsPanel, BorderLayout.CENTER);
        centerPanel.add(failedPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // ===== 底部：操作按钮区域 =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(240, 248, 255));
        
        bottomPanel.add(refreshButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建统计项面板
     * @param title 统计项标题
     * @param valueLabel 值标签
     * @return 统计项面板
     */
    private JPanel createStatItemPanel(String title, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(230, 240, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);
        
        return panel;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        // 查询按钮事件 - 使用匿名内部类
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleQuery();
            }
        });
        
        // 科目下拉框选择事件
        subjectComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 当科目变化时，可以自动刷新数据
            }
        });
        
        // 计算优秀率按钮事件
        calculateExcellentRateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCalculateExcellentRate();
            }
        });
        
        // 导出不及格名单按钮事件
        exportFailedListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExportFailedList();
            }
        });
        
        // 刷新按钮事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRefresh();
            }
        });
        
        // 及格线输入框键盘事件 - 回车触发查询
        passingScoreField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleQueryFailedStudents();
                }
            }
        });
        
        // 优秀标准输入框键盘事件 - 回车触发计算
        excellentStandardField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleCalculateExcellentRate();
                }
            }
        });
    }
    
    /**
     * 处理查询操作
     * 获取选中文科的统计信息和不及格学生列表
     */
    private void handleQuery() {
        try {
            // 获取选中的科目
            currentSubject = (String) subjectComboBox.getSelectedItem();
            
            // 获取统计信息
            loadStatistics();
            
            // 获取不及格学生列表
            handleQueryFailedStudents();
            
            JOptionPane.showMessageDialog(
                this,
                currentSubject + "科目成绩查询成功！",
                "查询成功",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "查询失败：" + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 加载统计信息
     */
    private void loadStatistics() {
        try {
            Map<String, Object> stats = scoreService.getSubjectStatistics(currentSubject);
            
            // 更新统计信息显示
            int count = stats.get("count") != null ? ((Number) stats.get("count")).intValue() : 0;
            double avgScore = stats.get("avgScore") != null ? ((Number) stats.get("avgScore")).doubleValue() : 0.0;
            double maxScore = stats.get("maxScore") != null ? ((Number) stats.get("maxScore")).doubleValue() : 0.0;
            double minScore = stats.get("minScore") != null ? ((Number) stats.get("minScore")).doubleValue() : 0.0;
            double passRate = stats.get("passRate") != null ? ((Number) stats.get("passRate")).doubleValue() : 0.0;
            
            totalCountLabel.setText(String.valueOf(count));
            avgScoreLabel.setText(String.format("%.1f", avgScore));
            maxScoreLabel.setText(String.format("%.1f", maxScore));
            minScoreLabel.setText(String.format("%.1f", minScore));
            passRateLabel.setText(String.format("%.1f%%", passRate * 100));
        } catch (SalaryException ex) {
            // 如果没有数据，显示默认值
            totalCountLabel.setText("0");
            avgScoreLabel.setText("0.0");
            maxScoreLabel.setText("0.0");
            minScoreLabel.setText("0.0");
            passRateLabel.setText("0.0%");
        }
    }
    
    /**
     * 处理查询不及格学生操作
     */
    private void handleQueryFailedStudents() {
        try {
            // 获取选中的科目
            if (currentSubject == null) {
                currentSubject = (String) subjectComboBox.getSelectedItem();
            }
            
            // 获取及格线
            String passingScoreText = passingScoreField.getText().trim();
            double passingScore;
            try {
                passingScore = Double.parseDouble(passingScoreText);
                if (passingScore < 0 || passingScore > 100) {
                    JOptionPane.showMessageDialog(
                        this,
                        "及格线必须在0-100之间！",
                        "输入错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "请输入有效的及格线分数！",
                    "输入错误",
                    JOptionPane.ERROR_MESSAGE
                );
                passingScoreField.requestFocus();
                return;
            }
            
            // 查询不及格学生
            List<Score> failedStudents = scoreService.findFailedStudents(currentSubject, passingScore);
            
            // 清空表格
            tableModel.setRowCount(0);
            
            // 填充表格数据
            for (Score score : failedStudents) {
                Object[] rowData = {
                    score.getStudentCode(),
                    score.getStudentName(),
                    score.getSubject(),
                    score.getScore() != null ? score.getScore().toString() : "0",
                    ""  // 班级字段暂时为空
                };
                tableModel.addRow(rowData);
            }
            
        } catch (SalaryException ex) {
            JOptionPane.showMessageDialog(
                this,
                "查询不及格学生失败：" + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 处理计算优秀率操作
     */
    private void handleCalculateExcellentRate() {
        try {
            // 获取选中的科目
            if (currentSubject == null) {
                currentSubject = (String) subjectComboBox.getSelectedItem();
            }
            
            // 获取优秀标准
            String standardText = excellentStandardField.getText().trim();
            double excellentStandard;
            try {
                excellentStandard = Double.parseDouble(standardText);
                if (excellentStandard < 0 || excellentStandard > 100) {
                    JOptionPane.showMessageDialog(
                        this,
                        "优秀标准必须在0-100之间！",
                        "输入错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "请输入有效的优秀标准分数！",
                    "输入错误",
                    JOptionPane.ERROR_MESSAGE
                );
                excellentStandardField.requestFocus();
                return;
            }
            
            // 计算优秀率
            double excellentRate = scoreService.calculateExcellentRate(currentSubject, excellentStandard);
            
            // 更新显示
            excellentRateLabel.setText(String.format("%.2f%%", excellentRate * 100));
            
        } catch (SalaryException ex) {
            JOptionPane.showMessageDialog(
                this,
                "计算优秀率失败：" + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 处理导出不及格名单操作
     */
    private void handleExportFailedList() {
        try {
            // 获取选中的科目
            if (currentSubject == null) {
                currentSubject = (String) subjectComboBox.getSelectedItem();
            }
            
            // 获取及格线
            String passingScoreText = passingScoreField.getText().trim();
            double passingScore;
            try {
                passingScore = Double.parseDouble(passingScoreText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "请输入有效的及格线分数！",
                    "输入错误",
                    JOptionPane.ERROR_MESSAGE
                );
                passingScoreField.requestFocus();
                return;
            }
            
            // 弹出文件保存对话框
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出不及格学生名单");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setSelectedFile(new java.io.File(
                currentSubject + "不及格学生名单.xls"
            ));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                
                // 验证文件格式
                if (!ExcelUtil.isValidExcelFile(filePath)) {
                    JOptionPane.showMessageDialog(
                        this,
                        "文件格式不正确，请使用.xls或.xlsx格式！",
                        "文件格式错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                
                // 导出数据
                scoreService.exportFailedStudentsToExcel(currentSubject, passingScore, filePath);
                
                JOptionPane.showMessageDialog(
                    this,
                    "不及格学生名单已成功导出到：\n" + filePath,
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
            
        } catch (SalaryException ex) {
            JOptionPane.showMessageDialog(
                this,
                "导出失败：" + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    /**
     * 处理刷新操作
     * 清空所有输入和显示，恢复默认值
     */
    private void handleRefresh() {
        // 确认是否刷新
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要刷新吗？当前未保存的数据将会丢失。",
            "刷新确认",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            // 重置科目选择
            subjectComboBox.setSelectedIndex(0);
            currentSubject = null;
            
            // 重置优秀标准
            excellentStandardField.setText("90");
            excellentRateLabel.setText("0.0%");
            
            // 重置及格线
            passingScoreField.setText("60");
            
            // 清空表格
            tableModel.setRowCount(0);
            
            // 重置统计信息
            totalCountLabel.setText("0");
            avgScoreLabel.setText("0.0");
            maxScoreLabel.setText("0.0");
            minScoreLabel.setText("0.0");
            passRateLabel.setText("0.0%");
            
            JOptionPane.showMessageDialog(
                this,
                "刷新成功！",
                "提示",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
