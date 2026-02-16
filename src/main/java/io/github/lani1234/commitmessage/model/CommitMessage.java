package io.github.lani1234.commitmessage.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitMessage {
    private String summary;
    private String fullMessage;

    @Override
    public String toString() {
        return fullMessage;
    }
}
