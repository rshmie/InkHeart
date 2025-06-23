package io.inkHeart.cli.dto;

import java.util.List;

public record CreateEntryPromptResult(String title, String content, String mood, List<String> tags, String visibleAfterStr, String expiresAtStr) {
}
