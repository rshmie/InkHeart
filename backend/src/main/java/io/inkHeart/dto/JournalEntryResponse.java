package io.inkHeart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record JournalEntryResponse (Long id, @NotBlank String encryptedTitle, @NotBlank(message = "Content cannot be empty") String encryptedContent,
                                    String encryptedMood, List<String> encryptedTags,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
}
