package com.salary.ui;

import com.salary.dao.EmployeeDAO;
import com.salary.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 * 数据查询面板
 * 提供多条件组合查询员工信息功能
 */
public class QueryPanel extends JPanel {
    
    // 部门选项
    private static final String[] DEPARTMENTS = {"", "技术部", "销售部", "财务部", "人力资源部", "行政部", "市场部"};
    
    // 岗位选项
    private static final String[] POSITIONS = {"", "经理", "技术员", "销售员", "销售经理", "主管", "专员"};
    
    // DAO
    private EmployeeDAO employeeDAO;
    
    // 查询条件组件
    private JTextField txtName;
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbPosition;
    
    // 按钮
    private JButton btnQuery;
    private JButton btnReset;
    
    // 结果表格
    private DefaultTableModel tableModel;
    private JTable table;
    
    // 分页组件
    private JButton btnPrevious;
    private JButton btnNext;
    private JLabel lblPageInfo;
    private JLabel lblTotalInfo;
    
    // 分页参数
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalRecords = 0;
    private int totalPages = 0;
    
    // 当前查询结果缓存
    private List<Employee> cachedResults;
    
    /**
     * 构造函数
     */
    public QueryPanel() {
        employeeDAO = new EmployeeDAO();
        initComponents();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 顶部：查询条件区域
        add(createQueryPanel(), BorderLayout.NORTH);
        
        // 中部：查询结果表格
        add(createTablePanel(), BorderLayout.CENTER);
        
        // 底部：分页和操作按钮
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * 创建查询条件面板
     */
    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("查询条件"));
        
        // 第一行：查询条件
        JPanel conditionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        conditionPanel.add(new JLabel("员工姓名:"));
        txtName = new JTextField(15);
        txtName.setToolTipText("支持模糊查询");
        conditionPanel.add(txtName);
        
        conditionPanel.add(new JLabel("部门:"));
        cmbDepartment = new JComboBox<>(DEPARTMENTS);
        conditionPanel.add(cmbDepartment);
        
        conditionPanel.add(new JLabel("岗位:"));
        cmbPosition = new JComboBox<>(POSITIONS);
        conditionPanel.add(cmbPosition);
        
        // 第二行：按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnQuery = new JButton("查询");
        btnQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });
        
        btnReset = new JButton("重置");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetQuery();
            }
        });
        
        buttonPanel.add(btnQuery);
        buttonPanel.add(btnReset);
        
        panel.add(conditionPanel);
        panel.add(buttonPanel);
        
        return panel;
    }
    
    /**
     * 创建结果表格面板
     */
    private JScrollPane createTablePanel() {
        String[] columnNames = {"编号", "员工编号", "姓名", "性别", "年龄", "部门", "岗位", "入职日期", "联系电话", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("查询结果"));
        
        return scrollPane;
    }
    
    /**
     * 创建底部面板（分页和统计）
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        
        // 分页组件
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnPrevious = new JButton("上一页");
        btnPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousPage();
            }
        });
        btnPrevious.setEnabled(false);
        
        btnNext = new JButton("下一页");
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextPage();
            }
        });
        btnNext.setEnabled(false);
        
        lblPageInfo = new JLabel("第 1 页");
        lblTotalInfo = new JLabel("共 0 条记录");
        
        paginationPanel.add(btnPrevious);
        paginationPanel.add(btnNext);
        paginationPanel.add(Box.createHorizontalStrut(20));
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(Box.createHorizontalStrut(10));
        paginationPanel.add(lblTotalInfo);
        
        panel.add(paginationPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 执行查询
     */
    private void doQuery() {
        try {
            String name = txtName.getText().trim();
            String department = (String) cmbDepartment.getSelectedItem();
            String position = (String) cmbPosition.getSelectedItem();
            
            // 处理空选择
            if (department != null && department.isEmpty()) {
                department = null;
            }
            if (position != null && position.isEmpty()) {
                position = null;
            }
            
            // 执行查询
            cachedResults = employeeDAO.findByConditions(name, department, position);
            
            if (cachedResults.isEmpty()) {
                JOptionPane.showMessageDialog(this, "未找到符合条件的员工！", "查询结果", JOptionPane.INFORMATION_MESSAGE);
                tableModel.setRowCount(0);
                totalRecords = 0;
                totalPages = 0;
                currentPage = 1;
                updatePaginationInfo();
                return;
            }
            
            totalRecords = cachedResults.size();
            totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages == 0) {
                totalPages = 1;
            }
            currentPage = 1;
            
            // 加载第一页数据
            loadPageData();
            updatePaginationInfo();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "查询失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载当前页数据到表格
     */
    private void loadPageData() {
        tableModel.setRowCount(0);
        
        if (cachedResults == null || cachedResults.isEmpty()) {
            return;
        }
        
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecords);
        
        for (int i = startIndex; i < endIndex; i++) {
            Employee emp = cachedResults.get(i);
            Vector<Object> row = new Vector<>();
            row.add(emp.getId());
            row.add(emp.getEmployeeCode());
            row.add(emp.getName());
            row.add(emp.getGender());
            row.add(emp.getAge());
            row.add(emp.getDepartment());
            row.add(emp.getPosition());
            row.add(emp.getHireDate());
            row.add(emp.getPhone());
            row.add(emp.getStatus());
            tableModel.addRow(row);
        }
    }
    
    /**
     * 更新分页信息显示
     */
    private void updatePaginationInfo() {
        lblPageInfo.setText("第 " + currentPage + " / " + totalPages + " 页");
        lblTotalInfo.setText("共 " + totalRecords + " 条记录");
        
        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }
    
    /**
     * 上一页
     */
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPageData();
            updatePaginationInfo();
        }
    }
    
    /**
     * 下一页
     */
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadPageData();
            updatePaginationInfo();
        }
    }
    
    /**
     * 重置查询条件
     */
    private void resetQuery() {
        txtName.setText("");
        cmbDepartment.setSelectedIndex(0);
        cmbPosition.setSelectedIndex(0);
        tableModel.setRowCount(0);
        cachedResults = null;
        totalRecords = 0;
        totalPages = 0;
        currentPage = 1;
        updatePaginationInfo();
    }
    
    /**
     * 获取选中的员工
     */
    public Employee getSelectedEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0 && cachedResults != null) {
            int index = (currentPage - 1) * pageSize + selectedRow;
            if (index >= 0 && index < cachedResults.size()) {
                return cachedResults.get(index);
            }
        }
        return null;
    }
}
