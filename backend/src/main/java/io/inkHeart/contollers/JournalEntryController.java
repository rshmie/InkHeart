package io.inkHeart.contollers;

import io.inkHeart.dto.CreateJournalEntryRequest;
import io.inkHeart.dto.JournalEntryResponse;
import io.inkHeart.entity.CustomUserDetails;
import io.inkHeart.entity.JournalEntry;
import io.inkHeart.entity.User;
import io.inkHeart.repository.JournalEntryRepository;
import io.inkHeart.service.JournalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
@Validated
public class JournalEntryController {
    private final JournalService journalService;
    public JournalEntryController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping
    public JournalEntryResponse create(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateJournalEntryRequest request) {
        System.out.println("Saving journal for create: " + userDetails.getUsername());
        return journalService.createEntry(userDetails.getUser(), request);
    }

    @GetMapping
    public List<JournalEntryResponse> getJournalEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        System.out.println("Saving journal for: " + userDetails.getUsername());
        return journalService.getJournalEntries(userDetails.getUsername());
    }
    @GetMapping("/entries")
    public List<JournalEntryResponse> getEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return journalService.getVisibleEntries(userDetails.getUser());
    }

    /*
    deleteEntryById()

    updateEntry()

    getEntryById()

    searchEntriesByTag()

    getTimeLockedOrExpiredEntries()
     */
}
