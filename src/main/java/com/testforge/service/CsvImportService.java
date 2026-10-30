package com.testforge.service;

import com.testforge.model.AnswerOption;
import com.testforge.model.Question;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing multiple-choice questions from standard CSV files.
 * Format: Question,Option A,Option B,Option C,Option D,Correct Option,Marks,Explanation
 */
public class CsvImportService {

    public static class ImportResult {
        private final List<Question> validQuestions = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();
        private int totalProcessed = 0;

        public List<Question> getValidQuestions() {
            return validQuestions;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        public int getTotalProcessed() {
            return totalProcessed;
        }

        public int getSuccessCount() {
            return validQuestions.size();
        }

        public int getFailureCount() {
            return errorMessages.size();
        }
    }

    /**
     * Parses a CSV file and converts valid rows into Question objects.
     */
    public ImportResult parseCsvFile(File file, int examId) throws IOException {
        ImportResult result = new ImportResult();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                // Check and skip header row if present
                if (isFirstLine) {
                    isFirstLine = false;
                    if (isHeaderRow(line)) {
                        continue;
                    }
                }

                result.totalProcessed++;
                try {
                    List<String> tokens = parseCsvLine(line);
                    if (tokens.size() < 6) {
                        result.errorMessages.add("Line " + lineNumber + ": Insufficient columns (expected at least 6 columns).");
                        continue;
                    }

                    String questionText = tokens.get(0).trim();
                    String optA = tokens.get(1).trim();
                    String optB = tokens.get(2).trim();
                    String optC = tokens.get(3).trim();
                    String optD = tokens.get(4).trim();
                    String correctStr = tokens.get(5).trim().toUpperCase();

                    int marks = 1;
                    if (tokens.size() >= 7 && !tokens.get(6).trim().isEmpty()) {
                        try {
                            marks = Integer.parseInt(tokens.get(6).trim());
                            if (marks <= 0) marks = 1;
                        } catch (NumberFormatException e) {
                            result.errorMessages.add("Line " + lineNumber + ": Invalid marks '" + tokens.get(6) + "'. Defaulting to 1.");
                        }
                    }

                    String explanation = (tokens.size() >= 8) ? tokens.get(7).trim() : "";

                    // Validation checks
                    if (questionText.isEmpty()) {
                        result.errorMessages.add("Line " + lineNumber + ": Empty question text.");
                        continue;
                    }
                    if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
                        result.errorMessages.add("Line " + lineNumber + ": All 4 options (A, B, C, D) must have text.");
                        continue;
                    }

                    int correctIndex = parseCorrectOptionIndex(correctStr);
                    if (correctIndex == -1) {
                        result.errorMessages.add("Line " + lineNumber + ": Invalid correct option identifier '" + correctStr + "'. Expected A, B, C, or D.");
                        continue;
                    }

                    Question q = new Question(examId, questionText, marks, explanation);
                    q.addOption(new AnswerOption(optA, correctIndex == 0, 1));
                    q.addOption(new AnswerOption(optB, correctIndex == 1, 2));
                    q.addOption(new AnswerOption(optC, correctIndex == 2, 3));
                    q.addOption(new AnswerOption(optD, correctIndex == 3, 4));

                    result.validQuestions.add(q);

                } catch (Exception e) {
                    result.errorMessages.add("Line " + lineNumber + ": Error parsing row - " + e.getMessage());
                }
            }
        }

        return result;
    }

    private boolean isHeaderRow(String line) {
        String lower = line.toLowerCase();
        return lower.contains("question") && (lower.contains("option") || lower.contains("correct"));
    }

    private int parseCorrectOptionIndex(String text) {
        String upper = text.toUpperCase();
        if (upper.equals("A") || upper.equals("OPTION A") || upper.equals("1")) return 0;
        if (upper.equals("B") || upper.equals("OPTION B") || upper.equals("2")) return 1;
        if (upper.equals("C") || upper.equals("OPTION C") || upper.equals("3")) return 2;
        if (upper.equals("D") || upper.equals("OPTION D") || upper.equals("4")) return 3;
        return -1;
    }

    /**
     * Standard RFC 4180 compliant CSV line tokenizer.
     * Supports quotes and commas within quotation marks.
     */
    public List<String> parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    // Escaped quote: "" -> "
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString().trim());
        return list;
    }
}
