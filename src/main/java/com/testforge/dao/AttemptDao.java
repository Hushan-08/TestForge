package com.testforge.dao;

import com.testforge.database.DatabaseManager;
import com.testforge.model.ExamAttempt;
import com.testforge.model.StudentAnswer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Student Exam Attempts and In-Progress Answer Tracking.
 */
public class AttemptDao {

    public ExamAttempt createAttempt(ExamAttempt attempt) throws SQLException {
        String sql = """
            INSERT INTO exam_attempts (exam_id, student_id, start_time, status, question_order_seed)
            VALUES (?, ?, ?, 'IN_PROGRESS', ?);
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, attempt.getExamId());
            pstmt.setInt(2, attempt.getStudentId());
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.setInt(4, attempt.getQuestionOrderSeed());

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    attempt.setId(rs.getInt(1));
                }
            }
        }
        return attempt;
    }

    public ExamAttempt findActiveAttempt(int examId, int studentId) throws SQLException {
        String sql = """
            SELECT a.*, e.title as exam_title, e.duration_minutes
            FROM exam_attempts a
            JOIN exams e ON a.exam_id = e.id
            WHERE a.exam_id = ? AND a.student_id = ? AND a.status = 'IN_PROGRESS'
            ORDER BY a.id DESC LIMIT 1;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            pstmt.setInt(2, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        }
        return null;
    }

    public boolean hasCompletedAttempt(int examId, int studentId) throws SQLException {
        String sql = """
            SELECT 1 FROM exam_attempts
            WHERE exam_id = ? AND student_id = ? AND status IN ('SUBMITTED', 'TIMED_OUT');
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            pstmt.setInt(2, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public ExamAttempt findAttemptById(int attemptId) throws SQLException {
        String sql = """
            SELECT a.*, u.full_name as student_name, u.email as student_email,
                   e.title as exam_title, e.duration_minutes
            FROM exam_attempts a
            JOIN users u ON a.student_id = u.id
            JOIN exams e ON a.exam_id = e.id
            WHERE a.id = ?;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attemptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttempt(rs);
                }
            }
        }
        return null;
    }

    public List<ExamAttempt> findAttemptsByStudentId(int studentId) throws SQLException {
        List<ExamAttempt> attempts = new ArrayList<>();
        String sql = """
            SELECT a.*, e.title as exam_title, e.duration_minutes
            FROM exam_attempts a
            JOIN exams e ON a.exam_id = e.id
            WHERE a.student_id = ?
            ORDER BY a.id DESC;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToAttempt(rs));
                }
            }
        }
        return attempts;
    }

    public List<ExamAttempt> findAttemptsByExamId(int examId) throws SQLException {
        List<ExamAttempt> attempts = new ArrayList<>();
        String sql = """
            SELECT a.*, u.full_name as student_name, u.email as student_email,
                   e.title as exam_title, e.duration_minutes
            FROM exam_attempts a
            JOIN users u ON a.student_id = u.id
            JOIN exams e ON a.exam_id = e.id
            WHERE a.exam_id = ?
            ORDER BY a.id DESC;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    attempts.add(mapResultSetToAttempt(rs));
                }
            }
        }
        return attempts;
    }

    /**
     * Periodic autosave / immediate option update.
     * Uses SQLite ON CONFLICT DO UPDATE for safe upsert.
     */
    public void saveOrUpdateAnswer(int attemptId, int questionId, Integer selectedOptionId, boolean isMarked) throws SQLException {
        String sql = """
            INSERT INTO student_answers (attempt_id, question_id, selected_option_id, is_marked_for_review, updated_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(attempt_id, question_id) DO UPDATE SET
                selected_option_id = excluded.selected_option_id,
                is_marked_for_review = excluded.is_marked_for_review,
                updated_at = CURRENT_TIMESTAMP;
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attemptId);
            pstmt.setInt(2, questionId);
            if (selectedOptionId != null) {
                pstmt.setInt(3, selectedOptionId);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setInt(4, isMarked ? 1 : 0);

            pstmt.executeUpdate();
        }
    }

    public Map<Integer, StudentAnswer> getSavedAnswers(int attemptId) throws SQLException {
        Map<Integer, StudentAnswer> map = new HashMap<>();
        String sql = "SELECT * FROM student_answers WHERE attempt_id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, attemptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int qId = rs.getInt("question_id");
                    int optId = rs.getInt("selected_option_id");
                    Integer selectedOpt = rs.wasNull() ? null : optId;
                    boolean marked = rs.getInt("is_marked_for_review") == 1;

                    StudentAnswer answer = new StudentAnswer(
                            rs.getInt("id"),
                            attemptId,
                            qId,
                            selectedOpt,
                            marked,
                            LocalDateTime.now()
                    );
                    map.put(qId, answer);
                }
            }
        }
        return map;
    }

    public boolean finalizeAttempt(int attemptId, double score, double totalMarks, double percentage, String status) throws SQLException {
        String sql = """
            UPDATE exam_attempts
            SET submit_time = CURRENT_TIMESTAMP,
                score = ?,
                total_marks = ?,
                percentage = ?,
                status = ?
            WHERE id = ? AND status = 'IN_PROGRESS';
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, score);
            pstmt.setDouble(2, totalMarks);
            pstmt.setDouble(3, percentage);
            pstmt.setString(4, status);
            pstmt.setInt(5, attemptId);

            return pstmt.executeUpdate() > 0;
        }
    }

    private ExamAttempt mapResultSetToAttempt(ResultSet rs) throws SQLException {
        ExamAttempt attempt = new ExamAttempt();
        attempt.setId(rs.getInt("id"));
        attempt.setExamId(rs.getInt("exam_id"));
        attempt.setStudentId(rs.getInt("student_id"));

        String startStr = rs.getString("start_time");
        if (startStr != null) {
            try {
                attempt.setStartTime(LocalDateTime.parse(startStr.replace(" ", "T")));
            } catch (Exception e) {
                attempt.setStartTime(LocalDateTime.now());
            }
        }

        String subStr = rs.getString("submit_time");
        if (subStr != null) {
            try {
                attempt.setSubmitTime(LocalDateTime.parse(subStr.replace(" ", "T")));
            } catch (Exception e) {
                attempt.setSubmitTime(LocalDateTime.now());
            }
        }

        attempt.setScore(rs.getDouble("score"));
        attempt.setTotalMarks(rs.getDouble("total_marks"));
        attempt.setPercentage(rs.getDouble("percentage"));
        attempt.setStatus(rs.getString("status"));
        attempt.setQuestionOrderSeed(rs.getInt("question_order_seed"));

        try {
            attempt.setStudentName(rs.getString("student_name"));
        } catch (SQLException ignored) {}
        try {
            attempt.setStudentEmail(rs.getString("student_email"));
        } catch (SQLException ignored) {}
        try {
            attempt.setExamTitle(rs.getString("exam_title"));
        } catch (SQLException ignored) {}
        try {
            attempt.setDurationMinutes(rs.getInt("duration_minutes"));
        } catch (SQLException ignored) {}

        return attempt;
    }
}
