package com.testforge.database;

import com.testforge.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database tables and ensures demo accounts and a sample exam exist.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseManager.getConnection()) {
            createTables(conn);
            seedDemoData(conn);
            System.out.println("TestForge database initialized successfully at: " + DatabaseManager.getDatabasePath());
        } catch (SQLException e) {
            System.err.println("Fatal Error initializing SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    full_name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(role IN ('CREATOR', 'STUDENT')),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            // 2. Exams Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS exams (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    creator_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    access_code TEXT UNIQUE NOT NULL,
                    duration_minutes INTEGER NOT NULL DEFAULT 30,
                    pass_percentage REAL NOT NULL DEFAULT 50.0,
                    is_published INTEGER NOT NULL DEFAULT 0,
                    release_results INTEGER NOT NULL DEFAULT 1,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 3. Questions Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    exam_id INTEGER NOT NULL,
                    question_text TEXT NOT NULL,
                    marks INTEGER NOT NULL DEFAULT 1,
                    explanation TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
                );
            """);

            // 4. Answer Options Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS answer_options (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    question_id INTEGER NOT NULL,
                    option_text TEXT NOT NULL,
                    is_correct INTEGER NOT NULL DEFAULT 0,
                    option_order INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
                );
            """);

            // 5. Exam Attempts Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS exam_attempts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    exam_id INTEGER NOT NULL,
                    student_id INTEGER NOT NULL,
                    start_time TIMESTAMP NOT NULL,
                    submit_time TIMESTAMP,
                    score REAL DEFAULT 0.0,
                    total_marks REAL DEFAULT 0.0,
                    percentage REAL DEFAULT 0.0,
                    status TEXT NOT NULL DEFAULT 'IN_PROGRESS',
                    question_order_seed INTEGER DEFAULT 0,
                    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
                    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 6. Student Answers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS student_answers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    attempt_id INTEGER NOT NULL,
                    question_id INTEGER NOT NULL,
                    selected_option_id INTEGER,
                    is_marked_for_review INTEGER DEFAULT 0,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id) ON DELETE CASCADE,
                    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
                    FOREIGN KEY (selected_option_id) REFERENCES answer_options(id) ON DELETE SET NULL,
                    UNIQUE(attempt_id, question_id)
                );
            """);
        }
    }

    private static void seedDemoData(Connection conn) throws SQLException {
        // Ensure Demo Creator exists
        int creatorId = 0;
        String findCreatorSql = "SELECT id FROM users WHERE email = 'creator@testforge.com';";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(findCreatorSql)) {
            if (rs.next()) {
                creatorId = rs.getInt("id");
            }
        }

        if (creatorId == 0) {
            String insertCreatorSql = """
                INSERT INTO users (username, full_name, email, password_hash, role)
                VALUES (?, ?, ?, ?, 'CREATOR');
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(insertCreatorSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "prof_alan");
                pstmt.setString(2, "Dr. Alan Turing");
                pstmt.setString(3, "creator@testforge.com");
                pstmt.setString(4, PasswordUtil.hashPassword("password123"));
                pstmt.executeUpdate();
                try (ResultSet gk = pstmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        creatorId = gk.getInt(1);
                    }
                }
            }
        }

        // Ensure Demo Student exists
        String findStudentSql = "SELECT id FROM users WHERE email = 'student@testforge.com';";
        boolean studentExists = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(findStudentSql)) {
            studentExists = rs.next();
        }

        if (!studentExists) {
            String insertStudentSql = """
                INSERT INTO users (username, full_name, email, password_hash, role)
                VALUES (?, ?, ?, ?, 'STUDENT');
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(insertStudentSql)) {
                pstmt.setString(1, "student_ada");
                pstmt.setString(2, "Ada Lovelace");
                pstmt.setString(3, "student@testforge.com");
                pstmt.setString(4, PasswordUtil.hashPassword("password123"));
                pstmt.executeUpdate();
            }
        }

        // Ensure Pre-Seeded Sample Exam JAVA-101 exists and is published
        String findExamSql = "SELECT id, is_published FROM exams WHERE UPPER(access_code) = 'JAVA-101';";
        boolean examExists = false;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(findExamSql)) {
            if (rs.next()) {
                examExists = true;
                // If it was toggled to unpublished during earlier tests, reset to published for smooth viva demo
                try (Statement updateStmt = conn.createStatement()) {
                    updateStmt.execute("UPDATE exams SET is_published = 1, release_results = 1 WHERE UPPER(access_code) = 'JAVA-101';");
                }
            }
        }

        if (!examExists && creatorId > 0) {
            System.out.println("Seeding default demo exam JAVA-101 with sample questions...");
            int examId;
            String insertExamSql = """
                INSERT INTO exams (creator_id, title, description, access_code, duration_minutes, pass_percentage, is_published, release_results)
                VALUES (?, ?, ?, ?, ?, ?, 1, 1);
            """;
            try (PreparedStatement pstmt = conn.prepareStatement(insertExamSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, creatorId);
                pstmt.setString(2, "Java Core & OOP Fundamentals");
                pstmt.setString(3, "A comprehensive benchmark on Java 21 features, OOP design principles, and memory management.");
                pstmt.setString(4, "JAVA-101");
                pstmt.setInt(5, 15);
                pstmt.setDouble(6, 50.0);
                pstmt.executeUpdate();
                try (ResultSet gk = pstmt.getGeneratedKeys()) {
                    if (gk.next()) {
                        examId = gk.getInt(1);
                    } else {
                        return;
                    }
                }
            }

            // Add 5 rich questions with 4 options each
            addSeedQuestion(conn, examId,
                    "Which feature introduced in modern Java allows writing records with immutable data without boilerplate getters and constructors?",
                    2, "Records were introduced to simplify immutable data carrier classes.",
                    new String[]{"Java Records", "Java JavaBeans", "Java Enums", "Java Interfaces"}, 0);

            addSeedQuestion(conn, examId,
                    "What is the time complexity of searching an element in a balanced Binary Search Tree (BST) with N nodes?",
                    1, "In a balanced BST, each comparison halves the search space, giving O(log N) time.",
                    new String[]{"O(N)", "O(log N)", "O(N log N)", "O(1)"}, 1);

            addSeedQuestion(conn, examId,
                    "In Object-Oriented Programming, which principle states that child classes should be replaceable by their base class without altering program correctness?",
                    2, "Liskov Substitution Principle (LSP) is the 'L' in SOLID principles.",
                    new String[]{"Single Responsibility Principle", "Open/Closed Principle", "Liskov Substitution Principle", "Interface Segregation"}, 2);

            addSeedQuestion(conn, examId,
                    "Which Java Swing class should be used to schedule periodic GUI updates on the Event Dispatch Thread (EDT) without freezing the UI?",
                    1, "javax.swing.Timer fires ActionEvents on the EDT, making it safe for UI updates.",
                    new String[]{"java.lang.Thread.sleep()", "javax.swing.Timer", "java.util.TimerTask", "java.util.concurrent.CountDownLatch"}, 1);

            addSeedQuestion(conn, examId,
                    "What is the effect of using PreparedStatements instead of Statement in JDBC?",
                    2, "PreparedStatements pre-compile SQL queries and use parameter binding, preventing SQL injection.",
                    new String[]{"It increases memory consumption without benefit", "It prevents SQL Injection and improves performance via pre-compilation", "It disables foreign key checks automatically", "It forces transactions to commit after each row"}, 1);
        }
    }

    private static void addSeedQuestion(Connection conn, int examId, String questionText, int marks,
                                        String explanation, String[] options, int correctIndex) throws SQLException {
        int qId;
        String qSql = "INSERT INTO questions (exam_id, question_text, marks, explanation) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(qSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, examId);
            pstmt.setString(2, questionText);
            pstmt.setInt(3, marks);
            pstmt.setString(4, explanation);
            pstmt.executeUpdate();
            try (ResultSet gk = pstmt.getGeneratedKeys()) {
                if (gk.next()) {
                    qId = gk.getInt(1);
                } else {
                    return;
                }
            }
        }

        String optSql = "INSERT INTO answer_options (question_id, option_text, is_correct, option_order) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(optSql)) {
            for (int i = 0; i < options.length; i++) {
                pstmt.setInt(1, qId);
                pstmt.setString(2, options[i]);
                pstmt.setInt(3, (i == correctIndex) ? 1 : 0);
                pstmt.setInt(4, i + 1);
                pstmt.executeUpdate();
            }
        }
    }
}
