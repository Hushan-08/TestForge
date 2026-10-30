package com.testforge.dao;

import com.testforge.database.DatabaseManager;
import com.testforge.model.Exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Exam management.
 */
public class ExamDao {

    public Exam createExam(Exam exam) throws SQLException {
        String sql = """
            INSERT INTO exams (creator_id, title, description, access_code, duration_minutes, pass_percentage, is_published, release_results)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, exam.getCreatorId());
            pstmt.setString(2, exam.getTitle().trim());
            pstmt.setString(3, exam.getDescription() != null ? exam.getDescription().trim() : "");
            pstmt.setString(4, exam.getAccessCode().trim().toUpperCase());
            pstmt.setInt(5, exam.getDurationMinutes());
            pstmt.setDouble(6, exam.getPassPercentage());
            pstmt.setInt(7, exam.isPublished() ? 1 : 0);
            pstmt.setInt(8, exam.isReleaseResults() ? 1 : 0);

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    exam.setId(rs.getInt(1));
                }
            }
        }
        return exam;
    }

    public boolean updateExam(Exam exam) throws SQLException {
        String sql = """
            UPDATE exams
            SET title = ?, description = ?, access_code = ?, duration_minutes = ?,
                pass_percentage = ?, is_published = ?, release_results = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, exam.getTitle().trim());
            pstmt.setString(2, exam.getDescription() != null ? exam.getDescription().trim() : "");
            pstmt.setString(3, exam.getAccessCode().trim().toUpperCase());
            pstmt.setInt(4, exam.getDurationMinutes());
            pstmt.setDouble(5, exam.getPassPercentage());
            pstmt.setInt(6, exam.isPublished() ? 1 : 0);
            pstmt.setInt(7, exam.isReleaseResults() ? 1 : 0);
            pstmt.setInt(8, exam.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteExam(int examId) throws SQLException {
        String sql = "DELETE FROM exams WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Exam findById(int examId) throws SQLException {
        String sql = """
            SELECT e.*, u.full_name as creator_name,
                   COUNT(q.id) as question_count,
                   COALESCE(SUM(q.marks), 0) as total_marks
            FROM exams e
            LEFT JOIN users u ON e.creator_id = u.id
            LEFT JOIN questions q ON e.id = q.exam_id
            WHERE e.id = ?
            GROUP BY e.id;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExam(rs);
                }
            }
        }
        return null;
    }

    public Exam findByAccessCode(String accessCode) throws SQLException {
        String sql = """
            SELECT e.*, u.full_name as creator_name,
                   COUNT(q.id) as question_count,
                   COALESCE(SUM(q.marks), 0) as total_marks
            FROM exams e
            LEFT JOIN users u ON e.creator_id = u.id
            LEFT JOIN questions q ON e.id = q.exam_id
            WHERE UPPER(e.access_code) = UPPER(?)
            GROUP BY e.id;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accessCode.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExam(rs);
                }
            }
        }
        return null;
    }

    public List<Exam> findByCreatorId(int creatorId) throws SQLException {
        List<Exam> exams = new ArrayList<>();
        String sql = """
            SELECT e.*, u.full_name as creator_name,
                   COUNT(q.id) as question_count,
                   COALESCE(SUM(q.marks), 0) as total_marks
            FROM exams e
            LEFT JOIN users u ON e.creator_id = u.id
            LEFT JOIN questions q ON e.id = q.exam_id
            WHERE e.creator_id = ?
            GROUP BY e.id
            ORDER BY e.created_at DESC;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, creatorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    exams.add(mapResultSetToExam(rs));
                }
            }
        }
        return exams;
    }

    public boolean togglePublish(int examId, boolean isPublished) throws SQLException {
        String sql = "UPDATE exams SET is_published = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, isPublished ? 1 : 0);
            pstmt.setInt(2, examId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean toggleReleaseResults(int examId, boolean releaseResults) throws SQLException {
        String sql = "UPDATE exams SET release_results = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, releaseResults ? 1 : 0);
            pstmt.setInt(2, examId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean existsByAccessCode(String accessCode, int excludeExamId) throws SQLException {
        String sql = "SELECT 1 FROM exams WHERE UPPER(access_code) = UPPER(?) AND id != ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accessCode.trim());
            pstmt.setInt(2, excludeExamId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Exam mapResultSetToExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam(
                rs.getInt("id"),
                rs.getInt("creator_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("access_code"),
                rs.getInt("duration_minutes"),
                rs.getDouble("pass_percentage"),
                rs.getInt("is_published") == 1,
                rs.getInt("release_results") == 1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        exam.setCreatorName(rs.getString("creator_name"));
        exam.setQuestionCount(rs.getInt("question_count"));
        exam.setTotalMarks(rs.getInt("total_marks"));
        return exam;
    }
}
