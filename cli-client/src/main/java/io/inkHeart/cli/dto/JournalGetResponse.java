package io.inkHeart.cli.dto;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;
import java.util.List;

public record JournalGetResponse(
        Long id,
        EncryptedPayload encryptedTitle,
        EncryptedPayload encryptedContent,
        EncryptedPayload encryptedMood,
        List<EncryptedPayload> encryptedTags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        @FutureOrPresent LocalDateTime visibleAfter,
        LocalDateTime expiresAt
) {

}
