package com.salary.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置工具类
 * 用于读取和访问config.properties配置文件
 * 采用单例模式管理Properties对象
 */
public class ConfigUtil {
    
    private static volatile ConfigUtil instance;
    private Properties properties;
    
    // 配置文件路径
    private static final String CONFIG_FILE = "config.properties";
    
    /**
     * 私有构造函数，防止外部实例化
     */
    private ConfigUtil() {
        properties = new Properties();
        loadConfig();
    }
    
    /**
     * 获取ConfigUtil单例实例（双重检查锁定）
     * @return ConfigUtil实例
     */
    public static ConfigUtil getInstance() {
        if (instance == null) {
            synchronized (ConfigUtil.class) {
                if (instance == null) {
                    instance = new ConfigUtil();
                }
            }
        }
        return instance;
    }
    
    /**
     * 从配置文件加载配置项
     */
    private void loadConfig() {
        InputStream inputStream = null;
        try {
            // 从类路径加载配置文件
            inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (inputStream == null) {
                // 尝试从文件系统加载
                inputStream = new java.io.FileInputStream(CONFIG_FILE);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new com.salary.exception.SalaryException(
                com.salary.exception.SalaryException.ERR_CONFIG_LOAD,
                com.salary.exception.SalaryException.MSG_CONFIG_LOAD,
                e
            );
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    /**
     * 根据键获取配置值
     * @param key 配置键名
     * @return 配置值，如果键不存在返回null
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 根据键获取配置值，如果键不存在返回默认值
     * @param key 配置键名
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取数据库连接URL
     * @return 数据库URL
     */
    public String getDatabaseUrl() {
        return getProperty("db.url");
    }
    
    /**
     * 获取数据库用户名
     * @return 数据库用户名
     */
    public String getDatabaseUsername() {
        return getProperty("db.username");
    }
    
    /**
     * 获取数据库密码
     * @return 数据库密码
     */
    public String getDatabasePassword() {
        return getProperty("db.password");
    }
    
    /**
     * 获取数据库驱动类名
     * @return 数据库驱动类名
     */
    public String getDatabaseDriver() {
        return getProperty("db.driver");
    }
    
    /**
     * 获取Excel导出路径
     * @return 导出路径
     */
    public String getExcelExportPath() {
        return getProperty("excel.export.path");
    }
    
    /**
     * 获取Excel导入路径
     * @return 导入路径
     */
    public String getExcelImportPath() {
        return getProperty("excel.import.path");
    }
    
    /**
     * 重新加载配置文件
     */
    public void reload() {
        properties.clear();
        loadConfig();
    }
}
