package io.inkHeart.cli.dto;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateJournalEntryRequest(
        UUID entryUUID,
        EncryptedPayload encryptedTitle,
        EncryptedPayload encryptedContent,
        List<EncryptedPayload> encryptedTags,
        EncryptedPayload encryptedMood,
        @FutureOrPresent LocalDateTime visibleAfter,
        LocalDateTime expiresAt
) {

}
