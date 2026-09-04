package com.salary.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工资记录模型类
 * 
 * @author salary-system
 * @version 1.0
 */
public class SalaryRecord {
    
    /** 记录ID */
    private Long id;
    
    /** 员工ID */
    private Long employeeId;
    
    /** 员工工号 */
    private String employeeCode;
    
    /** 员工姓名 */
    private String employeeName;
    
    /** 岗位 */
    private String position;
    
    /** 部门 */
    private String department;
    
    /** 基本工资 */
    private BigDecimal baseSalary;
    
    /** 工作时间 */
    private BigDecimal workHours;
    
    /** 销售额 */
    private BigDecimal salesAmount;
    
    /** 计算工资 */
    private BigDecimal calculatedSalary;
    
    /** 奖金 */
    private BigDecimal bonus;
    
    /** 扣款 */
    private BigDecimal deductions;
    
    /** 实发工资 */
    private BigDecimal netSalary;
    
    /** 工资月份 */
    private String salaryMonth;
    
    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 无参构造函数
     */
    public SalaryRecord() {
    }

    /**
     * 带参构造函数
     * 
     * @param id 记录ID
     * @param employeeId 员工ID
     * @param employeeCode 员工工号
     * @param employeeName 员工姓名
     * @param position 岗位
     * @param department 部门
     * @param baseSalary 基本工资
     * @param workHours 工作时间
     * @param salesAmount 销售额
     * @param calculatedSalary 计算工资
     * @param bonus 奖金
     * @param deductions 扣款
     * @param netSalary 实发工资
     * @param salaryMonth 工资月份
     * @param createTime 创建时间
     */
    public SalaryRecord(Long id, Long employeeId, String employeeCode, String employeeName,
                        String position, String department, BigDecimal baseSalary,
                        BigDecimal workHours, BigDecimal salesAmount, BigDecimal calculatedSalary,
                        BigDecimal bonus, BigDecimal deductions, BigDecimal netSalary,
                        String salaryMonth, LocalDateTime createTime) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.position = position;
        this.department = department;
        this.baseSalary = baseSalary;
        this.workHours = workHours;
        this.salesAmount = salesAmount;
        this.calculatedSalary = calculatedSalary;
        this.bonus = bonus;
        this.deductions = deductions;
        this.netSalary = netSalary;
        this.salaryMonth = salaryMonth;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getWorkHours() {
        return workHours;
    }

    public void setWorkHours(BigDecimal workHours) {
        this.workHours = workHours;
    }

    public BigDecimal getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount) {
        this.salesAmount = salesAmount;
    }

    public BigDecimal getCalculatedSalary() {
        return calculatedSalary;
    }

    public void setCalculatedSalary(BigDecimal calculatedSalary) {
        this.calculatedSalary = calculatedSalary;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public String getSalaryMonth() {
        return salaryMonth;
    }

    public void setSalaryMonth(String salaryMonth) {
        this.salaryMonth = salaryMonth;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SalaryRecord{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", employeeCode='" + employeeCode + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                ", baseSalary=" + baseSalary +
                ", workHours=" + workHours +
                ", salesAmount=" + salesAmount +
                ", calculatedSalary=" + calculatedSalary +
                ", bonus=" + bonus +
                ", deductions=" + deductions +
                ", netSalary=" + netSalary +
                ", salaryMonth='" + salaryMonth + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
