package com.salary.ui;

import com.salary.dao.EmployeeDAO;
import com.salary.exception.SalaryException;
import com.salary.model.Employee;
import com.salary.util.ExcelUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Vector;

/**
 * 员工信息管理面板
 * 提供员工信息的增删改查和导出功能
 */
public class EmployeePanel extends JPanel {
    
    // 部门选项
    private static final String[] DEPARTMENTS = {"技术部", "销售部", "财务部", "人力资源部", "行政部", "市场部"};
    
    // 岗位选项
    private static final String[] POSITIONS = {"经理", "技术员", "销售员", "销售经理", "主管", "专员"};
    
    // 性别选项
    private static final String[] GENDERS = {"男", "女"};
    
    // 员工DAO
    private EmployeeDAO employeeDAO;
    
    // 表格模型
    private DefaultTableModel tableModel;
    
    // 员工信息录入区域
    private JTextField txtEmployeeCode;
    private JTextField txtName;
    private JComboBox<String> cmbGender;
    private JTextField txtAge;
    private JComboBox<String> cmbDepartment;
    private JComboBox<String> cmbPosition;
    private JTextField txtHireDate;
    private JTextField txtPhone;
    
    // 按钮
    private JButton btnAdd;
    private JButton btnReset;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnExportExcel;
    
    // 表格
    private JTable table;
    
    // 日期格式
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 构造函数
     */
    public EmployeePanel() {
        employeeDAO = new EmployeeDAO();
        initComponents();
        loadEmployeeData();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 顶部：员工信息录入区域
        add(createInputPanel(), BorderLayout.NORTH);
        
        // 中部：员工列表表格
        add(createTablePanel(), BorderLayout.CENTER);
        
        // 底部：操作按钮
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * 创建员工信息录入区域
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("员工信息录入"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 第一行
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("员工编号:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtEmployeeCode = new JTextField(15);
        panel.add(txtEmployeeCode, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("姓名:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        txtName = new JTextField(15);
        panel.add(txtName, gbc);
        
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        panel.add(new JLabel("性别:"), gbc);
        
        gbc.gridx = 5;
        gbc.weightx = 0.5;
        cmbGender = new JComboBox<>(GENDERS);
        panel.add(cmbGender, gbc);
        
        // 第二行
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("年龄:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtAge = new JTextField(15);
        panel.add(txtAge, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("部门:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        cmbDepartment = new JComboBox<>(DEPARTMENTS);
        panel.add(cmbDepartment, gbc);
        
        gbc.gridx = 4;
        gbc.weightx = 0.0;
        panel.add(new JLabel("岗位:"), gbc);
        
        gbc.gridx = 5;
        gbc.weightx = 0.5;
        cmbPosition = new JComboBox<>(POSITIONS);
        panel.add(cmbPosition, gbc);
        
        // 第三行
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("入职日期:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtHireDate = new JTextField(15);
        txtHireDate.setToolTipText("格式：yyyy-MM-dd");
        panel.add(txtHireDate, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("联系电话:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        txtPhone = new JTextField(15);
        panel.add(txtPhone, gbc);
        
        // 第四行：按钮
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAdd = new JButton("添加员工");
        btnReset = new JButton("重置");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addEmployee();
            }
        });
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetInputFields();
            }
        });
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnReset);
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    /**
     * 创建员工列表表格
     */
    private JScrollPane createTablePanel() {
        String[] columnNames = {"编号", "员工编号", "姓名", "性别", "年龄", "部门", "岗位", "入职日期", "联系电话"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("员工列表"));
        
        // 添加表格选择监听器
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    loadSelectedEmployee(selectedRow);
                }
            }
        });
        
        return scrollPane;
    }
    
    /**
     * 创建操作按钮区域
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        btnUpdate = new JButton("修改");
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEmployee();
            }
        });
        
        btnDelete = new JButton("删除");
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteEmployee();
            }
        });
        
        btnExportExcel = new JButton("导出Excel");
        btnExportExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToExcel();
            }
        });
        
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnExportExcel);
        
        return panel;
    }
    
    /**
     * 添加员工
     */
    private void addEmployee() {
        try {
            // 验证输入
            if (!validateInput()) {
                return;
            }
            
            // 创建员工对象
            Employee employee = new Employee();
            employee.setEmployeeCode(txtEmployeeCode.getText().trim());
            employee.setName(txtName.getText().trim());
            employee.setGender((String) cmbGender.getSelectedItem());
            employee.setAge(Integer.parseInt(txtAge.getText().trim()));
            employee.setDepartment((String) cmbDepartment.getSelectedItem());
            employee.setPosition((String) cmbPosition.getSelectedItem());
            employee.setHireDate(LocalDate.parse(txtHireDate.getText().trim(), dateFormatter));
            employee.setPhone(txtPhone.getText().trim());
            employee.setStatus("在职");
            
            // 调用DAO添加
            EmployeeDAO dao = new EmployeeDAO();
            int result = dao.insert(employee);
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "添加员工成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                resetInputFields();
                loadEmployeeData();
            } else {
                JOptionPane.showMessageDialog(this, "添加员工失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "年龄必须是数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "日期格式不正确，请使用yyyy-MM-dd格式！", "输入错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "添加员工失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 修改员工
     */
    private void updateEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的员工！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (!validateInput()) {
                return;
            }
            
            Long employeeId = (Long) tableModel.getValueAt(selectedRow, 0);
            
            Employee employee = new Employee();
            employee.setId(employeeId);
            employee.setEmployeeCode(txtEmployeeCode.getText().trim());
            employee.setName(txtName.getText().trim());
            employee.setGender((String) cmbGender.getSelectedItem());
            employee.setAge(Integer.parseInt(txtAge.getText().trim()));
            employee.setDepartment((String) cmbDepartment.getSelectedItem());
            employee.setPosition((String) cmbPosition.getSelectedItem());
            employee.setHireDate(LocalDate.parse(txtHireDate.getText().trim(), dateFormatter));
            employee.setPhone(txtPhone.getText().trim());
            
            EmployeeDAO dao = new EmployeeDAO();
            int result = dao.update(employee);
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "修改员工成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                resetInputFields();
                loadEmployeeData();
            } else {
                JOptionPane.showMessageDialog(this, "修改员工失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "年龄必须是数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "日期格式不正确，请使用yyyy-MM-dd格式！", "输入错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "修改员工失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 删除员工
     */
    private void deleteEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的员工！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "确定要删除该员工吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            Long employeeId = (Long) tableModel.getValueAt(selectedRow, 0);
            
            EmployeeDAO dao = new EmployeeDAO();
            int result = dao.delete(employeeId.intValue());
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "删除员工成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                resetInputFields();
                loadEmployeeData();
            } else {
                JOptionPane.showMessageDialog(this, "删除员工失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "删除员工失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 导出到Excel
     */
    private void exportToExcel() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出员工数据");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel文件(*.xls)", "xls"));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".xls")) {
                    filePath += ".xls";
                }
                
                List<Employee> employees = employeeDAO.findAll();
                String[] headers = {"编号", "员工编号", "姓名", "性别", "年龄", "部门", "岗位", "入职日期", "联系电话"};
                java.util.List<String[]> data = new java.util.ArrayList<>();
                
                for (Employee emp : employees) {
                    String[] row = new String[9];
                    row[0] = emp.getId() != null ? emp.getId().toString() : "";
                    row[1] = emp.getEmployeeCode() != null ? emp.getEmployeeCode() : "";
                    row[2] = emp.getName() != null ? emp.getName() : "";
                    row[3] = emp.getGender() != null ? emp.getGender() : "";
                    row[4] = emp.getAge() != null ? emp.getAge().toString() : "";
                    row[5] = emp.getDepartment() != null ? emp.getDepartment() : "";
                    row[6] = emp.getPosition() != null ? emp.getPosition() : "";
                    row[7] = emp.getHireDate() != null ? emp.getHireDate().toString() : "";
                    row[8] = emp.getPhone() != null ? emp.getPhone() : "";
                    data.add(row);
                }
                
                ExcelUtil.exportToExcel(data, headers, filePath);
                JOptionPane.showMessageDialog(this, "导出成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SalaryException e) {
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 验证输入
     */
    private boolean validateInput() {
        if (txtEmployeeCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "员工编号不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
            txtEmployeeCode.requestFocus();
            return false;
        }
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "姓名不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return false;
        }
        if (txtAge.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "年龄不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
            txtAge.requestFocus();
            return false;
        }
        try {
            int age = Integer.parseInt(txtAge.getText().trim());
            if (age < 18 || age > 65) {
                JOptionPane.showMessageDialog(this, "年龄必须在18-65之间！", "输入错误", JOptionPane.ERROR_MESSAGE);
                txtAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "年龄必须是数字！", "输入错误", JOptionPane.ERROR_MESSAGE);
            txtAge.requestFocus();
            return false;
        }
        if (txtHireDate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "入职日期不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
            txtHireDate.requestFocus();
            return false;
        }
        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "联系电话不能为空！", "输入错误", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }
        return true;
    }
    
    /**
     * 重置输入字段
     */
    private void resetInputFields() {
        txtEmployeeCode.setText("");
        txtName.setText("");
        cmbGender.setSelectedIndex(0);
        txtAge.setText("");
        cmbDepartment.setSelectedIndex(0);
        cmbPosition.setSelectedIndex(0);
        txtHireDate.setText("");
        txtPhone.setText("");
        table.clearSelection();
    }
    
    /**
     * 加载选中的员工信息到输入区域
     */
    private void loadSelectedEmployee(int selectedRow) {
        txtEmployeeCode.setText(tableModel.getValueAt(selectedRow, 1).toString());
        txtName.setText(tableModel.getValueAt(selectedRow, 2).toString());
        
        String gender = tableModel.getValueAt(selectedRow, 3).toString();
        cmbGender.setSelectedItem(gender);
        
        txtAge.setText(tableModel.getValueAt(selectedRow, 4).toString());
        
        String department = tableModel.getValueAt(selectedRow, 5).toString();
        cmbDepartment.setSelectedItem(department);
        
        String position = tableModel.getValueAt(selectedRow, 6).toString();
        cmbPosition.setSelectedItem(position);
        
        txtHireDate.setText(tableModel.getValueAt(selectedRow, 7).toString());
        txtPhone.setText(tableModel.getValueAt(selectedRow, 8).toString());
    }
    
    /**
     * 加载员工数据到表格
     */
    private void loadEmployeeData() {
        try {
            tableModel.setRowCount(0);
            List<Employee> employees = employeeDAO.findAll();
            
            for (Employee emp : employees) {
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
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 刷新表格数据
     */
    public void refreshData() {
        loadEmployeeData();
    }
}
