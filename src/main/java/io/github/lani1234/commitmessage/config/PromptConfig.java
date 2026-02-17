package io.github.lani1234.commitmessage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "commit.prompt")
public class PromptConfig {

    private String template = """
            Analyze this git diff and generate a professional commit message.
            
            Format requirements:
            - First line: brief summary (50 characters or less)
            - Blank line
            - Bullet points explaining what changed and why (be specific)
            - Use conventional commit format if applicable (feat:, fix:, refactor:, etc.)
            - Do NOT wrap the output in markdown code fences or backticks
            
            Git diff:
            {diff}
            """;

    private String style = "conventional"; // conventional, emoji, detailed
}