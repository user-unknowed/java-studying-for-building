package com.salary.service;

import com.salary.model.Employee;
import com.salary.model.SalaryRecord;
import com.salary.dao.EmployeeDAO;
import com.salary.dao.SalaryRecordDAO;
import com.salary.exception.SalaryException;
import com.salary.util.ExcelUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工资业务服务类
 * 提供工资计算、生成、统计和导出功能
 */
public class SalaryService {

    private EmployeeDAO employeeDAO;
    private SalaryRecordDAO salaryRecordDAO;

    /**
     * 构造函数，初始化DAO对象
     */
    public SalaryService() {
        this.employeeDAO = new EmployeeDAO();
        this.salaryRecordDAO = new SalaryRecordDAO();
    }

    /**
     * 根据岗位计算工资
     * <ul>
     *   <li>经理：固定月薪8000元</li>
     *   <li>技术员：工作时间×100元/小时</li>
     *   <li>销售员：销售额×4%提成</li>
     *   <li>销售经理：5000元底薪＋所辖部门销售额总额×0.5%</li>
     * </ul>
     * 
     * @param emp 员工对象
     * @param workHours 工作时间（技术员用）
     * @param salesAmount 销售额（销售员和销售经理用）
     * @return 计算后的工资金额
     * @throws SalaryException 如果员工信息不完整或计算失败
     */
    public BigDecimal calculateSalary(Employee emp, double workHours, double salesAmount) {
        if (emp == null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工信息不能为空");
        }

        if (emp.getPosition() == null || emp.getPosition().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工岗位信息不完整");
        }

        BigDecimal workHoursBd = BigDecimal.valueOf(workHours);
        BigDecimal salesAmountBd = BigDecimal.valueOf(salesAmount);

        return emp.calculateSalary(workHoursBd, salesAmountBd);
    }

    /**
     * 生成月度工资记录
     * 根据员工信息和当月工作数据生成工资记录
     * 
     * @param employeeId 员工ID
     * @param month 工资月份（格式：YYYY-MM）
     * @return 生成的工资记录，如果员工不存在返回null
     * @throws SalaryException 如果生成失败
     */
    public SalaryRecord generateMonthlySalary(int employeeId, String month) {
        if (employeeId <= 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工ID无效");
        }

        if (month == null || !month.matches("\\d{4}-\\d{2}")) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "月份格式错误，请使用YYYY-MM格式");
        }

        Employee emp = employeeDAO.findById(employeeId);
        if (emp == null) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, "未找到对应员工，ID：" + employeeId);
        }

        SalaryRecord record = new SalaryRecord();
        record.setEmployeeId(emp.getId());
        record.setEmployeeCode(emp.getEmployeeCode());
        record.setEmployeeName(emp.getName());
        record.setPosition(emp.getPosition());
        record.setDepartment(emp.getDepartment());
        record.setBaseSalary(emp.getBaseSalary());
        record.setSalaryMonth(month);

        // 默认工作时间160小时，销售额为0，实际应用中应从考勤和销售系统获取
        BigDecimal workHours = new BigDecimal("160");
        BigDecimal salesAmount = BigDecimal.ZERO;

        record.setWorkHours(workHours);
        record.setSalesAmount(salesAmount);

        // 计算工资
        BigDecimal calculatedSalary = emp.calculateSalary(workHours, salesAmount);
        record.setCalculatedSalary(calculatedSalary);

        // 奖金和扣款
        record.setBonus(emp.getBonus() != null ? emp.getBonus() : BigDecimal.ZERO);
        record.setDeductions(emp.getDeductions() != null ? emp.getDeductions() : BigDecimal.ZERO);

        // 实发工资 = 计算工资 + 奖金 - 扣款
        BigDecimal netSalary = calculatedSalary
                .add(record.getBonus())
                .subtract(record.getDeductions());
        record.setNetSalary(netSalary);

        record.setCreateTime(LocalDateTime.now());

        // 保存记录
        int result = salaryRecordDAO.insert(record);
        if (result <= 0) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "工资记录保存失败");
        }

        return record;
    }

    /**
     * 生成所有员工指定月份的工资记录
     * 遍历所有在职员工，生成月度工资
     * 
     * @param month 工资月份（格式：YYYY-MM）
     * @return 成功生成的工资记录数量
     * @throws SalaryException 如果生成过程中发生错误
     */
    public int generateAllMonthlySalaries(String month) {
        if (month == null || !month.matches("\\d{4}-\\d{2}")) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "月份格式错误，请使用YYYY-MM格式");
        }

        List<Employee> employees = employeeDAO.findAll();
        if (employees == null || employees.isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, "未找到在职员工");
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (Employee emp : employees) {
            // 只处理在职员工
            if (emp.getStatus() != null && emp.getStatus().equals("在职")) {
                try {
                    generateMonthlySalary(emp.getId().intValue(), month);
                    successCount++;
                } catch (SalaryException e) {
                    errors.add("员工" + emp.getName() + "(" + emp.getId() + "): " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            System.err.println("部分员工工资生成失败：" + String.join("; ", errors));
        }

        return successCount;
    }

    /**
     * 获取工资统计数据
     * 统计指定月份的工资总额、平均工资、奖金总额、扣款总额等
     * 
     * @param month 工资月份（格式：YYYY-MM），如果为null或空则统计所有月份
     * @return 统计数据Map，包含：
     *         totalCount - 记录总数
     *         totalSalary - 总工资
     *         avgSalary - 平均工资
     *         totalBonus - 总奖金
     *         totalDeduction - 总扣款
     *         departmentStats - 部门统计
     * @throws SalaryException 如果获取统计失败
     */
    public Map<String, Object> getStatistics(String month) {
        Map<String, Object> params = new HashMap<>();
        if (month != null && !month.trim().isEmpty()) {
            params.put("month", month);
        }

        try {
            Map<String, Object> stats = salaryRecordDAO.getStatistics();
            
            // 如果指定了月份，进一步过滤统计
            if (month != null && !month.trim().isEmpty()) {
                List<SalaryRecord> records = salaryRecordDAO.findByConditions(params);
                if (records != null && !records.isEmpty()) {
                    double totalSalary = 0;
                    double totalBonus = 0;
                    double totalDeduction = 0;
                    for (SalaryRecord record : records) {
                        totalSalary += record.getNetSalary().doubleValue();
                        totalBonus += record.getBonus().doubleValue();
                        totalDeduction += record.getDeductions().doubleValue();
                    }
                    stats.put("totalCount", records.size());
                    stats.put("totalSalary", totalSalary);
                    stats.put("avgSalary", totalSalary / records.size());
                    stats.put("totalBonus", totalBonus);
                    stats.put("totalDeduction", totalDeduction);
                }
            }
            
            return stats;
        } catch (Exception e) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "获取工资统计数据失败", e);
        }
    }

    /**
     * 导出工资数据到Excel文件
     * 
     * @param month 工资月份（格式：YYYY-MM）
     * @param filePath 导出文件路径
     * @throws SalaryException 如果导出失败
     */
    public void exportSalaryToExcel(String month, String filePath) {
        if (month == null || !month.matches("\\d{4}-\\d{2}")) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "月份格式错误，请使用YYYY-MM格式");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "导出文件路径不能为空");
        }

        if (!ExcelUtil.isValidExcelFile(filePath)) {
            throw new SalaryException(SalaryException.ERR_FILE_IO, "文件格式不正确，请使用.xls或.xlsx格式");
        }

        List<SalaryRecord> records = salaryRecordDAO.findByMonth(month);
        if (records == null || records.isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, "未找到" + month + "月的工资数据");
        }

        // 准备表头
        String[] headers = {"员工编号", "姓名", "岗位", "部门", "基本工资", "工作时间", 
                           "销售额", "计算工资", "奖金", "扣款", "实发工资", "月份"};

        // 准备数据
        List<String[]> data = new ArrayList<>();
        for (SalaryRecord record : records) {
            String[] row = {
                record.getEmployeeCode(),
                record.getEmployeeName(),
                record.getPosition(),
                record.getDepartment(),
                record.getBaseSalary() != null ? record.getBaseSalary().toString() : "0",
                record.getWorkHours() != null ? record.getWorkHours().toString() : "0",
                record.getSalesAmount() != null ? record.getSalesAmount().toString() : "0",
                record.getCalculatedSalary() != null ? record.getCalculatedSalary().toString() : "0",
                record.getBonus() != null ? record.getBonus().toString() : "0",
                record.getDeductions() != null ? record.getDeductions().toString() : "0",
                record.getNetSalary() != null ? record.getNetSalary().toString() : "0",
                record.getSalaryMonth()
            };
            data.add(row);
        }

        // 导出到Excel
        ExcelUtil.exportToExcel(data, headers, filePath);
    }
}
