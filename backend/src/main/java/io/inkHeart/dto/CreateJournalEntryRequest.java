package io.inkHeart.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record CreateJournalEntryRequest(
        @NotBlank String encryptedTitle,
        @NotBlank(message = "Content cannot be empty") String encryptedContent,
        @FutureOrPresent LocalDateTime visibleAfter,
        LocalDateTime expiresAt, // use of optional<>?
        List<String> encryptedTags, // use of  Optional<>
        String encryptedMood
) {

}
