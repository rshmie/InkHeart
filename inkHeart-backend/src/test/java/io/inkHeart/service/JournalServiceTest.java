package io.inkHeart.service;

import io.inkHeart.dto.CreateJournalEntryRequest;
import io.inkHeart.dto.EncryptedPayload;
import io.inkHeart.dto.JournalEntryResponse;
import io.inkHeart.dto.UpdateJournalEntryRequest;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;
import io.inkHeart.exception.NoJournalEntryFoundException;
import io.inkHeart.repository.JournalEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceTest {
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @InjectMocks
    private JournalService journalService;

    @Test
    public void testCreateJournalEntry() {
        User mockUser = new User();
        CreateJournalEntryRequest createJournalEntryRequest = mockCreateJournalEntry();
        when(journalEntryRepository.save(any())).thenAnswer(x -> x.getArgument(0));

        JournalEntry entry = journalService.createEntry(mockUser, createJournalEntryRequest);

        assertEquals(mockUser, entry.getUser());
        assertEquals("encrypted-title", entry.getEncryptedTitle().cipherText());
        assertEquals("title-iv", entry.getEncryptedTitle().iv());
        assertEquals("encrypted-content", entry.getEncryptedContent().cipherText());
        assertEquals(1, entry.getEncryptedTags().size());

        verify(journalEntryRepository, times(1)).save(any());
    }

    @Test
    public void testGetAllEntries() {
        JournalEntry mockEntry = new JournalEntry();
        createMockJournalEntry(mockEntry);

        when(journalEntryRepository.findAllByUserEmail(any())).thenReturn(List.of(mockEntry));

        var entry = journalService.getAllJournalEntries("test@example.com");

        assertEquals(1, entry.size());
        assertEquals("encrypted-title", entry.get(0).encryptedTitle().cipherText());
        assertEquals("encrypted-content", entry.get(0).encryptedContent().cipherText());
    }

    @Test
    public void testGetJournalEntryById() {
        User mockUser = new User();
        JournalEntry mockEntry = new JournalEntry();
        createMockJournalEntry(mockEntry);

        when(journalEntryRepository.findByIdAndUser(1L, mockUser)).thenReturn(Optional.of(mockEntry));

        var entry = journalService.getCompleteJournalEntryById(mockUser, 1L);

        assertEquals(1L, entry.id());
        assertEquals("encrypted-title", entry.encryptedTitle().cipherText());
        assertEquals("encrypted-content", entry.encryptedContent().cipherText());
    }

    @Test
    public void testDeleteJournalEntry() {
        User mockUser = new User();
        JournalEntry mockEntry = new JournalEntry();
        createMockJournalEntry(mockEntry);

        when(journalEntryRepository.findByIdAndUser(1L, mockUser))
                .thenReturn(Optional.of(mockEntry))
                .thenReturn(Optional.empty());

        doNothing().when(journalEntryRepository).delete(mockEntry);
        JournalEntryResponse deleteResponse = journalService.deleteJournalEntryById(mockUser, 1L);
        assertNotNull(deleteResponse);
        assertEquals(1L, deleteResponse.id());
        assertEquals("encrypted-title", deleteResponse.encryptedTitle().cipherText());

        verify(journalEntryRepository).delete(mockEntry);

        //After delete
        assertThrows(NoJournalEntryFoundException.class, () -> {
            journalService.getCompleteJournalEntryById(mockUser, 1L);
        });
    }

    @Test
    public void testUpdateJournalEntry() {
        User mockUser = new User();
        JournalEntry mockEntry = new JournalEntry();
        createMockJournalEntry(mockEntry);
        mockEntry.setId(2L);

        when(journalEntryRepository.findByIdAndUser(2L, mockUser))
                .thenReturn(Optional.of(mockEntry));

        UpdateJournalEntryRequest mockUpdateEntry = new UpdateJournalEntryRequest(UUID.randomUUID(),
                new EncryptedPayload("updated-encrypted-title", "title-iv-updated"),
                new EncryptedPayload("updated-encrypted-content", "content-iv-updated"),
                null,
                List.of(new EncryptedPayload("updated-encrypted-tag", "updated-iv")),
                null,
                null
        );
        JournalEntryResponse updatedResponse = journalService.updateJournalEntryById(mockUser, 2L, mockUpdateEntry);
        assertEquals(2L, updatedResponse.id());
        assertEquals("updated-encrypted-title", updatedResponse.encryptedTitle().cipherText());
        assertEquals("title-iv-updated", updatedResponse.encryptedTitle().iv());
    }

    private static void createMockJournalEntry(JournalEntry mockEntry) {
        mockEntry.setId(1L);
        mockEntry.setEncryptedTitle(new EncryptedPayload("encrypted-title", "title-iv"));
        mockEntry.setEncryptedContent(new EncryptedPayload("encrypted-content", "content-iv"));
        mockEntry.setEncryptedMood(new EncryptedPayload("mood", "iv"));
        mockEntry.setEncryptedTags(List.of(new EncryptedPayload("encrypted-tag", "tag-iv")));
        mockEntry.setCreatedAt(LocalDateTime.now());
        mockEntry.setUpdatedAt(LocalDateTime.now());
    }

    private static CreateJournalEntryRequest mockCreateJournalEntry() {
        return new CreateJournalEntryRequest( UUID.randomUUID(),
                new EncryptedPayload("encrypted-title", "title-iv"),
                new EncryptedPayload("encrypted-content", "content-iv"),
                List.of(new EncryptedPayload("encrypted-tag", "test")),
                new EncryptedPayload("encrypted-mood", ""),
                LocalDateTime.of(2024, 1, 1, 12, 0),
                LocalDateTime.of(2030, 1, 1, 12, 0)
        );
    }
}