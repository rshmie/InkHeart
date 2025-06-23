package io.inkHeart.contollers;

import io.inkHeart.dto.CreateJournalEntryRequest;
import io.inkHeart.dto.EncryptedPayload;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;
import io.inkHeart.repository.JournalEntryRepository;
import io.inkHeart.service.JournalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryControllerTest {
    @Mock
    private JournalEntryRepository journalEntryRepository;
    @InjectMocks
    private JournalService journalService;

    @Test
    public void testCreateEntryShouldSaveEncryptedJournalData() {
        User mockUser = new User();
        CreateJournalEntryRequest createJournalEntryRequest = new CreateJournalEntryRequest(
                new EncryptedPayload("encrypted-title", "title-iv"),
                new EncryptedPayload("encrypted-content", "content-iv"),
                List.of(new EncryptedPayload("encrypted-tag", "test")),
                new EncryptedPayload("encrypted-mood", ""),
                LocalDateTime.of(2024, 1, 1, 12, 0),
                LocalDateTime.of(2025, 1, 1, 12, 0)
        );
        when(journalEntryRepository.save(any())).thenAnswer(x -> x.getArgument(0));

        JournalEntry entry = journalService.createEntry(mockUser, createJournalEntryRequest);

        assertEquals(mockUser, entry.getUser());
        assertEquals("encrypted-title", entry.getEncryptedTitle().cipherText());
        assertEquals("title-iv", entry.getEncryptedTitle().iv());
        assertEquals("encrypted-content", entry.getEncryptedContent().cipherText());
        assertEquals(1, entry.getEncryptedTags().size());

        verify(journalEntryRepository, times(1)).save(any());
    }

    

}