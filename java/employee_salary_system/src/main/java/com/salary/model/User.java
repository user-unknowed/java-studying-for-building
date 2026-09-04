package com.salary.model;

import java.time.LocalDateTime;

/**
 * 用户模型类
 * 
 * @author salary-system
 * @version 1.0
 */
public class User {
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 真实姓名 */
    private String realName;
    
    /** 角色 */
    private String role;
    
    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 无参构造函数
     */
    public User() {
    }

    /**
     * 带参构造函数
     * 
     * @param id 用户ID
     * @param username 用户名
     * @param password 密码
     * @param realName 真实姓名
     * @param role 角色
     * @param createTime 创建时间
     */
    public User(Long id, String username, String password, String realName,
                String role, LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", realName='" + realName + '\'' +
                ", role='" + role + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
