package com.salary.dao;

import com.salary.entity.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 员工数据访问类
 * 提供员工数据的增删改查操作
 */
public class EmployeeDAO extends BaseDAO<Employee> {

    @Override
    protected String getTableName() {
        return "employee";
    }

    @Override
    protected Employee mapResultSetToEntity(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setId(rs.getInt("id"));
        emp.setCode(rs.getString("code"));
        emp.setName(rs.getString("name"));
        emp.setGender(rs.getString("gender"));
        emp.setAge(rs.getInt("age"));
        emp.setDepartment(rs.getString("department"));
        emp.setPosition(rs.getString("position"));
        emp.setPhone(rs.getString("phone"));
        emp.setEmail(rs.getString("email"));
        emp.setHireDate(rs.getDate("hire_date"));
        emp.setStatus(rs.getInt("status"));
        emp.setCreateTime(rs.getTimestamp("create_time"));
        emp.setUpdateTime(rs.getTimestamp("update_time"));
        return emp;
    }

    /**
     * 新增员工
     * @param emp 员工实体
     * @return 受影响的行数
     */
    public int insert(Employee emp) {
        String sql = "INSERT INTO employee (code, name, gender, age, department, position, phone, email, hire_date, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql,
            emp.getCode(),
            emp.getName(),
            emp.getGender(),
            emp.getAge(),
            emp.getDepartment(),
            emp.getPosition(),
            emp.getPhone(),
            emp.getEmail(),
            emp.getHireDate(),
            emp.getStatus()
        );
    }

    /**
     * 更新员工信息
     * @param emp 员工实体
     * @return 受影响的行数
     */
    public int update(Employee emp) {
        String sql = "UPDATE employee SET name = ?, gender = ?, age = ?, department = ?, position = ?, " +
                     "phone = ?, email = ?, status = ?, update_time = CURRENT_TIMESTAMP WHERE id = ?";
        return executeUpdate(sql,
            emp.getName(),
            emp.getGender(),
            emp.getAge(),
            emp.getDepartment(),
            emp.getPosition(),
            emp.getPhone(),
            emp.getEmail(),
            emp.getStatus(),
            emp.getId()
        );
    }

    /**
     * 删除员工
     * @param id 员工ID
     * @return 受影响的行数
     */
    public int delete(int id) {
        String sql = "DELETE FROM employee WHERE id = ?";
        return executeUpdate(sql, id);
    }

    /**
     * 按ID查询员工
     * @param id 员工ID
     * @return 员工实体对象
     */
    public Employee findById(int id) {
        String sql = "SELECT * FROM employee WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    /**
     * 按员工编号查询
     * @param code 员工编号
     * @return 员工实体对象
     */
    public Employee findByCode(String code) {
        String sql = "SELECT * FROM employee WHERE code = ?";
        return executeQuerySingle(sql, code);
    }

    /**
     * 查询所有员工
     * @return 员工列表
     */
    public List<Employee> findAll() {
        String sql = "SELECT * FROM employee ORDER BY id";
        return executeQuery(sql);
    }

    /**
     * 多条件组合查询
     * @param name 姓名（模糊匹配）
     * @param department 部门
     * @param position 职位
     * @return 符合条件的员工列表
     */
    public List<Employee> findByConditions(String name, String department, String position) {
        StringBuilder sql = new StringBuilder("SELECT * FROM employee WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name + "%");
        }
        if (department != null && !department.trim().isEmpty()) {
            sql.append(" AND department = ?");
            params.add(department);
        }
        if (position != null && !position.trim().isEmpty()) {
            sql.append(" AND position = ?");
            params.add(position);
        }
        sql.append(" ORDER BY id");

        return executeQuery(sql.toString(), params.toArray());
    }

    /**
     * 统计员工数量
     * @return 员工总数
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM employee";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("统计员工数量失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库查询异常", e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return 0;
    }
}
