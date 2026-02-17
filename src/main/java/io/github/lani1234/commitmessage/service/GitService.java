package io.github.lani1234.commitmessage.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GitService {

    public String getStagedDiff() throws IOException {
        log.info("Reading staged git diff...");

        // First check if we're in a git repository
        if (!isGitRepository()) {
            throw new IOException("Not a git repository. Run 'git init' to initialize one.");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "--staged");
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String diff = reader.lines().collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Git command failed with exit code: " + exitCode);
            }

            log.info("Successfully read {} characters of diff", diff.length());
            return diff;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Git process was interrupted", e);
        }
    }

    public boolean hasStagedChanges() throws IOException {
        String diff = getStagedDiff();
        return diff != null && !diff.trim().isEmpty();
    }

    private boolean isGitRepository() {
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "--git-dir");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}