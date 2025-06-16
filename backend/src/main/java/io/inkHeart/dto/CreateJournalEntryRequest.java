package io.inkHeart.dto;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  Used for creating a new Journal Entry
*/
public record CreateJournalEntryRequest(
        EncryptedPayload encryptedTitle,
        EncryptedPayload encryptedContent,
        List<EncryptedPayload> encryptedTags,
        EncryptedPayload encryptedMood,
        @FutureOrPresent LocalDateTime visibleAfter,
        LocalDateTime expiresAt
) {

}
