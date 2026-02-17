package io.github.lani1234.commitmessage.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lani1234.commitmessage.config.ClaudeConfig;
import io.github.lani1234.commitmessage.config.PromptConfig;
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

    private final ClaudeConfig claudeConfig;
    private final PromptConfig promptConfig;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final Gson gson = new Gson();

    public CommitMessage generateCommitMessage(String diff) throws IOException {
        log.info("Generating commit message via Claude API...");

        if (diff == null || diff.trim().isEmpty()) {
            throw new IllegalArgumentException("Diff cannot be empty");
        }

        validateApiKey();

        String prompt = buildPrompt(diff);
        String response = callClaudeApi(prompt);

        log.info("Successfully generated commit message");
        return parseCommitMessage(response);
    }

    private void validateApiKey() {
        if (claudeConfig.getKey() == null || claudeConfig.getKey().trim().isEmpty()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY is not set. Please set it as an environment variable:\n" +
                            "export ANTHROPIC_API_KEY=\"your-key-here\""
            );
        }
    }

    private String buildPrompt(String diff) {
        return promptConfig.getTemplate().replace("{diff}", diff);
    }

    private String callClaudeApi(String prompt) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", claudeConfig.getModel());
        requestBody.put("max_tokens", claudeConfig.getMaxTokens());
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        RequestBody body = RequestBody.create(
                gson.toJson(requestBody),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(claudeConfig.getUrl())
                .addHeader("x-api-key", claudeConfig.getKey())
                .addHeader("anthropic-version", claudeConfig.getVersion())
                .addHeader("content-type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("API call failed: {} - {}", response.code(), errorBody);

                if (response.code() == 401) {
                    throw new IOException("API authentication failed. Please check your ANTHROPIC_API_KEY.");
                } else if (response.code() == 429) {
                    throw new IOException("Rate limit exceeded. Please try again in a moment.");
                } else {
                    throw new IOException("API call failed with status " + response.code() + ": " + errorBody);
                }
            }

            if (response.body() == null) {
                throw new IOException("API returned empty response");
            }

            String responseBody = response.body().string();
            return extractTextFromResponse(responseBody);
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonObject obj = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray content = obj.getAsJsonArray("content");

            if (content == null || content.isEmpty()) {
                throw new IOException("API response missing content");
            }

            return content.get(0).getAsJsonObject().get("text").getAsString();
        } catch (Exception e) {
            log.error("Failed to parse API response: {}", jsonResponse);
            throw new RuntimeException("Failed to parse API response", e);
        }
    }

    private CommitMessage parseCommitMessage(String message) {
        String[] lines = message.split("\n", 2);
        String summary = lines[0].trim();
        String fullMessage = message.trim();

        return new CommitMessage(summary, fullMessage);
    }
}