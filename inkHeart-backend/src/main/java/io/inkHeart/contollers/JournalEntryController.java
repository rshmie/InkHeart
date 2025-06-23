package io.inkHeart.contollers;

import io.inkHeart.dto.*;
import io.inkHeart.entity.CustomUserDetails;
import io.inkHeart.service.JournalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/journal")
@Validated
public class JournalEntryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JournalEntryController.class);
    private final JournalService journalService;
    public JournalEntryController(JournalService journalService) {
        this.journalService = journalService;
    }

    @PostMapping("/create")
    public ResponseEntity<JournalEntryResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody CreateJournalEntryRequest request) {
        var savedEntry = journalService.createEntry(userDetails.getUser(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedEntry.getId())
                .toUri();

        // Create a DTO to send back to the client, excluding sensitive info
        JournalEntryResponse response = new JournalEntryResponse(
                savedEntry.getId(),
                new EncryptedPayload(savedEntry.getEncryptedTitle().cipherText(), request.encryptedTitle().iv()),
                savedEntry.getCreatedAt(),
                savedEntry.getUpdatedAt()
        );

        LOGGER.info("Journal entry created with id: {} ", savedEntry.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/entries")
    public ResponseEntity<List<JournalGetResponse>> getAllEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<JournalGetResponse> getResponse = journalService.getAllJournalEntries(userDetails.getUsername());
        return ResponseEntity.ok().body(getResponse);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<JournalEntryResponse>> get10RecentEntries(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<JournalEntryResponse> recentJournalEntrySummary = journalService.get10RecentEntriesForUser(userDetails.getUser());
        return ResponseEntity.ok().body(recentJournalEntrySummary);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalGetResponse> getCompleteJournalEntryById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("id") Long id) {
        JournalGetResponse completeJournalEntryById = journalService.getCompleteJournalEntryById(userDetails.getUser(), id);
        return ResponseEntity.ok().body(completeJournalEntryById);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<JournalEntryResponse> deleteJournalEntryById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("id") Long id) {
        JournalEntryResponse deletedJournalEntryById = journalService.deleteJournalEntryById(userDetails.getUser(), id);
        return ResponseEntity.ok().body(deletedJournalEntryById);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JournalEntryResponse> updateJournalEntryById(@AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id, @Valid @RequestBody UpdateJournalEntryRequest request) {
        JournalEntryResponse updated = journalService.updateJournalEntryById(userDetails.getUser(), id, request);
        return ResponseEntity.ok().body(updated);
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryResponse>> getEntriesBetweenDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("from") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime to) {

        List<JournalEntryResponse> entriesBetweenRange = journalService.getJournalEntriesBetweenRange(userDetails.getUser(), from, to);
        return ResponseEntity.ok().body(entriesBetweenRange);
    }
}
