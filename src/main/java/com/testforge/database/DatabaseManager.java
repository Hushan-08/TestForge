package com.testforge.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the SQLite database connection and file path.
 * Stored in a predictable user home application folder: ~/.testforge/testforge.db
 */
public class DatabaseManager {

    private static String databasePath;
    private static String jdbcUrl;

    static {
        // Resolve application data directory in the user's home
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".testforge");
        if (!appDir.exists()) {
            boolean created = appDir.mkdirs();
            if (created) {
                System.out.println("Created TestForge data directory: " + appDir.getAbsolutePath());
            }
        }
        File dbFile = new File(appDir, "testforge.db");
        databasePath = dbFile.getAbsolutePath();
        jdbcUrl = "jdbc:sqlite:" + databasePath;
    }

    /**
     * Gets a new JDBC connection to the SQLite database with Foreign Keys enabled.
     *
     * @return active Connection instance
     * @throws SQLException if a database error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the SQLite JDBC driver class
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found in classpath.", e);
        }

        Connection conn = DriverManager.getConnection(jdbcUrl);
        // Enable foreign key constraints in SQLite (disabled by default in SQLite)
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static String getDatabasePath() {
        return databasePath;
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }
}
