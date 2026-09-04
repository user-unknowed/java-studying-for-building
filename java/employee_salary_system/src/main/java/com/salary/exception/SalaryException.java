package com.salary.exception;

import java.io.IOException;

/**
 * 自定义薪资系统异常类
 * 继承RuntimeException，提供错误码追踪功能
 */
public class SalaryException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    // 错误码常量
    public static final String ERR_DATABASE_CONNECTION = "DB001";
    public static final String ERR_DATABASE_QUERY = "DB002";
    public static final String ERR_DATA_VALIDATION = "VAL001";
    public static final String ERR_DATA_NOT_FOUND = "VAL002";
    public static final String ERR_FILE_IO = "IO001";
    public static final String ERR_FILE_NOT_FOUND = "IO002";
    public static final String ERR_EXCEL_EXPORT = "EXL001";
    public static final String ERR_EXCEL_IMPORT = "EXL002";
    public static final String ERR_CONFIG_LOAD = "CFG001";
    public static final String ERR_UNKNOWN = "SYS999";
    
    // 中文错误消息常量
    public static final String MSG_DATABASE_CONNECTION = "数据库连接失败，请检查数据库配置和网络连接";
    public static final String MSG_DATABASE_QUERY = "数据库查询失败，请检查SQL语句和数据完整性";
    public static final String MSG_DATA_VALIDATION = "数据验证失败，请检查输入数据的合法性";
    public static final String MSG_DATA_NOT_FOUND = "未找到相关数据，请检查查询条件";
    public static final String MSG_FILE_IO = "文件读写操作失败，请检查文件权限和路径";
    public static final String MSG_FILE_NOT_FOUND = "指定的文件不存在，请检查文件路径";
    public static final String MSG_EXCEL_EXPORT = "Excel导出失败，请检查导出路径和磁盘空间";
    public static final String MSG_EXCEL_IMPORT = "Excel导入失败，请检查文件格式和数据完整性";
    public static final String MSG_CONFIG_LOAD = "配置文件加载失败，请检查config.properties文件";
    public static final String MSG_UNKNOWN = "系统发生未知错误，请联系管理员";
    
    // 错误码属性
    private String errorCode;
    
    /**
     * 默认构造函数
     */
    public SalaryException() {
        super();
        this.errorCode = ERR_UNKNOWN;
    }
    
    /**
     * 带错误消息的构造函数
     * @param message 错误消息描述
     */
    public SalaryException(String message) {
        super(message);
        this.errorCode = ERR_UNKNOWN;
    }
    
    /**
     * 带错误消息和根因异常的构造函数
     * @param message 错误消息描述
     * @param cause 根因异常
     */
    public SalaryException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ERR_UNKNOWN;
    }
    
    /**
     * 带错误码和错误消息的构造函数
     * @param errorCode 错误码
     * @param message 错误消息描述
     */
    public SalaryException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 带错误码、错误消息和根因异常的构造函数
     * @param errorCode 错误码
     * @param message 错误消息描述
     * @param cause 根因异常
     */
    public SalaryException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误码
     * @return 错误码字符串
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 设置错误码
     * @param errorCode 错误码字符串
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * 根据错误码获取对应的中文错误消息
     * @param errorCode 错误码
     * @return 中文错误消息
     */
    public static String getMessageByCode(String errorCode) {
        switch (errorCode) {
            case ERR_DATABASE_CONNECTION:
                return MSG_DATABASE_CONNECTION;
            case ERR_DATABASE_QUERY:
                return MSG_DATABASE_QUERY;
            case ERR_DATA_VALIDATION:
                return MSG_DATA_VALIDATION;
            case ERR_DATA_NOT_FOUND:
                return MSG_DATA_NOT_FOUND;
            case ERR_FILE_IO:
                return MSG_FILE_IO;
            case ERR_FILE_NOT_FOUND:
                return MSG_FILE_NOT_FOUND;
            case ERR_EXCEL_EXPORT:
                return MSG_EXCEL_EXPORT;
            case ERR_EXCEL_IMPORT:
                return MSG_EXCEL_IMPORT;
            case ERR_CONFIG_LOAD:
                return MSG_CONFIG_LOAD;
            default:
                return MSG_UNKNOWN;
        }
    }
    
    /**
     * 创建数据库连接失败的异常
     * @param cause 根因异常
     * @return SalaryException实例
     */
    public static SalaryException databaseConnection(Throwable cause) {
        return new SalaryException(ERR_DATABASE_CONNECTION, MSG_DATABASE_CONNECTION, cause);
    }
    
    /**
     * 创建数据库查询失败的异常
     * @param cause 根因异常
     * @return SalaryException实例
     */
    public static SalaryException databaseQuery(Throwable cause) {
        return new SalaryException(ERR_DATABASE_QUERY, MSG_DATABASE_QUERY, cause);
    }
    
    /**
     * 创建数据验证失败的异常
     * @param cause 根因异常
     * @return SalaryException实例
     */
    public static SalaryException dataValidation(Throwable cause) {
        return new SalaryException(ERR_DATA_VALIDATION, MSG_DATA_VALIDATION, cause);
    }
    
    /**
     * 创建Excel导出失败的异常
     * @param cause 根因异常
     * @return SalaryException实例
     */
    public static SalaryException excelExport(Throwable cause) {
        return new SalaryException(ERR_EXCEL_EXPORT, MSG_EXCEL_EXPORT, cause);
    }
    
    /**
     * 创建Excel导入失败的异常
     * @param cause 根因异常
     * @return SalaryException实例
     */
    public static SalaryException excelImport(Throwable cause) {
        return new SalaryException(ERR_EXCEL_IMPORT, MSG_EXCEL_IMPORT, cause);
    }
    
    @Override
    public String toString() {
        return "SalaryException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
