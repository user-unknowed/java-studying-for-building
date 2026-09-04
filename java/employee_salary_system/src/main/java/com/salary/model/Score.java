package com.salary.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成绩模型类
 * 
 * @author salary-system
 * @version 1.0
 */
public class Score {
    
    /** 成绩ID */
    private Long id;
    
    /** 学生ID */
    private Long studentId;
    
    /** 学号 */
    private String studentCode;
    
    /** 学生姓名 */
    private String studentName;
    
    /** 科目 */
    private String subject;
    
    /** 分数 */
    private BigDecimal score;
    
    /** 考试日期 */
    private LocalDate examDate;
    
    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 无参构造函数
     */
    public Score() {
    }

    /**
     * 带参构造函数
     * 
     * @param id 成绩ID
     * @param studentId 学生ID
     * @param studentCode 学号
     * @param studentName 学生姓名
     * @param subject 科目
     * @param score 分数
     * @param examDate 考试日期
     * @param createTime 创建时间
     */
    public Score(Long id, Long studentId, String studentCode, String studentName,
                 String subject, BigDecimal score, LocalDate examDate, LocalDateTime createTime) {
        this.id = id;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.subject = subject;
        this.score = score;
        this.examDate = examDate;
        this.createTime = createTime;
    }

    // Getter和Setter方法
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", studentCode='" + studentCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", score=" + score +
                ", examDate=" + examDate +
                ", createTime=" + createTime +
                '}';
    }
}
