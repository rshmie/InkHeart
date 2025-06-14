package io.inkHeart.dto;

import java.time.LocalDateTime;

public record JournalEntryResponse(
        Long id,
        EncryptedPayload encryptedTitle,
        LocalDateTime createdAt, // Essential: The client needs the server's official timestamp.
        LocalDateTime updatedAt  // Essential for the same reason.
) {}