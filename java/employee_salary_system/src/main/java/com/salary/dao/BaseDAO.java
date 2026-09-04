package com.salary.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO基类，提供通用的增删改查方法
 * @param <T> 实体类型
 */
public abstract class BaseDAO<T> {

    /**
     * 获取当前实体类对应的数据库表名
     * 子类必须实现此方法
     * @return 表名
     */
    protected abstract String getTableName();

    /**
     * 将ResultSet结果集映射为实体对象
     * @param rs 结果集
     * @return 实体对象
     * @throws SQLException SQL异常
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    /**
     * 执行更新操作（增、删、改）
     * @param sql SQL语句
     * @param params 参数列表
     * @return 受影响的行数
     */
    public int executeUpdate(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("执行更新操作失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库更新异常", e);
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    /**
     * 执行查询操作
     * @param sql SQL语句
     * @param params 参数列表
     * @return 查询结果列表
     */
    public List<T> executeQuery(String sql, Object... params) {
        List<T> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("执行查询操作失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库查询异常", e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return results;
    }

    /**
     * 执行查询操作，返回单个对象
     * @param sql SQL语句
     * @param params 参数列表
     * @return 查询结果对象，如果不存在返回null
     */
    public T executeQuerySingle(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            setParameters(pstmt, params);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
        } catch (SQLException e) {
            System.err.println("执行查询操作失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库查询异常", e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * 设置PreparedStatement参数
     * @param pstmt PreparedStatement对象
     * @param params 参数数组
     * @throws SQLException SQL异常
     */
    protected void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }

    /**
     * 关闭数据库资源
     * @param rs ResultSet对象
     * @param pstmt PreparedStatement对象
     * @param conn Connection对象
     */
    protected void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭ResultSet失败: " + e.getMessage());
        }
        try {
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭PreparedStatement失败: " + e.getMessage());
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭Connection失败: " + e.getMessage());
        }
    }
}
