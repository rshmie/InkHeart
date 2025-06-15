package io.inkHeart.service;

import io.inkHeart.dto.*;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;
import io.inkHeart.exception.NoJournalEntryFoundException;
import io.inkHeart.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JournalService {
    private final JournalEntryRepository journalEntryRepository;
    public JournalService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public JournalEntry createEntry(User user, CreateJournalEntryRequest request) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setUser(user);
        journalEntry.setEncryptedTitle(new EncryptedPayload(request.encryptedTitle().cipherText(), request.encryptedTitle().iv()));
        journalEntry.setEncryptedContent(new EncryptedPayload(request.encryptedContent().cipherText(), request.encryptedContent().iv()));
        journalEntry.setEncryptedMood(checkNull(request.encryptedMood()));
        journalEntry.setEncryptedTags(getEncryptedTagList(request.encryptedTags()));
        journalEntry.setVisibleAfter(request.visibleAfter());
        journalEntry.setExpiresAt(request.expiresAt());
        journalEntry.setCreatedAt(LocalDateTime.now());
        journalEntry.setUpdatedAt(LocalDateTime.now());

        journalEntryRepository.save(journalEntry);
        return journalEntry;
    }

    private List<EncryptedPayload> getEncryptedTagList(List<EncryptedPayload> tags) {
        List<EncryptedPayload> tagList = new ArrayList<>();
        for (EncryptedPayload tag : tags) {
            tagList.add(checkNull(tag));
        }
        return tagList;
    }
//    public List<JournalEntryResponse> getVisibleEntries(User user) {
//        var now = LocalDateTime.now();
//        return journalEntryRepository.findByUserAndCreatedAtBeforeAndVisibleAfterBefore(user, now, now)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }

    public List<JournalGetResponse> getAllJournalEntries(String userName) {
        return journalEntryRepository.findAllByUserEmail(userName)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<JournalEntryResponse> get10RecentEntriesForUser(User user) {
        return journalEntryRepository.findTop10ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(entry -> new JournalEntryResponse(entry.id(), entry.encryptedTitle(), entry.createdAt(), entry.updatedAt()))
                .collect(Collectors.toList());
    }

    public JournalGetResponse getCompleteJournalEntryById(User user, Long journalEntryId) {
        return mapToResponse(journalEntryRepository.findByIdAndUser(journalEntryId, user)
                .orElseThrow(() -> new NoJournalEntryFoundException(journalEntryId)));
    }

    public JournalEntryResponse deleteJournalEntryById(User user, Long journalEntryId) {
        JournalEntry journalEntry = journalEntryRepository.findByIdAndUser(journalEntryId, user)
                .orElseThrow(() -> new NoJournalEntryFoundException(journalEntryId));
        journalEntryRepository.delete(journalEntry);
        return new JournalEntryResponse(journalEntry.getId(), journalEntry.getEncryptedTitle(), journalEntry.getCreatedAt(), journalEntry.getUpdatedAt());
    }

//    public List<JournalEntryResponse> getVisibleEntries(User user) {
//        var now = LocalDateTime.now();
//        return journalEntryRepository.findByUserAndCreatedAtBeforeAndVisibleAfterBefore(user, now, now)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }

    private JournalGetResponse mapToResponse(JournalEntry entry) {
        return new JournalGetResponse(entry.getId(),
                new EncryptedPayload(entry.getEncryptedTitle().cipherText(), entry.getEncryptedTitle().iv()),
                new EncryptedPayload(entry.getEncryptedContent().cipherText(), entry.getEncryptedContent().iv()),
                checkNull(entry.getEncryptedMood()),
                getEncryptedTagList(entry.getEncryptedTags()), entry.getCreatedAt(), entry.getUpdatedAt(),
                entry.getVisibleAfter(), entry.getExpiresAt());
    }

    private EncryptedPayload checkNull(EncryptedPayload encryptedPayload) {
        return encryptedPayload == null ? null : new EncryptedPayload(encryptedPayload.cipherText(), encryptedPayload.iv());
    }
}
