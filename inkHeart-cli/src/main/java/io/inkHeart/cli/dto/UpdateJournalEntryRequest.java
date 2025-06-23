package io.inkHeart.cli.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UpdateJournalEntryRequest(
        EncryptedPayload encryptedTitle,
        EncryptedPayload encryptedContent,
        EncryptedPayload encryptedMood,
        List<EncryptedPayload> encryptedTags,
        LocalDateTime visibleAfter,
        LocalDateTime expiresAt
) {}
