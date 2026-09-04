package com.salary.service;

import com.salary.model.Score;
import com.salary.dao.ScoreDAO;
import com.salary.exception.SalaryException;
import com.salary.util.ExcelUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩业务服务类
 * 提供成绩的添加、批量导入、统计和导出功能
 */
public class ScoreService {

    private ScoreDAO scoreDAO;

    /**
     * 构造函数，初始化DAO对象
     */
    public ScoreService() {
        this.scoreDAO = new ScoreDAO();
    }

    /**
     * 添加单条成绩记录
     * 
     * @param score 成绩对象
     * @return 成功添加的成绩ID
     * @throws SalaryException 如果添加失败
     */
    public Long addScore(Score score) {
        validateScore(score);

        // 设置创建时间
        if (score.getCreateTime() == null) {
            score.setCreateTime(LocalDateTime.now());
        }

        int result = scoreDAO.insert(score);
        if (result <= 0) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "成绩记录添加失败");
        }

        return score.getId();
    }

    /**
     * 批量导入成绩记录
     * 使用事务确保数据一致性
     * 
     * @param scores 成绩列表
     * @return 成功导入的记录数
     * @throws SalaryException 如果导入失败
     */
    public int batchImportScores(List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "成绩列表不能为空");
        }

        // 验证所有成绩数据
        for (Score score : scores) {
            validateScore(score);
        }

        int result = scoreDAO.batchInsert(scores);
        if (result <= 0) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "批量导入成绩失败");
        }

        return result;
    }

    /**
     * 计算指定科目的优秀率
     * 优秀率 = 达到优秀标准的人数 / 参考总人数
     * 
     * @param subject 科目名称
     * @param excellentStandard 优秀分数线
     * @return 优秀率（0-1之间的小数）
     * @throws SalaryException 如果计算失败
     */
    public double calculateExcellentRate(String subject, double excellentStandard) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "科目名称不能为空");
        }

        if (excellentStandard < 0 || excellentStandard > 100) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "优秀分数线必须在0-100之间");
        }

        try {
            return scoreDAO.getExcellentRate(subject, excellentStandard);
        } catch (Exception e) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "计算优秀率失败", e);
        }
    }

    /**
     * 查找指定科目不及格的学生
     * 
     * @param subject 科目名称
     * @param passingScore 及格分数线
     * @return 不及格学生成绩列表
     * @throws SalaryException 如果查询失败
     */
    public List<Score> findFailedStudents(String subject, double passingScore) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "科目名称不能为空");
        }

        if (passingScore < 0 || passingScore > 100) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "及格分数线必须在0-100之间");
        }

        try {
            List<Score> allScores = scoreDAO.findBySubject(subject);
            List<Score> failedStudents = new ArrayList<>();

            for (Score score : allScores) {
                if (score.getScore().doubleValue() < passingScore) {
                    failedStudents.add(score);
                }
            }

            return failedStudents;
        } catch (Exception e) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "查询不及格学生失败", e);
        }
    }

    /**
     * 获取指定科目的统计信息
     * 包括：参考人数、平均分、最高分、最低分、及格率、优秀率
     * 
     * @param subject 科目名称
     * @return 统计信息Map，包含：
     *         count - 参考人数
     *         avgScore - 平均分
     *         maxScore - 最高分
     *         minScore - 最低分
     *         passRate - 及格率
     *         excellentRate - 优秀率（90分以上）
     * @throws SalaryException 如果获取统计失败
     */
    public Map<String, Object> getSubjectStatistics(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "科目名称不能为空");
        }

        try {
            Map<String, Object> stats = scoreDAO.getStatisticsBySubject(subject);
            
            // 计算优秀率（默认90分以上为优秀）
            double excellentRate = scoreDAO.getExcellentRate(subject, 90);
            stats.put("excellentRate", excellentRate);

            return stats;
        } catch (Exception e) {
            throw new SalaryException(SalaryException.ERR_DATABASE_QUERY, "获取科目统计信息失败", e);
        }
    }

    /**
     * 导出不及格学生到Excel文件
     * 
     * @param subject 科目名称
     * @param passingScore 及格分数线
     * @param filePath 导出文件路径
     * @throws SalaryException 如果导出失败
     */
    public void exportFailedStudentsToExcel(String subject, double passingScore, String filePath) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "科目名称不能为空");
        }

        if (passingScore < 0 || passingScore > 100) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "及格分数线必须在0-100之间");
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "导出文件路径不能为空");
        }

        if (!ExcelUtil.isValidExcelFile(filePath)) {
            throw new SalaryException(SalaryException.ERR_FILE_IO, "文件格式不正确，请使用.xls或.xlsx格式");
        }

        List<Score> failedStudents = findFailedStudents(subject, passingScore);
        if (failedStudents == null || failedStudents.isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_NOT_FOUND, 
                    "未找到" + subject + "科目不及格的学生");
        }

        // 准备表头
        String[] headers = {"学号", "姓名", "科目", "分数", "考试日期"};

        // 准备数据
        List<String[]> data = new ArrayList<>();
        for (Score score : failedStudents) {
            String[] row = {
                score.getStudentCode(),
                score.getStudentName(),
                score.getSubject(),
                score.getScore() != null ? score.getScore().toString() : "0",
                score.getExamDate() != null ? score.getExamDate().toString() : ""
            };
            data.add(row);
        }

        // 导出到Excel
        ExcelUtil.exportToExcel(data, headers, filePath);
    }

    /**
     * 验证成绩数据的合法性
     * 
     * @param score 成绩对象
     * @throws SalaryException 如果数据不合法
     */
    private void validateScore(Score score) {
        if (score == null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "成绩信息不能为空");
        }

        if (score.getStudentId() == null || score.getStudentId() <= 0) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "学生ID无效");
        }

        if (score.getStudentName() == null || score.getStudentName().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "学生姓名不能为空");
        }

        if (score.getSubject() == null || score.getSubject().trim().isEmpty()) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "科目名称不能为空");
        }

        if (score.getScore() == null) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "分数不能为空");
        }

        double scoreValue = score.getScore().doubleValue();
        if (scoreValue < 0 || scoreValue > 100) {
            throw new SalaryException(SalaryException.ERR_DATA_VALIDATION, "分数必须在0-100之间");
        }
    }
}
