package io.github.lani1234.commitmessage.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lani1234.commitmessage.config.ClaudeConfig;
import io.github.lani1234.commitmessage.model.CommitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeService {

    private final ClaudeConfig config;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    public CommitMessage generateCommitMessage(String diff) throws IOException {
        log.info("Generating commit message via Claude API...");

        if (diff == null || diff.trim().isEmpty()) {
            throw new IllegalArgumentException("Diff cannot be empty");
        }

        String prompt = buildPrompt(diff);
        String response = callClaudeApi(prompt);

        log.info("Successfully generated commit message");
        return parseCommitMessage(response);
    }

    private String buildPrompt(String diff) {
        return """
                Analyze this git diff and generate a professional commit message.
                
                Format requirements:
                - First line: brief summary (50 characters or less)
                - Blank line
                - Bullet points explaining what changed and why (be specific)
                - Use conventional commit format if applicable (feat:, fix:, refactor:, etc.)
                
                Git diff:
                %s
                """.formatted(diff);
    }

    private String callClaudeApi(String prompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("max_tokens", config.getMaxTokens());
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(config.getUrl())
                .addHeader("x-api-key", config.getKey())
                .addHeader("anthropic-version", config.getVersion())
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                throw new IOException("API call failed: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            return extractTextFromResponse(responseBody);
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        JsonObject obj = gson.fromJson(jsonResponse, JsonObject.class);
        JsonArray content = obj.getAsJsonArray("content");
        return content.get(0).getAsJsonObject().get("text").getAsString();
    }

    private CommitMessage parseCommitMessage(String message) {
        String[] lines = message.split("\n", 2);
        String summary = lines[0].trim();
        String fullMessage = message.trim();

        return new CommitMessage(summary, fullMessage);
    }
}
