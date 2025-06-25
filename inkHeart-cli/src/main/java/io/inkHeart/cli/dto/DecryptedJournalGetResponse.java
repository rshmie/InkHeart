package io.inkHeart.cli.dto;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO which handles the viewing complete journal entry in
 * decrypted format
 */
public record DecryptedJournalGetResponse (
    Long id,
    UUID entryUUID,
    String decryptedTitle,
    String decryptedContent,
    String decryptedMood,
    List<String> decryptedTags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    @FutureOrPresent LocalDateTime visibleAfter,
    LocalDateTime expiresAt
) {

}
