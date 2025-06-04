package io.inkHeart.service;

import io.inkHeart.dto.CreateJournalEntryRequest;
import io.inkHeart.dto.JournalEntryResponse;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;
import io.inkHeart.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalService {
    private final JournalEntryRepository journalEntryRepository;
    public JournalService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public JournalEntryResponse createEntry(User user, CreateJournalEntryRequest request) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setUser(user);
        journalEntry.setEncryptedTitle(request.encryptedTitle());
        journalEntry.setEncryptedContent(request.encryptedContent());
        journalEntry.setEncryptedMood(request.encryptedMood());
        journalEntry.setEncryptedTags(request.encryptedTags());
        journalEntry.setVisibleAfter(request.visibleAfter());
        journalEntry.setExpiresAt(request.expiresAt());
        journalEntry.setCreatedAt(LocalDateTime.now());
        journalEntry.setUpdatedAt(LocalDateTime.now());

        journalEntryRepository.save(journalEntry);

        return mapToResponse(journalEntry);
    }

    public List<JournalEntryResponse> getJournalEntries(String userName) {
        return journalEntryRepository.findAllByUserEmail(userName)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    public List<JournalEntryResponse> getVisibleEntries(User user) {
        var now = LocalDateTime.now();
        return journalEntryRepository.findByUserAndCreatedAtBeforeAndVisibleAfterBefore(user, now, now)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private JournalEntryResponse mapToResponse(JournalEntry entry) {
        return new JournalEntryResponse(entry.getId(), entry.getEncryptedTitle(),
                entry.getEncryptedContent(), entry.getEncryptedMood(),
                entry.getEncryptedTags(), entry.getCreatedAt(), entry.getUpdatedAt());
    }
}
