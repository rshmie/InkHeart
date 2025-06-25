package io.inkHeart.cli.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UpdateJournalEntryRequest(
        UUID entryUUID,
        EncryptedPayload encryptedTitle,
        EncryptedPayload encryptedContent,
        EncryptedPayload encryptedMood,
        List<EncryptedPayload> encryptedTags,
        LocalDateTime visibleAfter,
        LocalDateTime expiresAt
) {}
