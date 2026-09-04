package com.salary.dao;

import com.salary.entity.Score;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩数据访问类
 * 提供学生成绩数据的增删改查操作
 */
public class ScoreDAO extends BaseDAO<Score> {

    @Override
    protected String getTableName() {
        return "score";
    }

    @Override
    protected Score mapResultSetToEntity(ResultSet rs) throws SQLException {
        Score score = new Score();
        score.setId(rs.getInt("id"));
        score.setStudentId(rs.getInt("student_id"));
        score.setStudentName(rs.getString("student_name"));
        score.setSubject(rs.getString("subject"));
        score.setScore(rs.getDouble("score"));
        score.setExamDate(rs.getDate("exam_date"));
        score.setCreateTime(rs.getTimestamp("create_time"));
        return score;
    }

    /**
     * 新增成绩记录
     * @param score 成绩实体
     * @return 受影响的行数
     */
    public int insert(Score score) {
        String sql = "INSERT INTO score (student_id, student_name, subject, score, exam_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        return executeUpdate(sql,
            score.getStudentId(),
            score.getStudentName(),
            score.getSubject(),
            score.getScore(),
            score.getExamDate()
        );
    }

    /**
     * 批量新增成绩
     * @param scores 成绩列表
     * @return 成功插入的记录数
     */
    public int batchInsert(List<Score> scores) {
        String sql = "INSERT INTO score (student_id, student_name, subject, score, exam_date) " +
                     "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int count = 0;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(sql);
            for (Score score : scores) {
                pstmt.setInt(1, score.getStudentId());
                pstmt.setString(2, score.getStudentName());
                pstmt.setString(3, score.getSubject());
                pstmt.setDouble(4, score.getScore());
                pstmt.setDate(5, score.getExamDate());
                pstmt.addBatch();
                count++;
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("批量新增成绩失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库批量插入异常", e);
        } finally {
            closeResources(null, pstmt, conn);
        }
        return count;
    }

    /**
     * 查询某学生所有成绩
     * @param studentId 学生ID
     * @return 成绩列表
     */
    public List<Score> findByStudentId(int studentId) {
        String sql = "SELECT * FROM score WHERE student_id = ? ORDER BY exam_date DESC";
        return executeQuery(sql, studentId);
    }

    /**
     * 按科目查询成绩
     * @param subject 科目名称
     * @return 成绩列表
     */
    public List<Score> findBySubject(String subject) {
        String sql = "SELECT * FROM score WHERE subject = ? ORDER BY score DESC";
        return executeQuery(sql, subject);
    }

    /**
     * 查询不及格学生
     * @param passingScore 及格分数线
     * @return 不及格学生列表
     */
    public List<Score> findFailedStudents(double passingScore) {
        String sql = "SELECT * FROM score WHERE score < ? ORDER BY score";
        return executeQuery(sql, passingScore);
    }

    /**
     * 计算优秀率
     * @param subject 科目名称
     * @param excellentScore 优秀分数线
     * @return 优秀率（0-1之间的小数）
     */
    public double getExcellentRate(String subject, double excellentScore) {
        String totalSql = "SELECT COUNT(*) FROM score WHERE subject = ?";
        String excellentSql = "SELECT COUNT(*) FROM score WHERE subject = ? AND score >= ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            int total = 0;
            int excellent = 0;

            pstmt = conn.prepareStatement(totalSql);
            pstmt.setString(1, subject);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
            closeResources(rs, pstmt, null);

            if (total == 0) {
                return 0.0;
            }

            pstmt = conn.prepareStatement(excellentSql);
            pstmt.setString(1, subject);
            pstmt.setDouble(2, excellentScore);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                excellent = rs.getInt(1);
            }

            return (double) excellent / total;
        } catch (SQLException e) {
            System.err.println("计算优秀率失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库查询异常", e);
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    /**
     * 获取科目统计信息
     * @param subject 科目名称
     * @return 统计信息Map，包含：
     *         count - 参考人数
     *         avgScore - 平均分
     *         maxScore - 最高分
     *         minScore - 最低分
     *         passRate - 及格率
     *         excellentRate - 优秀率
     */
    public Map<String, Object> getStatisticsBySubject(String subject) {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT COUNT(*) as count, AVG(score) as avg_score, MAX(score) as max_score, " +
                     "MIN(score) as min_score FROM score WHERE subject = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, subject);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("count", rs.getInt("count"));
                stats.put("avgScore", rs.getDouble("avg_score"));
                stats.put("maxScore", rs.getDouble("max_score"));
                stats.put("minScore", rs.getDouble("min_score"));
            }
        } catch (SQLException e) {
            System.err.println("获取科目统计信息失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库查询异常", e);
        } finally {
            closeResources(rs, pstmt, conn);
        }

        // 计算及格率（60分及以上）
        String passSql = "SELECT COUNT(*) FROM score WHERE subject = ? AND score >= 60";
        try {
            int total = (int) stats.getOrDefault("count", 0);
            if (total > 0) {
                pstmt = conn.prepareStatement(passSql);
                pstmt.setString(1, subject);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("passRate", (double) rs.getInt(1) / total);
                }
            } else {
                stats.put("passRate", 0.0);
            }
        } catch (SQLException e) {
            System.err.println("计算及格率失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return stats;
    }
}
