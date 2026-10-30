package com.testforge.app;

import com.testforge.database.DatabaseInitializer;
import com.testforge.ui.MainFrame;
import com.testforge.ui.components.UITheme;

import javax.swing.SwingUtilities;

/**
 * Entry point for TestForge — Desktop Exam Application.
 * Tagline: “Forge your future.”
 *
 * Designed for beginner engineering students to understand, run, debug,
 * and demonstrate during a college viva.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  TestForge — \"Forge your future.\"");
        System.out.println("  A Modern Java Desktop Examination Application");
        System.out.println("=================================================");

        // 1. Initialize modern dark UI theme (FlatLaf Dark + Custom Navy styling)
        UITheme.setupTheme();

        // 2. Initialize SQLite Database tables and seed demo accounts
        DatabaseInitializer.initializeDatabase();

        // 3. Launch graphical user interface safely on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                System.out.println("TestForge application launched successfully.");
            } catch (Exception e) {
                System.err.println("Fatal error starting TestForge UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
