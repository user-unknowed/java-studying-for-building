package com.salary.dao;

import com.salary.entity.SalaryRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工资记录数据访问类
 * 提供工资记录的增删改查操作
 */
public class SalaryRecordDAO extends BaseDAO<SalaryRecord> {

    @Override
    protected String getTableName() {
        return "salary_record";
    }

    @Override
    protected SalaryRecord mapResultSetToEntity(ResultSet rs) throws SQLException {
        SalaryRecord record = new SalaryRecord();
        record.setId(rs.getInt("id"));
        record.setEmployeeId(rs.getInt("employee_id"));
        record.setEmployeeName(rs.getString("employee_name"));
        record.setMonth(rs.getString("month"));
        record.setBaseSalary(rs.getDouble("base_salary"));
        record.setBonus(rs.getDouble("bonus"));
        record.setDeduction(rs.getDouble("deduction"));
        record.setActualSalary(rs.getDouble("actual_salary"));
        record.setCreateTime(rs.getTimestamp("create_time"));
        return record;
    }

    /**
     * 新增工资记录
     * @param record 工资记录实体
     * @return 受影响的行数
     */
    public int insert(SalaryRecord record) {
        String sql = "INSERT INTO salary_record (employee_id, employee_name, month, base_salary, bonus, deduction, actual_salary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql,
            record.getEmployeeId(),
            record.getEmployeeName(),
            record.getMonth(),
            record.getBaseSalary(),
            record.getBonus(),
            record.getDeduction(),
            record.getActualSalary()
        );
    }

    /**
     * 查询某员工的工资记录
     * @param employeeId 员工ID
     * @return 工资记录列表
     */
    public List<SalaryRecord> findByEmployeeId(int employeeId) {
        String sql = "SELECT * FROM salary_record WHERE employee_id = ? ORDER BY month DESC";
        return executeQuery(sql, employeeId);
    }

    /**
     * 按月份查询工资记录
     * @param month 月份（格式：YYYY-MM）
     * @return 工资记录列表
     */
    public List<SalaryRecord> findByMonth(String month) {
        String sql = "SELECT * FROM salary_record WHERE month = ? ORDER BY employee_id";
        return executeQuery(sql, month);
    }

    /**
     * 条件查询工资记录
     * @param params 查询条件参数
     *               employeeId - 员工ID
     *               month - 月份
     *               department - 部门
     *               startDate - 开始日期
     *               endDate - 结束日期
     * @return 符合条件的工资记录列表
     */
    public List<SalaryRecord> findByConditions(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT * FROM salary_record WHERE 1=1");
        List<Object> queryParams = new ArrayList<>();

        if (params != null) {
            if (params.containsKey("employeeId") && params.get("employeeId") != null) {
                sql.append(" AND employee_id = ?");
                queryParams.add(params.get("employeeId"));
            }
            if (params.containsKey("month") && params.get("month") != null) {
                sql.append(" AND month = ?");
                queryParams.add(params.get("month"));
            }
            if (params.containsKey("department") && params.get("department") != null) {
                sql.append(" AND department = ?");
                queryParams.add(params.get("department"));
            }
            if (params.containsKey("startDate") && params.get("startDate") != null) {
                sql.append(" AND month >= ?");
                queryParams.add(params.get("startDate"));
            }
            if (params.containsKey("endDate") && params.get("endDate") != null) {
                sql.append(" AND month <= ?");
                queryParams.add(params.get("endDate"));
            }
        }
        sql.append(" ORDER BY month DESC, employee_id");

        return executeQuery(sql.toString(), queryParams.toArray());
    }

    /**
     * 获取统计数据
     * @return 统计数据Map，包含：
     *         totalCount - 记录总数
     *         totalSalary - 总工资
     *         avgSalary - 平均工资
     *         totalBonus - 总奖金
     *         totalDeduction - 总扣款
     *         departmentStats - 部门统计（Map<部门, 平均工资>）
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as total_count, SUM(actual_salary) as total_salary, " +
                     "AVG(actual_salary) as avg_salary, SUM(bonus) as total_bonus, " +
                     "SUM(deduction) as total_deduction FROM salary_record";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalCount", rs.getInt("total_count"));
                stats.put("totalSalary", rs.getDouble("total_salary"));
                stats.put("avgSalary", rs.getDouble("avg_salary"));
                stats.put("totalBonus", rs.getDouble("total_bonus"));
                stats.put("totalDeduction", rs.getDouble("total_deduction"));
            }
        } catch (SQLException e) {
            System.err.println("获取统计数据失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库查询异常", e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        // 获取各部门平均工资
        String deptSql = "SELECT department, AVG(actual_salary) as avg_salary " +
                        "FROM salary_record GROUP BY department";
        Map<String, Double> departmentStats = new HashMap<>();
        try {
            pstmt = conn.prepareStatement(deptSql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                departmentStats.put(rs.getString("department"), rs.getDouble("avg_salary"));
            }
            stats.put("departmentStats", departmentStats);
        } catch (SQLException e) {
            System.err.println("获取部门统计失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return stats;
    }

    /**
     * 删除某员工所有工资记录
     * @param employeeId 员工ID
     * @return 受影响的行数
     */
    public int deleteByEmployeeId(int employeeId) {
        String sql = "DELETE FROM salary_record WHERE employee_id = ?";
        return executeUpdate(sql, employeeId);
    }
}
