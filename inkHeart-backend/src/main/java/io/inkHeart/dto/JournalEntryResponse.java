package io.inkHeart.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Minimum journal entry information that is required to send to the client
 */
public record JournalEntryResponse(
        Long id,
        UUID entryUUID,
        EncryptedPayload encryptedTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}