package com.testforge.dao;

import com.testforge.database.DatabaseManager;
import com.testforge.model.AnswerOption;
import com.testforge.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Questions and Answer Options.
 */
public class QuestionDao {

    public Question createQuestion(Question question) throws SQLException {
        String qSql = "INSERT INTO questions (exam_id, question_text, marks, explanation) VALUES (?, ?, ?, ?);";
        String optSql = "INSERT INTO answer_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?);";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement pstmt = conn.prepareStatement(qSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, question.getExamId());
                pstmt.setString(2, question.getQuestionText().trim());
                pstmt.setInt(3, question.getMarks());
                pstmt.setString(4, question.getExplanation() != null ? question.getExplanation().trim() : "");
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        question.setId(rs.getInt(1));
                    }
                }
            }

            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                try (PreparedStatement pstmt = conn.prepareStatement(optSql, Statement.RETURN_GENERATED_KEYS)) {
                    int order = 1;
                    for (AnswerOption option : question.getOptions()) {
                        pstmt.setInt(1, question.getId());
                        pstmt.setString(2, option.getOptionText().trim());
                        pstmt.setInt(3, option.isCorrect() ? 1 : 0);
                        pstmt.setInt(4, order++);
                        pstmt.executeUpdate();

                        try (ResultSet rs = pstmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                option.setId(rs.getInt(1));
                            }
                        }
                    }
                }
            }

            conn.commit(); // Commit transaction
            return question;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean updateQuestion(Question question) throws SQLException {
        String updateQSql = "UPDATE questions SET question_text = ?, marks = ?, explanation = ? WHERE id = ?;";
        String deleteOptsSql = "DELETE FROM answer_options WHERE question_id = ?;";
        String insertOptSql = "INSERT INTO answer_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?);";

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateQSql)) {
                pstmt.setString(1, question.getQuestionText().trim());
                pstmt.setInt(2, question.getMarks());
                pstmt.setString(3, question.getExplanation() != null ? question.getExplanation().trim() : "");
                pstmt.setInt(4, question.getId());
                pstmt.executeUpdate();
            }

            // Replace options
            try (PreparedStatement pstmt = conn.prepareStatement(deleteOptsSql)) {
                pstmt.setInt(1, question.getId());
                pstmt.executeUpdate();
            }

            if (question.getOptions() != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertOptSql, Statement.RETURN_GENERATED_KEYS)) {
                    int order = 1;
                    for (AnswerOption option : question.getOptions()) {
                        pstmt.setInt(1, question.getId());
                        pstmt.setString(2, option.getOptionText().trim());
                        pstmt.setInt(3, option.isCorrect() ? 1 : 0);
                        pstmt.setInt(4, order++);
                        pstmt.executeUpdate();

                        try (ResultSet rs = pstmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                option.setId(rs.getInt(1));
                            }
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Question> findQuestionsByExamId(int examId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE exam_id = ? ORDER BY id ASC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, examId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question(
                            rs.getInt("id"),
                            rs.getInt("exam_id"),
                            rs.getString("question_text"),
                            rs.getInt("marks"),
                            rs.getString("explanation"),
                            LocalDateTime.now()
                    );
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public Question findQuestionById(int questionId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Question q = new Question(
                            rs.getInt("id"),
                            rs.getInt("exam_id"),
                            rs.getString("question_text"),
                            rs.getInt("marks"),
                            rs.getString("explanation"),
                            LocalDateTime.now()
                    );
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    return q;
                }
            }
        }
        return null;
    }

    public List<AnswerOption> findOptionsByQuestionId(int questionId) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            return findOptionsByQuestionId(conn, questionId);
        }
    }

    private List<AnswerOption> findOptionsByQuestionId(Connection conn, int questionId) throws SQLException {
        List<AnswerOption> options = new ArrayList<>();
        String sql = "SELECT * FROM answer_options WHERE question_id = ? ORDER BY option_order ASC;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    options.add(new AnswerOption(
                            rs.getInt("id"),
                            rs.getInt("question_id"),
                            rs.getString("option_text"),
                            rs.getInt("is_correct") == 1,
                            rs.getInt("option_order")
                    ));
                }
            }
        }
        return options;
    }
}
