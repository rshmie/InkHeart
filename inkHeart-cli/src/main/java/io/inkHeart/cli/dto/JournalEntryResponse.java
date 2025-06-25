package io.inkHeart.cli.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record JournalEntryResponse(
        Long id,
        UUID entryUUID,
        EncryptedPayload encryptedTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}