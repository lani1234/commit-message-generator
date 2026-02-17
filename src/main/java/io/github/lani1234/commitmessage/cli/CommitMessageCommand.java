package io.github.lani1234.commitmessage.cli;

import io.github.lani1234.commitmessage.model.CommitMessage;
import io.github.lani1234.commitmessage.service.ClaudeService;
import io.github.lani1234.commitmessage.service.GitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommitMessageCommand implements CommandLineRunner {

    private final GitService gitService;
    private final ClaudeService claudeService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Commit Message Generator ===\n");

        try {
            // Check for staged changes
            if (!gitService.hasStagedChanges()) {
                System.out.println("No staged changes found.");
                System.out.println("\nTo stage changes, run:");
                System.out.println("  git add <files>");
                return;
            }

            // Get the diff
            String diff = gitService.getStagedDiff();

            // Check if diff is too large
            if (diff.length() > 50000) {
                System.out.println("Warning: Your diff is very large (" + diff.length() + " characters).");
                System.out.println("This may result in higher API costs or truncated output.\n");
            }

            // Generate commit message
            boolean keepTrying = true;
            while (keepTrying) {
                System.out.println("Generating commit message...\n");

                CommitMessage message = claudeService.generateCommitMessage(diff);

                // Display result
                System.out.println("Generated Commit Message:");
                System.out.println("─".repeat(60));
                System.out.println(cleanMessage(message.getFullMessage()));
                System.out.println("─".repeat(60));

                // Interactive mode
                System.out.print("\nUse this message? (y/n/r for regenerate): ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String response = reader.readLine().trim().toLowerCase();

                switch (response) {
                    case "y":
                    case "yes":
                        System.out.println("\n Great! Copy the message above and commit with:");
                        System.out.println("   git commit -m \"" + message.getSummary().replace("\"", "\\\"") + "\"");
                        keepTrying = false;
                        break;
                    case "r":
                    case "regenerate":
                        System.out.println("\n Regenerating...\n");
                        break;
                    case "n":
                    case "no":
                        System.out.println("\n No problem! Edit your changes and try again.");
                        keepTrying = false;
                        break;
                    default:
                        System.out.println("\n Message generated. Copy it when ready!");
                        keepTrying = false;
                }
            }

        } catch (Exception e) {
            log.error("Error generating commit message", e);
            System.err.println("\n Error: " + e.getMessage());
            System.err.println("\nPossible causes:");
            System.err.println("  - API key not set or invalid");
            System.err.println("  - Network connectivity issues");
            System.err.println("  - Git repository not initialized");
            System.err.println("\nFor more details, check the logs above.");
        }
    }

    /**
     * Clean the message by removing markdown code fences and extra whitespace
     */
    private String cleanMessage(String message) {
        if (message == null) {
            return "";
        }

        // Remove markdown code fences (```, ```markdown, etc.)
        String cleaned = message.replaceAll("```\\w*\\n?", "")
                .replaceAll("```", "");

        // Trim extra whitespace
        cleaned = cleaned.trim();

        return cleaned;
    }
}