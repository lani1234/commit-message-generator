package io.github.lani1234.commitmessage.cli;

import io.github.lani1234.commitmessage.model.CommitMessage;
import io.github.lani1234.commitmessage.service.ClaudeService;
import io.github.lani1234.commitmessage.service.GitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommitMessageCommand implements CommandLineRunner {

    private final GitService gitService;
    private final ClaudeService claudeService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Commit Message Generator ===\n");

        // Check for staged changes
        if (!gitService.hasStagedChanges()) {
            System.out.println("⚠️  No staged changes found.");
            System.out.println("\nTo stage changes, run:");
            System.out.println("  git add <files>");
            return;
        }

        // Get the diff
        String diff = gitService.getStagedDiff();

        // Generate commit message
        System.out.println("🤖 Generating commit message...\n");
        CommitMessage message = claudeService.generateCommitMessage(diff);

        // Display result
        System.out.println("📝 Generated Commit Message:");
        System.out.println("─".repeat(50));
        System.out.println(message.getFullMessage());
        System.out.println("─".repeat(50));
        System.out.println("\n✅ Copy the message above and use it for your commit!");
    }
}
