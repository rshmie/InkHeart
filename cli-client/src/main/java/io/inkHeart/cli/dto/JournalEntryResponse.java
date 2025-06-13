package io.inkHeart.cli.dto;

import java.time.LocalDateTime;

public record JournalEntryResponse(
        Long id,
        EncryptedPayload encryptedTitle,

        // Optional: other small metadata the user might want to see confirmed.
        // String encryptedMood,
        // List<String> encryptedTags,

        LocalDateTime createdAt, // Essential: The client needs the server's official timestamp.
        LocalDateTime updatedAt  // Essential for the same reason.
) {}