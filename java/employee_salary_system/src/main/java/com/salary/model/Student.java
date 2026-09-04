package com.salary.model;

import java.time.LocalDateTime;

/**
 * 学生模型类
 * 
 * @author salary-system
 * @version 1.0
 */
public class Student {
    
    /** 学生ID */
    private Long id;
    
    /** 学号 */
    private String studentCode;
    
    /** 学生姓名 */
    private String name;
    
    /** 性别 */
    private String gender;
    
    /** 班级 */
    private String className;
    
    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 无参构造函数
     */
    public Student() {
    }

    /**
     * 带参构造函数
     * 
     * @param id 学生ID
     * @param studentCode 学号
     * @param name 学生姓名
     * @param gender 性别
     * @param className 班级
     * @param createTime 创建时间
     */
    public Student(Long id, String studentCode, String name, String gender,
                   String className, LocalDateTime createTime) {
        this.id = id;
        this.studentCode = studentCode;
        this.name = name;
        this.gender = gender;
        this.className = className;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentCode='" + studentCode + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", className='" + className + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
