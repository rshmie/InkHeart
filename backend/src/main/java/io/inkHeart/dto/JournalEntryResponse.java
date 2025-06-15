package io.inkHeart.dto;

import java.time.LocalDateTime;

/**
 * Minimum journal entry information that is required to send to the client
 */
public record JournalEntryResponse(
        Long id,
        EncryptedPayload encryptedTitle,
        LocalDateTime createdAt, // Essential: The client needs the server's official timestamp.
        LocalDateTime updatedAt  // Essential for the same reason.
) {}