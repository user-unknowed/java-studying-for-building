package com.salary.service;

import com.salary.model.Employee;
import com.salary.dao.EmployeeDAO;
import com.salary.exception.SalaryException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 员工业务服务类
 * 提供员工的增删改查和验证功能
 */
public class EmployeeService {

    private EmployeeDAO employeeDAO;

    // 邮箱格式验证
    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    
    // 电话号码格式验证（简单验证：11位数字）
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 构造函数，初始化DAO对象
     */
    public EmployeeService() {
        this.employeeDAO = new EmployeeDAO();
    }

    /**
     * 添加新员工
     * 
     * @param emp 员工对象
     * @return 新增员工的ID
     * @throws SalaryException 如果添加失败
     */
    public Long addEmployee(Employee emp) {
        // 验证员工数据
        validateEmployee(emp);

        // 检查员工编号是否已存在
        if (emp.getEmployeeCode() != null && !emp.getEmployeeCode().trim().isEmpty()) {
            Employee existing = employeeDAO.findByCode(emp.getEmployeeCode());
            if (existing != null) {
                throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, 
                        "员工编号已存在：" + emp.getEmployeeCode());
            }
        }

        // 设置默认值
        if (emp.getHireDate() == null) {
            emp.setHireDate(LocalDate.now());
        }

        if (emp.getStatus() == null || emp.getStatus().trim().isEmpty()) {
            emp.setStatus("在职");
        }

        if (emp.getBaseSalary() == null) {
            emp.setBaseSalary(BigDecimal.ZERO);
        }

        if (emp.getBonus() == null) {
            emp.setBonus(BigDecimal.ZERO);
        }

        if (emp.getDeductions() == null) {
            emp.setDeductions(BigDecimal.ZERO);
        }

        int result = employeeDAO.insert(emp);
        if (result <= 0) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "员工信息保存失败");
        }

        return emp.getId();
    }

    /**
     * 更新员工信息
     * 
     * @param emp 员工对象
     * @throws SalaryException 如果更新失败
     */
    public void updateEmployee(Employee emp) {
        if (emp == null || emp.getId() == null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工信息不完整");
        }

        // 检查员工是否存在
        Employee existing = employeeDAO.findById(emp.getId().intValue());
        if (existing == null) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, 
                    "未找到对应员工，ID：" + emp.getId());
        }

        // 验证员工数据
        validateEmployee(emp);

        int result = employeeDAO.update(emp);
        if (result <= 0) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "员工信息更新失败");
        }
    }

    /**
     * 删除员工（逻辑删除，设置状态为离职）
     * 
     * @param id 员工ID
     * @throws SalaryException 如果删除失败
     */
    public void deleteEmployee(int id) {
        if (id <= 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工ID无效");
        }

        Employee emp = employeeDAO.findById(id);
        if (emp == null) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, 
                    "未找到对应员工，ID：" + id);
        }

        // 逻辑删除：设置状态为离职
        emp.setStatus("离职");
        int result = employeeDAO.update(emp);
        if (result <= 0) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "删除员工失败");
        }
    }

    /**
     * 多条件搜索员工
     * 支持按姓名（模糊匹配）、部门、岗位进行组合查询
     * 
     * @param name 姓名（可选，支持模糊匹配）
     * @param department 部门（可选，精确匹配）
     * @param position 岗位（可选，精确匹配）
     * @return 符合条件的员工列表
     * @throws SalaryException 如果查询失败
     */
    public List<Employee> searchEmployees(String name, String department, String position) {
        try {
            return employeeDAO.findByConditions(name, department, position);
        } catch (Exception e) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "查询员工失败", e);
        }
    }

    /**
     * 获取所有员工列表
     * 
     * @return 所有员工列表
     * @throws SalaryException 如果查询失败
     */
    public List<Employee> getAllEmployees() {
        try {
            return employeeDAO.findAll();
        } catch (Exception e) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "获取员工列表失败", e);
        }
    }

    /**
     * 验证员工数据合法性
     * 对员工各项数据进行业务规则验证
     * 
     * @param emp 员工对象
     * @throws SalaryException 如果数据不合法
     */
    public void validateEmployee(Employee emp) {
        if (emp == null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工信息不能为空");
        }

        // 验证姓名
        if (emp.getName() == null || emp.getName().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工姓名不能为空");
        }

        if (emp.getName().length() < 2 || emp.getName().length() > 50) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "员工姓名长度必须在2-50之间");
        }

        // 验证性别
        if (emp.getGender() != null && !emp.getGender().trim().isEmpty()) {
            if (!emp.getGender().equals("男") && !emp.getGender().equals("女")) {
                throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "性别必须是'男'或'女'");
            }
        }

        // 验证年龄
        if (emp.getAge() != null) {
            if (emp.getAge() < 18 || emp.getAge() > 65) {
                throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "年龄必须在18-65之间");
            }
        }

        // 验证电话
        if (emp.getPhone() != null && !emp.getPhone().trim().isEmpty()) {
            if (!PHONE_PATTERN.matcher(emp.getPhone()).matches()) {
                throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, 
                        "电话号码格式不正确，应为11位手机号码");
            }
        }

        // 验证邮箱
        if (emp.getEmail() != null && !emp.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(emp.getEmail()).matches()) {
                throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "邮箱格式不正确");
            }
        }

        // 验证部门
        if (emp.getDepartment() == null || emp.getDepartment().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "部门不能为空");
        }

        // 验证岗位
        if (emp.getPosition() == null || emp.getPosition().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "岗位不能为空");
        }

        // 验证基本工资
        if (emp.getBaseSalary() != null && emp.getBaseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "基本工资不能为负数");
        }

        // 验证奖金
        if (emp.getBonus() != null && emp.getBonus().compareTo(BigDecimal.ZERO) < 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "奖金不能为负数");
        }

        // 验证扣款
        if (emp.getDeductions() != null && emp.getDeductions().compareTo(BigDecimal.ZERO) < 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "扣款不能为负数");
        }

        // 验证入职日期
        if (emp.getHireDate() != null && emp.getHireDate().isAfter(LocalDate.now())) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "入职日期不能晚于当前日期");
        }

        // 验证状态
        if (emp.getStatus() != null && !emp.getStatus().trim().isEmpty()) {
            if (!emp.getStatus().equals("在职") && !emp.getStatus().equals("离职")) {
                throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, 
                        "状态只能是'在职'或'离职'");
            }
        }
    }

    /**
     * 根据ID获取员工信息
     * 
     * @param id 员工ID
     * @return 员工对象
     * @throws SalaryException 如果员工不存在
     */
    public Employee getEmployeeById(int id) {
        Employee emp = employeeDAO.findById(id);
        if (emp == null) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, 
                    "未找到对应员工，ID：" + id);
        }
        return emp;
    }
}
