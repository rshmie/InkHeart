package io.inkHeart.cli.dto;

import java.time.LocalDateTime;

/**
 * DTO which handles the viewing summary of journal entry in
 * decrypted format
 */
public record DecryptedJournalEntryResponse(Long id, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
