package com.testforge.service;

import com.testforge.model.AnswerOption;
import com.testforge.model.Question;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for generating multiple-choice exam questions using the Groq AI API.
 * Uses Java 21's built-in HttpClient.
 * Never hardcodes API keys; checks environment variable GROQ_API_KEY or user-entered session key.
 */
public class GroqAiService {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    private final HttpClient httpClient;

    public GroqAiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Resolves the Groq API key: checks custom key passed in, otherwise checks GROQ_API_KEY env var.
     */
    public static String resolveApiKey(String userProvidedKey) {
        if (userProvidedKey != null && !userProvidedKey.trim().isEmpty()) {
            return userProvidedKey.trim();
        }
        String envKey = System.getenv("GROQ_API_KEY");
        if (envKey != null && !envKey.trim().isEmpty()) {
            return envKey.trim();
        }
        return null;
    }

    /**
     * Generates multiple-choice questions based on study notes.
     *
     * @param apiKey         Groq API key
     * @param notes          Study text or topic summary
     * @param count          Number of questions to generate (1-10)
     * @param difficulty     "Easy", "Medium", or "Hard"
     * @param examId         Target exam ID
     * @return List of generated Question objects (pending creator review)
     * @throws Exception with user-friendly error message on failure
     */
    public List<Question> generateQuestions(String apiKey, String notes, int count, String difficulty, int examId) throws Exception {
        String effectiveKey = resolveApiKey(apiKey);
        if (effectiveKey == null || effectiveKey.isEmpty()) {
            throw new IllegalArgumentException("Groq API key not provided. Set the GROQ_API_KEY environment variable or enter your key in the field.");
        }
        if (notes == null || notes.trim().isEmpty()) {
            throw new IllegalArgumentException("Please paste study notes or enter a topic description to generate questions from.");
        }

        int clampedCount = Math.clamp(count, 1, 10);

        String systemPrompt = """
            You are an expert academic examiner. You generate high-quality, multiple-choice questions (MCQs) for university exams.
            Each question must have exactly four distinct options (A, B, C, D), exactly one unambiguous correct option, and a brief explanation.
            You MUST respond ONLY with a raw JSON array of objects. Do not wrap in markdown quotes. Do not include introductory or concluding text.
            Schema:
            [
              {
                "question": "Question text here?",
                "optionA": "First option",
                "optionB": "Second option",
                "optionC": "Third option",
                "optionD": "Fourth option",
                "correctOption": "A",
                "marks": 1,
                "explanation": "Brief explanation of why this answer is correct."
              }
            ]
            """;

        String userPrompt = String.format(
                "Generate exactly %d multiple choice questions with difficulty level '%s' based on the following study material:\n\n%s",
                clampedCount, difficulty, notes.trim()
        );

        // Build Groq request payload
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", DEFAULT_MODEL);
        requestBody.put("temperature", 0.3);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", userPrompt));
        requestBody.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + effectiveKey)
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpConnectTimeoutException | java.net.ConnectException e) {
            throw new Exception("Connection to Groq API timed out. Please check your internet connection.");
        } catch (Exception e) {
            throw new Exception("Network error connecting to Groq API: " + e.getMessage(), e);
        }

        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 401) {
            throw new Exception("Invalid Groq API key (HTTP 401). Please check your key at https://console.groq.com/keys.");
        }
        if (statusCode == 429) {
            throw new Exception("Groq API rate limit reached (HTTP 429). Please wait a moment before trying again.");
        }
        if (statusCode >= 400) {
            throw new Exception("Groq API returned HTTP error " + statusCode + ": " + extractErrorMessage(responseBody));
        }

        return parseQuestionsFromJson(responseBody, examId);
    }

    private List<Question> parseQuestionsFromJson(String rawJson, int examId) throws Exception {
        try {
            JSONObject root = new JSONObject(rawJson);
            JSONArray choices = root.getJSONArray("choices");
            if (choices.isEmpty()) {
                throw new Exception("AI returned empty choices.");
            }

            String content = choices.getJSONObject(0).getJSONObject("message").getString("content").trim();

            // Strip possible markdown fences ```json ... ```
            if (content.startsWith("```")) {
                int firstNewline = content.indexOf('\n');
                int lastFence = content.lastIndexOf("```");
                if (firstNewline != -1 && lastFence > firstNewline) {
                    content = content.substring(firstNewline + 1, lastFence).trim();
                }
            }

            JSONArray questionsArray = new JSONArray(content);
            List<Question> result = new ArrayList<>();

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject item = questionsArray.getJSONObject(i);
                String questionText = item.getString("question").trim();
                String optA = item.optString("optionA", "Option A").trim();
                String optB = item.optString("optionB", "Option B").trim();
                String optC = item.optString("optionC", "Option C").trim();
                String optD = item.optString("optionD", "Option D").trim();
                String correct = item.optString("correctOption", "A").trim().toUpperCase();
                int marks = item.optInt("marks", 1);
                String explanation = item.optString("explanation", "").trim();

                int correctIdx = 0;
                if ("B".equals(correct)) correctIdx = 1;
                else if ("C".equals(correct)) correctIdx = 2;
                else if ("D".equals(correct)) correctIdx = 3;

                Question q = new Question(examId, questionText, marks, explanation);
                q.addOption(new AnswerOption(optA, correctIdx == 0, 1));
                q.addOption(new AnswerOption(optB, correctIdx == 1, 2));
                q.addOption(new AnswerOption(optC, correctIdx == 2, 3));
                q.addOption(new AnswerOption(optD, correctIdx == 3, 4));

                result.add(q);
            }

            return result;

        } catch (Exception e) {
            throw new Exception("Failed to parse AI response into questions. Response format was invalid: " + e.getMessage(), e);
        }
    }

    private String extractErrorMessage(String body) {
        try {
            JSONObject obj = new JSONObject(body);
            if (obj.has("error")) {
                JSONObject err = obj.getJSONObject("error");
                return err.optString("message", body);
            }
        } catch (Exception ignored) {}
        return body.length() > 150 ? body.substring(0, 150) + "..." : body;
    }
}
