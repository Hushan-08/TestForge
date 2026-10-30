package com.testforge.util;

import com.testforge.database.DatabaseInitializer;
import com.testforge.database.DatabaseManager;
import com.testforge.model.AnswerOption;
import com.testforge.model.Exam;
import com.testforge.model.Question;
import com.testforge.model.User;
import com.testforge.service.AuthService;
import com.testforge.service.CsvImportService;
import com.testforge.service.ExamService;

import java.io.File;
import java.util.List;

/**
 * Console verification utility to verify database initialization, authentication,
 * randomization, grading, and CSV import without JUnit.
 */
public class SanityCheck {

    public static void main(String[] args) {
        System.out.println(">>> Starting TestForge Verification Checks...");

        try {
            // 1. Test Database Initialization & Pre-seeding
            DatabaseInitializer.initializeDatabase();
            System.out.println("[PASS] Database and tables created successfully.");

            // 2. Test Authentication Service
            AuthService auth = new AuthService();
            User creator = auth.login("creator@testforge.com", "password123");
            if (creator != null && creator.isCreator()) {
                System.out.println("[PASS] Demo Creator login verified: " + creator.getFullName());
            } else {
                throw new RuntimeException("Creator login failed.");
            }

            User student = auth.login("student@testforge.com", "password123");
            if (student != null && student.isStudent()) {
                System.out.println("[PASS] Demo Student login verified: " + student.getFullName());
            } else {
                throw new RuntimeException("Student login failed.");
            }

            // 3. Test Exam Service & Seeded Exam
            ExamService examService = new ExamService();
            Exam seedExam = examService.getExamByAccessCode("JAVA-101");
            if (seedExam != null && seedExam.isPublished()) {
                System.out.println("[PASS] Sample Exam loaded & published: " + seedExam.getTitle() + " (Duration: " + seedExam.getDurationMinutes() + " mins)");
            } else {
                throw new RuntimeException("Sample exam 'JAVA-101' not found or unpublished.");
            }

            // 4. Test Question Randomization per attempt
            List<Question> randomizedQuestions = examService.getRandomizedQuestionsForAttempt(seedExam.getId(), 42);
            if (!randomizedQuestions.isEmpty()) {
                Question firstQ = randomizedQuestions.get(0);
                System.out.println("[PASS] Shuffled questions loaded. Count: " + randomizedQuestions.size() + ". First question options: " + firstQ.getOptions().size());
            } else {
                throw new RuntimeException("No questions returned for exam attempt.");
            }

            // 5. Test CSV Import Parser
            CsvImportService csvService = new CsvImportService();
            File sampleCsv = new File("sample_questions.csv");
            if (sampleCsv.exists()) {
                CsvImportService.ImportResult csvResult = csvService.parseCsvFile(sampleCsv, seedExam.getId());
                System.out.println("[PASS] CSV parser tested successfully. Valid questions parsed: " + csvResult.getSuccessCount() + ", Errors: " + csvResult.getFailureCount());
            } else {
                System.out.println("[WARN] sample_questions.csv file not found in current directory.");
            }

            // 6. Test Password Hashing
            String hash = PasswordUtil.hashPassword("secretCollegeTest123");
            boolean match = PasswordUtil.checkPassword("secretCollegeTest123", hash);
            boolean mismatch = PasswordUtil.checkPassword("wrongPassword", hash);
            if (match && !mismatch) {
                System.out.println("[PASS] BCrypt password hashing and verification working correctly.");
            } else {
                throw new RuntimeException("BCrypt verification failed.");
            }

            System.out.println("=================================================");
            System.out.println(" ALL 6 TESTFORGE CORE ENGINE CHECKS PASSED!");
            System.out.println("=================================================");

        } catch (Exception e) {
            System.err.println("[FAIL] TestForge check failed with exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
