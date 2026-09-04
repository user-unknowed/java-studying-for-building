package com.salary.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import com.salary.exception.SalaryException;

/**
 * 数据库工具类
 * 提供数据库连接管理和资源关闭功能
 * 采用单例模式，使用双重检查锁定实现线程安全
 */
public class DBUtil {
    
    private static volatile DBUtil instance;
    private Connection connection;
    
    // 连接池配置
    private static final int MAX_CONNECTIONS = 10;
    private Queue<Connection> connectionPool;
    private int poolSize = 0;
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private DBUtil() {
        connectionPool = new LinkedList<>();
        initializeDriver();
    }
    
    /**
     * 获取DBUtil单例实例（双重检查锁定）
     * @return DBUtil实例
     */
    public static DBUtil getInstance() {
        if (instance == null) {
            synchronized (DBUtil.class) {
                if (instance == null) {
                    instance = new DBUtil();
                }
            }
        }
        return instance;
    }
    
    /**
     * 初始化数据库驱动
     */
    private void initializeDriver() {
        try {
            String driverClassName = ConfigUtil.getInstance().getDatabaseDriver();
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw SalaryException.databaseConnection(e);
        }
    }
    
    /**
     * 从连接池获取数据库连接
     * @return 数据库连接对象
     * @throws SalaryException 如果获取连接失败
     */
    public Connection getConnection() throws SalaryException {
        Connection conn = null;
        
        // 尝试从连接池获取连接
        synchronized (connectionPool) {
            if (!connectionPool.isEmpty()) {
                conn = connectionPool.poll();
                poolSize--;
                
                // 检查连接是否有效
                try {
                    if (conn == null || !conn.isValid(1000)) {
                        if (conn != null) {
                            try {
                                conn.close();
                            } catch (SQLException e) {
                                // 忽略关闭异常
                            }
                        }
                        conn = createNewConnection();
                    }
                } catch (SQLException e) {
                    throw SalaryException.databaseConnection(e);
                }
                
                return conn;
            }
        }
        
        // 连接池为空，创建新连接
        conn = createNewConnection();
        return conn;
    }
    
    /**
     * 创建新的数据库连接
     * @return 新的数据库连接
     * @throws SalaryException 如果创建连接失败
     */
    private Connection createNewConnection() throws SalaryException {
        try {
            ConfigUtil config = ConfigUtil.getInstance();
            String url = config.getDatabaseUrl();
            String username = config.getDatabaseUsername();
            String password = config.getDatabasePassword();
            
            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } catch (SQLException e) {
            throw SalaryException.databaseConnection(e);
        }
    }
    
    /**
     * 将连接归还到连接池
     * @param conn 要归还的数据库连接
     */
    public void returnConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        
        synchronized (connectionPool) {
            if (poolSize < MAX_CONNECTIONS) {
                try {
                    if (conn.isValid(1000)) {
                        connectionPool.offer(conn);
                        poolSize++;
                    } else {
                        conn.close();
                    }
                } catch (SQLException e) {
                    try {
                        conn.close();
                    } catch (SQLException closeEx) {
                        // 忽略关闭异常
                    }
                }
            } else {
                // 连接池已满，直接关闭连接
                try {
                    conn.close();
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    /**
     * 关闭所有数据库资源
     * @param connections 要关闭的连接数组
     * @param statements 要关闭的语句数组
     * @param resultSets 要关闭的结果集数组
     */
    public void closeAll(Connection[] connections, 
                        PreparedStatement[] statements, 
                        ResultSet[] resultSets) {
        // 关闭ResultSet
        if (resultSets != null) {
            for (ResultSet rs : resultSets) {
                closeResultSet(rs);
            }
        }
        
        // 关闭PreparedStatement
        if (statements != null) {
            for (PreparedStatement ps : statements) {
                closeStatement(ps);
            }
        }
        
        // 关闭Connection，归还到连接池
        if (connections != null) {
            for (Connection conn : connections) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * 关闭ResultSet
     * @param resultSet 要关闭的ResultSet
     */
    private void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 关闭PreparedStatement
     * @param statement 要关闭的PreparedStatement
     */
    private void closeStatement(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 关闭数据库连接
     * @param conn 要关闭的连接
     */
    public void closeConnection(Connection conn) {
        returnConnection(conn);
    }
    
    /**
     * 关闭所有连接池中的连接
     */
    public void shutdown() {
        synchronized (connectionPool) {
            for (Connection conn : connectionPool) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
            connectionPool.clear();
            poolSize = 0;
        }
    }
    
    /**
     * 执行数据库查询
     * @param sql SQL查询语句
     * @param params 查询参数
     * @return ResultSet结果集
     * @throws SalaryException 如果查询执行失败
     */
    public ResultSet executeQuery(String sql, Object... params) throws SalaryException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            closeAll(new Connection[]{conn}, new PreparedStatement[]{ps}, new ResultSet[]{rs});
            throw SalaryException.databaseQuery(e);
        }
    }
    
    /**
     * 执行数据库更新操作
     * @param sql SQL更新语句
     * @param params 更新参数
     * @return 影响的行数
     * @throws SalaryException 如果更新执行失败
     */
    public int executeUpdate(String sql, Object... params) throws SalaryException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            
            int rows = ps.executeUpdate();
            return rows;
        } catch (SQLException e) {
            throw SalaryException.databaseQuery(e);
        } finally {
            closeAll(new Connection[]{conn}, new PreparedStatement[]{ps}, null);
        }
    }
    
    /**
     * 开始事务
     * @throws SalaryException 如果开启事务失败
     */
    public void beginTransaction() throws SalaryException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw SalaryException.databaseQuery(e);
        }
    }
    
    /**
     * 提交事务
     * @param conn 数据库连接
     * @throws SalaryException 如果提交事务失败
     */
    public void commit(Connection conn) throws SalaryException {
        if (conn != null) {
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                throw SalaryException.databaseQuery(e);
            } finally {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * 回滚事务
     * @param conn 数据库连接
     */
    public void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                // 忽略回滚异常
            } finally {
                returnConnection(conn);
            }
        }
    }
}
