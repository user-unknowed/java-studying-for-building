package com.salary.ui;

import com.salary.dao.EmployeeDAO;
import com.salary.dao.SalaryRecordDAO;
import com.salary.exception.SalaryException;
import com.salary.model.Employee;
import com.salary.model.SalaryRecord;
import com.salary.util.ExcelUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;

/**
 * 工资统计面板
 * 提供工资生成、统计和导出功能
 */
public class StatisticsPanel extends JPanel {
    
    // 月份选择
    private JComboBox<String> cmbMonth;
    
    // 按钮
    private JButton btnGenerateSalary;
    private JButton btnStatistics;
    private JButton btnExportReport;
    
    // 统计结果表格
    private DefaultTableModel statisticsTableModel;
    private JTable statisticsTable;
    
    // 汇总信息显示
    private JTextArea txtSummary;
    
    // DAO
    private SalaryRecordDAO salaryRecordDAO;
    private EmployeeDAO employeeDAO;
    
    // 日期格式
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 构造函数
     */
    public StatisticsPanel() {
        salaryRecordDAO = new SalaryRecordDAO();
        employeeDAO = new EmployeeDAO();
        initComponents();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 顶部：月份选择和操作按钮
        add(createTopPanel(), BorderLayout.NORTH);
        
        // 中部：统计结果表格
        add(createCenterPanel(), BorderLayout.CENTER);
        
        // 底部：汇总信息和导出按钮
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * 创建顶部面板
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("月份选择"));
        
        panel.add(new JLabel("选择月份:"));
        
        // 月份选择下拉框
        cmbMonth = new JComboBox<>();
        initializeMonthComboBox();
        panel.add(cmbMonth);
        
        btnGenerateSalary = new JButton("生成工资");
        btnGenerateSalary.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateSalary();
            }
        });
        
        btnStatistics = new JButton("统计");
        btnStatistics.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doStatistics();
            }
        });
        
        panel.add(btnGenerateSalary);
        panel.add(btnStatistics);
        
        return panel;
    }
    
    /**
     * 初始化月份下拉框
     */
    private void initializeMonthComboBox() {
        cmbMonth.removeAllItems();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 12; i++) {
            LocalDateTime targetMonth = now.minusMonths(i);
            String monthStr = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            cmbMonth.addItem(monthStr);
        }
    }
    
    /**
     * 创建中部面板（统计结果表格）
     */
    private JScrollPane createCenterPanel() {
        String[] columnNames = {"员工编号", "员工姓名", "部门", "岗位", "基本工资", "奖金", "扣款", "实发工资", "月份"};
        statisticsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        statisticsTable = new JTable(statisticsTableModel);
        statisticsTable.setRowHeight(25);
        statisticsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(statisticsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("工资记录"));
        
        return scrollPane;
    }
    
    /**
     * 创建底部面板
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("统计汇总"));
        
        // 汇总信息文本区
        txtSummary = new JTextArea(8, 40);
        txtSummary.setEditable(false);
        txtSummary.setLineWrap(true);
        txtSummary.setWrapStyleWord(true);
        txtSummary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane summaryScrollPane = new JScrollPane(txtSummary);
        
        // 导出按钮
        btnExportReport = new JButton("导出统计报表");
        btnExportReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnExportReport);
        
        panel.add(summaryScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 生成工资
     */
    private void generateSalary() {
        String selectedMonth = (String) cmbMonth.getSelectedItem();
        if (selectedMonth == null || selectedMonth.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择月份！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            List<Employee> employees = employeeDAO.findAll();
            if (employees.isEmpty()) {
                JOptionPane.showMessageDialog(this, "没有员工数据，请先添加员工！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int generatedCount = 0;
            for (Employee emp : employees) {
                // 检查该员工当月是否已有工资记录
                List<SalaryRecord> existingRecords = salaryRecordDAO.findByEmployeeId(emp.getId().intValue());
                boolean hasRecordForMonth = false;
                for (SalaryRecord record : existingRecords) {
                    if (selectedMonth.equals(record.getSalaryMonth())) {
                        hasRecordForMonth = true;
                        break;
                    }
                }
                
                if (!hasRecordForMonth) {
                    // 创建工资记录
                    SalaryRecord record = new SalaryRecord();
                    record.setEmployeeId(emp.getId());
                    record.setEmployeeCode(emp.getEmployeeCode());
                    record.setEmployeeName(emp.getName());
                    record.setPosition(emp.getPosition());
                    record.setDepartment(emp.getDepartment());
                    record.setSalaryMonth(selectedMonth);
                    
                    // 设置默认基本工资
                    BigDecimal baseSalary = emp.getBaseSalary() != null ? emp.getBaseSalary() : new BigDecimal("3000");
                    record.setBaseSalary(baseSalary);
                    
                    // 根据岗位计算工资
                    BigDecimal calculatedSalary = calculateSalaryByPosition(emp);
                    record.setCalculatedSalary(calculatedSalary);
                    
                    // 奖金和扣款默认为0
                    BigDecimal bonus = emp.getBonus() != null ? emp.getBonus() : BigDecimal.ZERO;
                    BigDecimal deductions = emp.getDeductions() != null ? emp.getDeductions() : BigDecimal.ZERO;
                    record.setBonus(bonus);
                    record.setDeductions(deductions);
                    
                    // 实发工资 = 计算工资 + 奖金 - 扣款
                    BigDecimal netSalary = calculatedSalary.add(bonus).subtract(deductions);
                    record.setNetSalary(netSalary);
                    
                    record.setCreateTime(LocalDateTime.now());
                    
                    salaryRecordDAO.insert(record);
                    generatedCount++;
                }
            }
            
            JOptionPane.showMessageDialog(this, "成功生成 " + generatedCount + " 条工资记录！", "成功", JOptionPane.INFORMATION_MESSAGE);
            doStatistics();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "生成工资失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 根据岗位计算工资
     */
    private BigDecimal calculateSalaryByPosition(Employee emp) {
        String position = emp.getPosition();
        if (position == null) {
            return emp.getBaseSalary() != null ? emp.getBaseSalary() : BigDecimal.ZERO;
        }
        
        switch (position) {
            case "经理":
                return new BigDecimal("8000");
            case "技术员":
                // 默认160小时工作时间
                return new BigDecimal("16000");
            case "销售员":
                // 默认销售额
                return new BigDecimal("5000").multiply(new BigDecimal("0.04")).add(new BigDecimal("3000"));
            case "销售经理":
                return new BigDecimal("5000");
            default:
                return emp.getBaseSalary() != null ? emp.getBaseSalary() : new BigDecimal("3000");
        }
    }
    
    /**
     * 执行统计
     */
    private void doStatistics() {
        String selectedMonth = (String) cmbMonth.getSelectedItem();
        if (selectedMonth == null || selectedMonth.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择月份！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // 加载该月的工资记录到表格
            statisticsTableModel.setRowCount(0);
            List<SalaryRecord> records = salaryRecordDAO.findByMonth(selectedMonth);
            
            BigDecimal totalSalary = BigDecimal.ZERO;
            BigDecimal totalBonus = BigDecimal.ZERO;
            BigDecimal totalDeductions = BigDecimal.ZERO;
            BigDecimal maxSalary = BigDecimal.ZERO;
            BigDecimal minSalary = BigDecimal.valueOf(Double.MAX_VALUE);
            
            // 按岗位统计
            Map<String, BigDecimal> positionSalaries = new HashMap<>();
            Map<String, Integer> positionCounts = new HashMap<>();
            
            // 按部门统计
            Map<String, BigDecimal> departmentSalaries = new HashMap<>();
            Map<String, Integer> departmentCounts = new HashMap<>();
            
            for (SalaryRecord record : records) {
                Vector<Object> row = new Vector<>();
                row.add(record.getEmployeeCode());
                row.add(record.getEmployeeName());
                row.add(record.getDepartment());
                row.add(record.getPosition());
                row.add(record.getBaseSalary());
                row.add(record.getBonus());
                row.add(record.getDeductions());
                row.add(record.getNetSalary());
                row.add(record.getSalaryMonth());
                statisticsTableModel.addRow(row);
                
                // 累计汇总
                BigDecimal netSalary = record.getNetSalary() != null ? record.getNetSalary() : BigDecimal.ZERO;
                totalSalary = totalSalary.add(netSalary);
                totalBonus = totalBonus.add(record.getBonus() != null ? record.getBonus() : BigDecimal.ZERO);
                totalDeductions = totalDeductions.add(record.getDeductions() != null ? record.getDeductions() : BigDecimal.ZERO);
                
                if (netSalary.compareTo(maxSalary) > 0) {
                    maxSalary = netSalary;
                }
                if (netSalary.compareTo(minSalary) < 0) {
                    minSalary = netSalary;
                }
                
                // 岗位统计
                String pos = record.getPosition();
                if (pos != null) {
                    positionSalaries.put(pos, positionSalaries.getOrDefault(pos, BigDecimal.ZERO).add(netSalary));
                    positionCounts.put(pos, positionCounts.getOrDefault(pos, 0) + 1);
                }
                
                // 部门统计
                String dept = record.getDepartment();
                if (dept != null) {
                    departmentSalaries.put(dept, departmentSalaries.getOrDefault(dept, BigDecimal.ZERO).add(netSalary));
                    departmentCounts.put(dept, departmentCounts.getOrDefault(dept, 0) + 1);
                }
            }
            
            // 生成汇总报告
            StringBuilder summary = new StringBuilder();
            summary.append("===========================================\n");
            summary.append("           工资统计报表\n");
            summary.append("===========================================\n");
            summary.append("统计月份: ").append(selectedMonth).append("\n");
            summary.append("生成时间: ").append(LocalDateTime.now().format(dateTimeFormatter)).append("\n");
            summary.append("---------------------------------------- ---\n");
            summary.append("\n【总体统计】\n");
            summary.append("工资记录数: ").append(records.size()).append(" 条\n");
            summary.append("工资总额: ¥").append(totalSalary.setScale(2, BigDecimal.ROUND_HALF_UP)).append("\n");
            summary.append("总奖金: ¥").append(totalBonus.setScale(2, BigDecimal.ROUND_HALF_UP)).append("\n");
            summary.append("总扣款: ¥").append(totalDeductions.setScale(2, BigDecimal.ROUND_HALF_UP)).append("\n");
            
            if (!records.isEmpty()) {
                summary.append("平均工资: ¥").append(totalSalary.divide(BigDecimal.valueOf(records.size()), 2, BigDecimal.ROUND_HALF_UP)).append("\n");
                summary.append("最高工资: ¥").append(maxSalary.setScale(2, BigDecimal.ROUND_HALF_UP)).append("\n");
                summary.append("最低工资: ¥").append(minSalary.setScale(2, BigDecimal.ROUND_HALF_UP)).append("\n");
            }
            
            summary.append("\n【岗位平均工资】\n");
            for (Map.Entry<String, BigDecimal> entry : positionSalaries.entrySet()) {
                String position = entry.getKey();
                BigDecimal total = entry.getValue();
                int count = positionCounts.get(position);
                BigDecimal avg = total.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
                summary.append(position).append(": ¥").append(avg).append(" (").append(count).append("人)\n");
            }
            
            summary.append("\n【部门统计】\n");
            for (Map.Entry<String, BigDecimal> entry : departmentSalaries.entrySet()) {
                String department = entry.getKey();
                BigDecimal total = entry.getValue();
                int count = departmentCounts.get(department);
                BigDecimal avg = total.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
                summary.append(department).append(": ¥").append(avg).append(" (").append(count).append("人)\n");
            }
            
            summary.append("\n===========================================");
            
            txtSummary.setText(summary.toString());
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "统计失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 导出统计报表
     */
    private void exportReport() {
        String selectedMonth = (String) cmbMonth.getSelectedItem();
        if (selectedMonth == null) {
            JOptionPane.showMessageDialog(this, "请先选择月份进行统计！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出统计报表");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel文件(*.xls)", "xls"));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xls")) {
                    filePath += ".xls";
                }
                
                // 导出工资记录
                List<SalaryRecord> records = salaryRecordDAO.findByMonth(selectedMonth);
                String[] headers = {"员工编号", "员工姓名", "部门", "岗位", "基本工资", "奖金", "扣款", "实发工资", "月份"};
                java.util.List<String[]> data = new java.util.ArrayList<>();
                
                for (SalaryRecord record : records) {
                    String[] row = new String[9];
                    row[0] = record.getEmployeeCode() != null ? record.getEmployeeCode() : "";
                    row[1] = record.getEmployeeName() != null ? record.getEmployeeName() : "";
                    row[2] = record.getDepartment() != null ? record.getDepartment() : "";
                    row[3] = record.getPosition() != null ? record.getPosition() : "";
                    row[4] = record.getBaseSalary() != null ? record.getBaseSalary().toString() : "0";
                    row[5] = record.getBonus() != null ? record.getBonus().toString() : "0";
                    row[6] = record.getDeductions() != null ? record.getDeductions().toString() : "0";
                    row[7] = record.getNetSalary() != null ? record.getNetSalary().toString() : "0";
                    row[8] = record.getSalaryMonth() != null ? record.getSalaryMonth() : "";
                    data.add(row);
                }
                
                ExcelUtil.exportToExcel(data, headers, filePath);
                JOptionPane.showMessageDialog(this, "导出成功！\n文件保存至：" + filePath, "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SalaryException e) {
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
