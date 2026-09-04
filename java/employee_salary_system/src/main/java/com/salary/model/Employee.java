package com.salary.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工模型类
 * 
 * @author salary-system
 * @version 1.0
 */
public class Employee {
    
    /** 员工ID */
    private Long id;
    
    /** 员工工号 */
    private String employeeCode;
    
    /** 员工姓名 */
    private String name;
    
    /** 性别 */
    private String gender;
    
    /** 年龄 */
    private Integer age;
    
    /** 电话 */
    private String phone;
    
    /** 邮箱 */
    private String email;
    
    /** 部门 */
    private String department;
    
    /** 岗位 */
    private String position;
    
    /** 基本工资 */
    private BigDecimal baseSalary;
    
    /** 奖金 */
    private BigDecimal bonus;
    
    /** 扣款 */
    private BigDecimal deductions;
    
    /** 入职日期 */
    private LocalDate hireDate;
    
    /** 状态 */
    private String status;

    /**
     * 无参构造函数
     */
    public Employee() {
    }

    /**
     * 带参构造函数
     * 
     * @param id 员工ID
     * @param employeeCode 员工工号
     * @param name 员工姓名
     * @param gender 性别
     * @param age 年龄
     * @param phone 电话
     * @param email 邮箱
     * @param department 部门
     * @param position 岗位
     * @param baseSalary 基本工资
     * @param bonus 奖金
     * @param deductions 扣款
     * @param hireDate 入职日期
     * @param status 状态
     */
    public Employee(Long id, String employeeCode, String name, String gender, Integer age,
                    String phone, String email, String department, String position,
                    BigDecimal baseSalary, BigDecimal bonus, BigDecimal deductions,
                    LocalDate hireDate, String status) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.department = department;
        this.position = position;
        this.baseSalary = baseSalary;
        this.bonus = bonus;
        this.deductions = deductions;
        this.hireDate = hireDate;
        this.status = status;
    }

    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
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

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeCode='" + employeeCode + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", baseSalary=" + baseSalary +
                ", bonus=" + bonus +
                ", deductions=" + deductions +
                ", hireDate=" + hireDate +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * 根据岗位计算薪资
     * <ul>
     *   <li>经理：固定月薪8000元</li>
     *   <li>技术员：工作时间×100元/小时</li>
     *   <li>销售员：销售额×4%提成</li>
     *   <li>销售经理：5000元底薪＋所辖部门销售额总额×0.5%</li>
     * </ul>
     * 
     * @param workHours 工作时间（技术员用）
     * @param salesAmount 销售额（销售员和销售经理用）
     * @return 计算后的薪资
     */
    public BigDecimal calculateSalary(BigDecimal workHours, BigDecimal salesAmount) {
        if (position == null) {
            return BigDecimal.ZERO;
        }
        
        switch (position) {
            case "经理":
                return new BigDecimal("8000");
            case "技术员":
                return workHours.multiply(new BigDecimal("100"));
            case "销售员":
                return salesAmount.multiply(new BigDecimal("0.04"));
            case "销售经理":
                return new BigDecimal("5000").add(salesAmount.multiply(new BigDecimal("0.005")));
            default:
                return baseSalary != null ? baseSalary : BigDecimal.ZERO;
        }
    }
}
